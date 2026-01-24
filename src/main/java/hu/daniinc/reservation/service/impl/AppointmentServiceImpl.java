package hu.daniinc.reservation.service.impl;

import hu.daniinc.reservation.domain.*;
import hu.daniinc.reservation.domain.enumeration.AppointmentStatus;
import hu.daniinc.reservation.domain.enumeration.BusinessPermission;
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
    private final UserService userService;

    public AppointmentServiceImpl(
        AppointmentRepository appointmentRepository,
        AppointmentMapper appointmentMapper,
        CustomWorkingHoursRepository customWorkingHoursRepository,
        WorkingHoursRepository workingHoursRepository,
        BusinessRepository businessRepository,
        OfferingRepository offeringRepository,
        GuestRepository guestRepository,
        BusinessEmployeeRepository businessEmployeeRepository,
        AppointmentReminderService appointmentReminderService,
        UserService userService
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
        this.userService = userService;
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
        User user = userService
            .getUserWithAuthorities()
            .orElseThrow(() -> new GeneralException("No user logged in!", "no-user-logged-in", HttpStatus.NOT_FOUND));
        BusinessEmployee businessEmployee = businessEmployeeRepository
            .findByUserLoginAndBusinessId(businessId)
            .orElseThrow(() ->
                new GeneralException("No employee logged in for businessId: " + businessId, "no-employee-logged-in", HttpStatus.NOT_FOUND)
            );

        String myName = user.getFullName();

        //if employee don't have permission to view all or different employee, we set the filter name to the logged in employee
        if (!myName.equals(employeeFullName) && !businessEmployee.hasPermission(BusinessPermission.VIEW_ALL_SCHEDULE)) {
            LOG.warn("Unauthorized access attempt by {}: requested {}, allowed {}", user.getLogin(), employeeFullName, myName);
            employeeFullName = myName;
        }
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

        // 1. Business lekérése (időzóna és max foglalási idő miatt)
        Business business = businessRepository.findById(businessId).orElseThrow(() -> new EntityNotFoundException("Business not found"));

        // Időzóna dinamikus kezelése
        ZoneId zone = ZoneId.of(business.getTimeZone() != null ? business.getTimeZone() : "Europe/Budapest");

        // 2. Max előre foglalható idő korlátozása
        Integer maxWeeks = business.getMaxWeeksInAdvance();
        if (maxWeeks != null && maxWeeks > 0) {
            LocalDate maxAllowedDate = LocalDate.now(zone).plusWeeks(maxWeeks);
            if (to.isAfter(maxAllowedDate)) {
                to = maxAllowedDate;
            }
        }

        // 3. Összes releváns foglalás lekérése a tartományra
        Instant overallStart = from.atStartOfDay(zone).toInstant();
        Instant overallEnd = to.plusDays(1).atStartOfDay(zone).toInstant();

        List<Appointment> allAppointments = appointmentRepository.findByBusinessIdAndEmployeeIdAndDateRange(
            businessId,
            employeeId,
            overallStart,
            overallEnd
        );

        Instant now = Instant.now();

        // 4. Napokon való végigfutás
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            List<Instant> potentialSlots = new ArrayList<>();

            // Egyedi munkaidő ellenőrzés
            Optional<CustomWorkingHours> customOpt = customWorkingHoursRepository.findByBusinessIdAndEmployeeIdAndWorkDate(
                businessId,
                employeeId,
                date
            );

            if (customOpt.isPresent()) {
                CustomWorkingHours cwh = customOpt.get();
                generateSlots(potentialSlots, cwh.getStartTime(), cwh.getEndTime(), slotLength);
            } else {
                // Alapértelmezett munkaidő
                int dayOfWeek = date.getDayOfWeek().getValue();
                List<WorkingHours> whList = workingHoursRepository.findByBusinessIdAndEmployeeIdAndDayOfWeek(
                    businessId,
                    employeeId,
                    dayOfWeek
                );

                for (WorkingHours wh : whList) {
                    // Itt vetítjük rá a LocalTime-ot az adott napra a cég zónájában
                    Instant start = date.atTime(wh.getStartTime()).atZone(zone).toInstant();
                    Instant end = date.atTime(wh.getEndTime()).atZone(zone).toInstant();
                    generateSlots(potentialSlots, start, end, slotLength);
                }
            }

            // 5. Szűrés: Múltbeli időpontok és ütközések kiszűrése
            List<Instant> available = potentialSlots
                .stream()
                .filter(slot -> !slot.isBefore(now)) // Ne legyen a múltban
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

    /**
     * Segédmetódus a slotok legenerálásához két időpont között
     */
    private void generateSlots(List<Instant> slots, Instant start, Instant end, Duration slotLength) {
        Instant current = start;
        while (!current.plus(slotLength).isAfter(end)) {
            slots.add(current);
            current = current.plus(slotLength);
        }
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
        // 1. Business lekérése az elején (kell az időzóna és az approval beállítás miatt)
        Business business = businessRepository
            .findById(businessId)
            .orElseThrow(() -> new EntityNotFoundException("Business not found with id: " + businessId));

        // Időzóna meghatározása (ha a cégnek nincs megadva, default Europe/Budapest)
        ZoneId zone = ZoneId.of(business.getTimeZone() != null ? business.getTimeZone() : "Europe/Budapest");

        // 2. Ellenőrizzük, hogy a cég nyújtja-e ezt a szolgáltatást
        if (!offeringRepository.isBusinessHasTheOffer(dto.getOfferingId(), businessId)) {
            throw new RuntimeException("Business doesn't have the offer with id: " + dto.getOfferingId());
        }

        // 3. Alkalmazott létezésének ellenőrzése
        BusinessEmployee employee = businessEmployeeRepository
            .findByBusinessIdAndEmployeeId(businessId, employeeId)
            .orElseThrow(() -> new EntityNotFoundException("Employee not found"));

        // 4. Szabad hely ellenőrzése (átadjuk a kinyert zónát)
        if (!isSlotAvailable(businessId, employeeId, dto.getDate(), dto.getTime(), dto.getOfferingId(), zone)) {
            throw new BadRequestAlertException("Appointment is reserved or outside working hours!", null, "appointment.reserved");
        }

        // 5. Vendég kezelése
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
            throw new BadRequestAlertException("Guest can't book!", null, "guest.cantbook");
        }

        // 6. Foglalás létrehozása
        Appointment appointment = new Appointment();
        appointment.setGuest(guest);
        appointment.setBusinessEmployee(employee);

        Offering offering = offeringRepository.findById(dto.getOfferingId()).orElseThrow(() -> new RuntimeException("No offering found"));
        appointment.setOffering(offering);

        // Státusz beállítása
        appointment.setStatus(business.getAppointmentApprovalRequired() ? AppointmentStatus.PENDING : AppointmentStatus.CONFIRMED);

        // Start és End dátum kiszámítása Instant-ként
        Instant startDate = dto.getDate().atTime(dto.getTime()).atZone(zone).toInstant();
        Instant endDate = startDate.plusSeconds(offering.getDurationMinutes() * 60L);

        appointment.setStartDate(startDate);
        appointment.setEndDate(endDate);
        appointment.setModifierToken(UUID.randomUUID().toString());

        // 7. Mentés és Reminder
        Appointment savedAppointment = appointmentRepository.save(appointment);

        if (savedAppointment.getStatus() == AppointmentStatus.CONFIRMED) {
            appointmentReminderService.scheduleEmailReminder(savedAppointment);
        }

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
    public boolean isSlotAvailable(Long businessId, Long employeeId, LocalDate date, LocalTime time, Long offeringId, ZoneId zone) {
        // Offering lekérése a hossz miatt
        Offering offering = offeringRepository.findById(offeringId).orElseThrow(() -> new RuntimeException("Offering not found"));

        // 1. Slot kiszámítása a cég időzónájában, majd konvertálás Instant-ra
        Instant slotStart = date.atTime(time).atZone(zone).toInstant();
        Instant slotEnd = slotStart.plusSeconds(offering.getDurationMinutes() * 60L);

        // 2. Múlt kizárása (azonnali ellenőrzés UTC szerint)
        if (slotStart.isBefore(Instant.now())) {
            LOG.debug("Booking attempt in the past: {}", slotStart);
            return false;
        }

        // 3. Meglévő foglalások (Appointments) ellenőrzése
        Instant dayStart = date.atStartOfDay(zone).toInstant();
        Instant dayEnd = date.plusDays(1).atStartOfDay(zone).toInstant();

        List<Appointment> appointments = appointmentRepository.findByBusinessIdAndEmployeeIdAndDateRange(
            businessId,
            employeeId,
            dayStart,
            dayEnd
        );

        boolean overlaps = appointments.stream().anyMatch(a -> slotStart.isBefore(a.getEndDate()) && slotEnd.isAfter(a.getStartDate()));

        if (overlaps) {
            LOG.debug("Slot overlaps with existing appointment for employee {}", employeeId);
            return false;
        }

        // 4. Munkaidő ellenőrzése (Egyedi munkaidő elsőbbséget élvez)
        Optional<CustomWorkingHours> customOpt = customWorkingHoursRepository.findByBusinessIdAndEmployeeIdAndWorkDate(
            businessId,
            employeeId,
            date
        );

        if (customOpt.isPresent()) {
            CustomWorkingHours cwh = customOpt.get();
            // Feltételezzük, hogy a DB-ben a CustomWorkingHours már Instant (UTC)
            return !slotStart.isBefore(cwh.getStartTime()) && !slotEnd.isAfter(cwh.getEndTime());
        }

        // 5. Alapértelmezett munkaidő ellenőrzése
        int dow = date.getDayOfWeek().getValue();
        List<WorkingHours> whList = workingHoursRepository.findByBusinessIdAndEmployeeIdAndDayOfWeek(businessId, employeeId, dow);

        for (WorkingHours wh : whList) {
            Instant whStart = date.atTime(wh.getStartTime()).atZone(zone).toInstant();
            Instant whEnd = date.atTime(wh.getEndTime()).atZone(zone).toInstant();

            if (!slotStart.isBefore(whStart) && !slotEnd.isAfter(whEnd)) {
                return true;
            }
        }

        LOG.debug("No working hours defined or slot is outside of working hours for employee {}", employeeId);
        return false;
    }
}
