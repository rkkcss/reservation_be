package hu.daniinc.reservation.service.impl;

import hu.daniinc.reservation.domain.*;
import hu.daniinc.reservation.domain.enumeration.AppointmentStatus;
import hu.daniinc.reservation.repository.*;
import hu.daniinc.reservation.service.AppointmentService;
import hu.daniinc.reservation.service.UserService;
import hu.daniinc.reservation.service.dto.*;
import hu.daniinc.reservation.service.jobs.AppointmentReminderJob;
import hu.daniinc.reservation.service.mapper.AppointmentMapper;
import hu.daniinc.reservation.service.quartz.AppointmentReminderService;
import hu.daniinc.reservation.service.specifications.AppointmentsSpecification;
import hu.daniinc.reservation.web.rest.errors.BadRequestAlertException;
import hu.daniinc.reservation.web.rest.errors.GeneralException;
import hu.daniinc.reservation.web.rest.errors.NotFoundException;
import io.undertow.util.BadRequestException;
import jakarta.persistence.EntityNotFoundException;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Service Implementation for managing {@link hu.daniinc.reservation.domain.Appointment}.
 */
@Service
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private static final Logger LOG = LoggerFactory.getLogger(AppointmentServiceImpl.class);

    private final AppointmentRepository appointmentRepository;

    private final AppointmentMapper appointmentMapper;
    private final CustomWorkingHoursRepository customWorkingHoursRepository;
    private final WorkingHoursRepository workingHoursRepository;
    private final BusinessRepository businessRepository;
    private final OfferingRepository offeringRepository;
    private final GuestRepository guestRepository;
    private final BusinessEmployeeRepository businessEmployeeRepository;
    private final AppointmentReminderService appointmentReminderService;

    public AppointmentServiceImpl(
        AppointmentRepository appointmentRepository,
        AppointmentMapper appointmentMapper,
        CustomWorkingHoursRepository customWorkingHoursRepository,
        WorkingHoursRepository workingHoursRepository,
        BusinessRepository businessRepository,
        OfferingRepository offeringRepository,
        GuestRepository guestRepository,
        BusinessEmployeeRepository businessEmployeeRepository,
        AppointmentReminderService appointmentReminderService
    ) {
        this.appointmentRepository = appointmentRepository;
        this.appointmentMapper = appointmentMapper;
        this.customWorkingHoursRepository = customWorkingHoursRepository;
        this.workingHoursRepository = workingHoursRepository;
        this.businessRepository = businessRepository;
        this.offeringRepository = offeringRepository;
        this.guestRepository = guestRepository;
        this.businessEmployeeRepository = businessEmployeeRepository;
        this.appointmentReminderService = appointmentReminderService;
    }

    @Override
    public AppointmentDTO save(AppointmentDTO appointmentDTO) {
        LOG.debug("Request to save Appointment : {}", appointmentDTO);
        Appointment appointment = appointmentMapper.toEntity(appointmentDTO);
        appointment = appointmentRepository.save(appointment);
        return appointmentMapper.toDto(appointment);
    }

    @Override
    public AppointmentDTO update(AppointmentDTO appointmentDTO) {
        LOG.debug("Request to update Appointment : {}", appointmentDTO);
        Appointment appointment = appointmentMapper.toEntity(appointmentDTO);
        appointment = appointmentRepository.save(appointment);
        return appointmentMapper.toDto(appointment);
    }

    @Override
    @Transactional
    public Optional<AppointmentDTO> partialUpdate(UpdateAppointmentDTO dto) {
        LOG.debug("Request to partially update Appointment : {}", dto);

        return appointmentRepository
            .findById(dto.getId())
            .map(existingAppointment -> {
                //update offering
                offeringRepository.findByIdToBusiness(dto.getOfferingId()).ifPresent(existingAppointment::setOffering);

                //update guest
                Optional.ofNullable(dto.getGuestId()).flatMap(guestRepository::findById).ifPresent(existingAppointment::setGuest);

                //rest
                existingAppointment.setStartDate(dto.getStartDate());
                existingAppointment.setEndDate(dto.getEndDate());
                existingAppointment.setNote(dto.getNote());
                existingAppointment.setStatus(dto.getStatus());
                return existingAppointment;
            })
            .map(appointmentRepository::save)
            .map(appointmentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDTO> findOverlappingAppointments(Instant startDate, Instant endDate, Long businessId, String employeeFullName) {
        Specification<Appointment> spec = AppointmentsSpecification.overlappingAppointmentsByEmployeeName(
            startDate,
            endDate,
            businessId,
            employeeFullName
        );

        return appointmentRepository.findAll(spec).stream().map(appointmentMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AppointmentDTO> findOne(Long id) {
        LOG.debug("Request to get Appointment : {}", id);
        return appointmentRepository.findById(id).map(appointmentMapper::toDto);
    }

    @Override
    public void logicalDelete(Long id) {
        LOG.debug("Request to delete Appointment : {}", id);

        Appointment appointment = appointmentRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Appointment not found with ID: " + id));

        if (!appointmentRepository.isUserTheAppointmentOwnerById(id)) {
            throw new RuntimeException("You are not the owner of this appointment");
        }
        appointment.setStatus(AppointmentStatus.DELETED);
        appointmentRepository.save(appointment);
    }

    @Override
    public Map<LocalDate, List<Instant>> getAvailableSlotsBetweenDates(
        Long businessId,
        Long employeeId,
        LocalDate from,
        LocalDate to,
        Duration slotLength
    ) {
        Map<LocalDate, List<Instant>> availableSlotsMap = new LinkedHashMap<>();

        Business business = businessRepository.findById(businessId).orElseThrow(() -> new EntityNotFoundException("Business not found"));

        // Max előre foglalható idő
        Integer maxWeeks = business.getMaxWeeksInAdvance(); // pl. 26
        if (maxWeeks != null && maxWeeks > 0) {
            LocalDate maxAllowedDate = LocalDate.now().plusWeeks(maxWeeks);
            if (to.isAfter(maxAllowedDate)) {
                to = maxAllowedDate;
            }
        }

        ZoneId zone = ZoneId.of("Europe/Budapest");

        // Lekérjük az összes releváns appointmentet employee-specifikusan
        Instant overallStart = from.atStartOfDay(zone).toInstant();
        Instant overallEnd = to.plusDays(1).atStartOfDay(zone).toInstant();

        List<Appointment> allAppointments = appointmentRepository.findByBusinessIdAndEmployeeIdAndDateRange(
            businessId,
            employeeId,
            overallStart,
            overallEnd
        );

        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            List<Instant> slots = new ArrayList<>();

            // Custom working hours employee-specifikusan
            Optional<CustomWorkingHours> customOpt = customWorkingHoursRepository.findByBusinessIdAndEmployeeIdAndWorkDate(
                businessId,
                employeeId,
                date
            );

            if (customOpt.isPresent()) {
                CustomWorkingHours cwh = customOpt.get();
                Instant start = cwh.getStartTime().toInstant();
                Instant end = cwh.getEndTime().toInstant();

                Instant current = start;
                while (!current.plus(slotLength).isAfter(end)) {
                    slots.add(current);
                    current = current.plus(slotLength);
                }
            } else {
                // Default working hours employee-specifikusan
                int dayOfWeek = date.getDayOfWeek().getValue();
                List<WorkingHours> workingHoursList = workingHoursRepository.findByBusinessIdAndEmployeeIdAndDayOfWeek(
                    businessId,
                    employeeId,
                    dayOfWeek
                );

                if (workingHoursList.isEmpty()) continue;

                for (WorkingHours wh : workingHoursList) {
                    Instant start = date.atTime(wh.getStartTime()).atZone(zone).toInstant();
                    Instant end = date.atTime(wh.getEndTime()).atZone(zone).toInstant();

                    Instant current = start;
                    while (!current.plus(slotLength).isAfter(end)) {
                        slots.add(current);
                        current = current.plus(slotLength);
                    }
                }
            }

            // Szűrés ütköző foglalások alapján
            Instant now = Instant.now();

            List<Instant> available = slots
                .stream()
                .filter(slot -> !slot.isBefore(now))
                .filter(slot ->
                    allAppointments
                        .stream()
                        .noneMatch(appt -> slot.isBefore(appt.getEndDate()) && slot.plus(slotLength).isAfter(appt.getStartDate()))
                )
                .collect(Collectors.toList());

            if (!available.isEmpty()) {
                availableSlotsMap.put(date, available);
            }
        }

        return availableSlotsMap;
    }

    @Override
    @Transactional
    public AppointmentDTO saveByOwner(Long employeeId, Long businessId, CreateAppointmentRequestDTO createAppointmentRequestDTO) {
        BusinessEmployee employee = businessEmployeeRepository
            .findByUserLoginAndBusinessId(businessId)
            .orElseThrow(() -> new GeneralException("Employee not found!", "employee-not-found", HttpStatus.NOT_FOUND));

        Appointment appointment = new Appointment();
        appointment.setStartDate(createAppointmentRequestDTO.getStartDate());
        appointment.setEndDate(createAppointmentRequestDTO.getEndDate());
        appointment.setNote(createAppointmentRequestDTO.getNote());
        appointment.setBusinessEmployee(employee);
        appointment.setCreatedDate(Instant.now());

        //set offering
        offeringRepository.findByIdToBusiness(createAppointmentRequestDTO.getOfferingId()).ifPresent(appointment::setOffering);

        //set status
        appointment.setStatus(createAppointmentRequestDTO.getStatus());

        //set guest
        Optional.ofNullable(createAppointmentRequestDTO.getGuestId()).flatMap(guestRepository::findById).ifPresent(appointment::setGuest);

        appointment.setModifierToken(UUID.randomUUID().toString());

        return appointmentMapper.toDto(appointmentRepository.save(appointment));
    }

    @Override
    @Transactional
    public AppointmentDTO saveAppointmentByGuest(Long businessId, Long employeeId, CreateAppointmentByGuestDTO dto) {
        // --- 1) Check if the business has the offering ---
        if (!offeringRepository.isBusinessHasTheOffer(dto.getOfferingId(), businessId)) {
            LOG.error("Business: {} doesn't have the offer with id: {}", businessId, dto.getOfferingId());
            throw new RuntimeException("Business doesn't have the offer with id: " + dto.getOfferingId());
        }

        // --- 2) Check if employee exists ---
        BusinessEmployee employee = businessEmployeeRepository
            .findByBusinessIdAndEmployeeId(businessId, employeeId)
            .orElseThrow(() -> new EntityNotFoundException("Employee not found"));

        // --- 3) Check if slot is available for this employee ---
        if (!isSlotAvailable(businessId, employee.getId(), dto.getDate(), dto.getTime(), dto.getOfferingId())) {
            throw new BadRequestAlertException("Appointment is reserved!", null, "appointment.reserved");
        }

        // --- 4) Retrieve business ---
        Business business = businessRepository
            .findById(businessId)
            .orElseThrow(() -> new EntityNotFoundException("No business found for login"));

        // --- 5) Set guest ---
        Guest guest = guestRepository
            .findByEmail(dto.getEmail(), business.getId())
            .orElseGet(() -> {
                Guest newGuest = new Guest();
                newGuest.setEmail(dto.getEmail());
                newGuest.setName(dto.getName());
                newGuest.setPhoneNumber(dto.getPhoneNumber());
                newGuest.setBusinessEmployee(employee);
                newGuest.setCanBook(true);
                return guestRepository.save(newGuest);
            });

        if (!guest.getCanBook()) {
            LOG.debug("Guest cannot book: guest id {}, canBook: {}", guest.getId(), guest.getCanBook());
            throw new BadRequestAlertException("Guest can't book!", null, "guest.cantbook");
        }

        // --- 6) Create appointment ---
        Appointment appointment = new Appointment();
        appointment.setGuest(guest);
        appointment.setBusinessEmployee(employee);

        // Offering
        Offering offering = offeringRepository
            .findById(dto.getOfferingId())
            .orElseThrow(() -> new RuntimeException("No offering found for id: " + dto.getOfferingId()));
        appointment.setOffering(offering);

        // --- 7) Set status ---
        if (business.getAppointmentApprovalRequired()) {
            appointment.setStatus(AppointmentStatus.PENDING);
        } else {
            appointment.setStatus(AppointmentStatus.CONFIRMED);
        }

        // --- 8) Set startDate & endDate ---
        ZoneId zone = ZoneId.of("Europe/Budapest");
        Instant startDate = dto.getDate().atTime(dto.getTime()).atZone(zone).toInstant();
        Instant endDate = startDate.plus(Duration.ofMinutes(offering.getDurationMinutes()));

        appointment.setStartDate(startDate);
        appointment.setEndDate(endDate);

        appointment.setModifierToken(UUID.randomUUID().toString());

        // --- 9) Save appointment
        Appointment savedAppointment = appointmentRepository.save(appointment);

        // --- 10) Schedule Email Reminder ---
        if (savedAppointment.getStatus() == AppointmentStatus.CONFIRMED) {
            appointmentReminderService.scheduleEmailReminder(savedAppointment);
        }

        //return DTO ---
        return appointmentMapper.toDto(savedAppointment);
    }

    //get and appointment by guest name and appointment ID
    @Override
    public AppointmentDTO getAppointmentByModifierToken(String token) {
        return appointmentRepository
            .findByModifierToken(token)
            .map(appointmentMapper::toDto)
            .orElseThrow(() -> new NotFoundException("Entity not found", token));
    }

    //cancel appointment by modifier token
    @Override
    public void cancelByModifierToken(String modifierToken) {
        Appointment appointment = appointmentRepository
            .findByModifierToken(modifierToken)
            .orElseThrow(() -> new EntityNotFoundException("Appointment not found"));
        appointmentRepository.deleteById(appointment.getId());
    }

    @Override
    public List<AppointmentDTO> getAllPendingAppointments(Long businessId) {
        return appointmentRepository
            .findAllPendingAppointments(businessId)
            .stream()
            .map(appointmentMapper::toDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AppointmentDTO approveAppointment(Long appointmentId, Long employeeId) {
        Appointment appointment = appointmentRepository
            .findByIdAndLoggedInOwner(appointmentId, employeeId)
            .orElseThrow(() -> new EntityNotFoundException("appointment"));
        appointment.setStatus(AppointmentStatus.CONFIRMED);

        return appointmentMapper.toDto(appointmentRepository.save(appointment));
    }

    @Override
    @Transactional
    public AppointmentDTO cancelAppointment(Long appointmentId, Long employeeId) {
        Appointment appointment = appointmentRepository
            .findByIdAndLoggedInOwner(appointmentId, employeeId)
            .orElseThrow(() -> new EntityNotFoundException("appointment"));
        appointment.setStatus(AppointmentStatus.CANCELLED);

        return appointmentMapper.toDto(appointmentRepository.save(appointment));
    }

    //checking the appointment is available return TRUE if yes else FALSE
    public boolean isSlotAvailable(Long businessId, Long employeeId, LocalDate date, LocalTime time, Long offeringId) {
        ZoneId zone = ZoneId.of("Europe/Budapest");

        // Offering lekérése a hossz miatt
        Offering offering = offeringRepository.findById(offeringId).orElseThrow(() -> new RuntimeException("Offering not found"));

        Duration duration = Duration.ofMinutes(offering.getDurationMinutes());

        // Slot kezdete és vége
        ZonedDateTime slotStart = ZonedDateTime.of(date, time, zone);
        ZonedDateTime slotEnd = slotStart.plus(duration);

        // múlt kizárása
        if (slotStart.isBefore(ZonedDateTime.now(zone))) {
            return false;
        }

        // Nap eleje és vége Instant-re
        Instant dayStart = date.atStartOfDay(zone).toInstant();
        Instant dayEnd = date.plusDays(1).atStartOfDay(zone).toInstant();

        // Appointments lekérése employee szinten
        List<Appointment> appointments = appointmentRepository.findByBusinessIdAndEmployeeIdAndDateRange(
            businessId,
            employeeId,
            dayStart,
            dayEnd
        );

        Instant slotStartInstant = slotStart.toInstant();
        Instant slotEndInstant = slotEnd.toInstant();

        // Időütközés ellenőrzés
        boolean overlaps = appointments
            .stream()
            .anyMatch(a -> slotStartInstant.isBefore(a.getEndDate()) && slotEndInstant.isAfter(a.getStartDate()));

        if (overlaps) return false;

        // Egyedi munkaidő employee szinten
        Optional<CustomWorkingHours> customOpt = customWorkingHoursRepository.findByBusinessIdAndEmployeeIdAndWorkDate(
            businessId,
            employeeId,
            date
        );

        if (customOpt.isPresent()) {
            CustomWorkingHours cwh = customOpt.get();

            ZonedDateTime whStart = cwh.getStartTime();
            ZonedDateTime whEnd = cwh.getEndTime();

            // Slot belefér-e az egyedi munkaidőbe?
            return !slotStart.isBefore(whStart) && !slotEnd.isAfter(whEnd);
        }

        // Default working hours employee szinten
        int dow = date.getDayOfWeek().getValue();
        List<WorkingHours> whList = workingHoursRepository.findByBusinessIdAndEmployeeIdAndDayOfWeek(businessId, employeeId, dow);

        for (WorkingHours wh : whList) {
            ZonedDateTime whStart = date.atTime(wh.getStartTime()).atZone(zone);
            ZonedDateTime whEnd = date.atTime(wh.getEndTime()).atZone(zone);

            if (!slotStart.isBefore(whStart) && !slotEnd.isAfter(whEnd)) {
                return true;
            }
        }

        return false;
    }
}
