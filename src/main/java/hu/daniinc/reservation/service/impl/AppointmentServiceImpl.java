package hu.daniinc.reservation.service.impl;

import hu.daniinc.reservation.domain.*;
import hu.daniinc.reservation.domain.enumeration.AppointmentStatus;
import hu.daniinc.reservation.repository.*;
import hu.daniinc.reservation.service.AppointmentService;
import hu.daniinc.reservation.service.dto.AppointmentDTO;
import hu.daniinc.reservation.service.dto.CreateAppointmentByGuestDTO;
import hu.daniinc.reservation.service.dto.CreateAppointmentRequestDTO;
import hu.daniinc.reservation.service.dto.UpdateAppointmentDTO;
import hu.daniinc.reservation.service.mapper.AppointmentMapper;
import jakarta.persistence.EntityNotFoundException;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public AppointmentServiceImpl(
        AppointmentRepository appointmentRepository,
        AppointmentMapper appointmentMapper,
        CustomWorkingHoursRepository customWorkingHoursRepository,
        WorkingHoursRepository workingHoursRepository,
        BusinessRepository businessRepository,
        OfferingRepository offeringRepository,
        GuestRepository guestRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.appointmentMapper = appointmentMapper;
        this.customWorkingHoursRepository = customWorkingHoursRepository;
        this.workingHoursRepository = workingHoursRepository;
        this.businessRepository = businessRepository;
        this.offeringRepository = offeringRepository;
        this.guestRepository = guestRepository;
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
    public List<AppointmentDTO> findOverlappingAppointments(ZonedDateTime startDate, ZonedDateTime endDate) {
        LOG.debug("Request to get all Appointments");
        return appointmentRepository.findOverlappingAppointments(startDate, endDate).stream().map(appointmentMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AppointmentDTO> findOne(Long id) {
        LOG.debug("Request to get Appointment : {}", id);
        return appointmentRepository.findById(id).map(appointmentMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Appointment : {}", id);

        if (!appointmentRepository.isBusinessTheOwnerById(id)) {
            throw new RuntimeException("You are not the owner of this appointment");
        }
        appointmentRepository.deleteById(id);
    }

    @Override
    public Map<LocalDate, List<ZonedDateTime>> getAvailableSlotsBetweenDates(
        Long businessId,
        LocalDate from,
        LocalDate to,
        Duration slotLength
    ) {
        Map<LocalDate, List<ZonedDateTime>> availableSlotsMap = new LinkedHashMap<>();

        // Lekérjük az összes releváns appointmentet előre
        ZonedDateTime overallStart = from.atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime overallEnd = to.plusDays(1).atStartOfDay(ZoneId.systemDefault());
        List<Appointment> allAppointments = appointmentRepository.findByBusinessIdAndDateRange(businessId, overallStart, overallEnd);

        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            List<ZonedDateTime> slots = new ArrayList<>();

            Optional<CustomWorkingHours> customOpt = customWorkingHoursRepository.findByBusinessIdAndWorkDate(businessId, date);

            if (customOpt.isPresent()) {
                // Ha van egyedi munkaidő, csak azt vesszük figyelembe
                CustomWorkingHours cwh = customOpt.get();
                ZonedDateTime start = cwh.getStartTime();
                ZonedDateTime end = cwh.getEndTime();

                ZonedDateTime current = start;
                while (!current.plus(slotLength).isAfter(end)) {
                    slots.add(current);
                    current = current.plus(slotLength);
                }
            } else {
                // Alapértelmezett (több) munkasáv lekérdezése
                int dayOfWeek = date.getDayOfWeek().getValue();
                List<WorkingHours> workingHoursList = workingHoursRepository.findByBusinessIdAndDayOfWeek(businessId, dayOfWeek);

                if (workingHoursList.isEmpty()) continue;

                for (WorkingHours wh : workingHoursList) {
                    ZonedDateTime start = date.atTime(wh.getStartTime()).atZone(ZoneId.systemDefault());
                    ZonedDateTime end = date.atTime(wh.getEndTime()).atZone(ZoneId.systemDefault());

                    ZonedDateTime current = start;
                    while (!current.plus(slotLength).isAfter(end)) {
                        slots.add(current);
                        current = current.plus(slotLength);
                    }
                }
            }

            // Szűrés ütköző foglalások alapján

            ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());

            List<ZonedDateTime> available = slots
                .stream()
                .filter(slot -> !slot.isBefore(now)) //can't add appointment in the past
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
    public AppointmentDTO saveByOwner(CreateAppointmentRequestDTO createAppointmentRequestDTO) {
        Appointment appointment = new Appointment();
        appointment.setStartDate(createAppointmentRequestDTO.getStartDate());
        appointment.setEndDate(createAppointmentRequestDTO.getEndDate());
        appointment.setNote(createAppointmentRequestDTO.getNote());

        appointment.setCreatedDate(ZonedDateTime.now());

        Business ownerBusiness = businessRepository.findByLogin().orElseThrow(() -> new RuntimeException("No business found for login"));
        appointment.setBusiness(ownerBusiness);

        //set offering
        offeringRepository.findByIdToBusiness(createAppointmentRequestDTO.getOfferingId()).ifPresent(appointment::setOffering);

        //set status
        appointment.setStatus(AppointmentStatus.CONFIRMED);

        //set guest
        Optional.ofNullable(createAppointmentRequestDTO.getGuestId()).flatMap(guestRepository::findById).ifPresent(appointment::setGuest);

        return appointmentMapper.toDto(appointmentRepository.save(appointment));
    }

    @Override
    @Transactional
    public AppointmentDTO saveAppointmentByGuest(CreateAppointmentByGuestDTO createAppointmentByGuestDTO) {
        //check the business has the selected offer
        if (
            !offeringRepository.isBusinessHasTheOffer(
                createAppointmentByGuestDTO.getOfferingId(),
                createAppointmentByGuestDTO.getBusinessId()
            )
        ) {
            LOG.error(
                "Business: {} doesn't have the offer with id: {}",
                createAppointmentByGuestDTO.getBusinessId(),
                createAppointmentByGuestDTO.getOfferingId()
            );
            throw new RuntimeException("Business doesn't have the offer with id: " + createAppointmentByGuestDTO.getOfferingId());
        }

        //check the date is available for the selected time
        if (
            !isSlotAvailable(
                createAppointmentByGuestDTO.getBusinessId(),
                createAppointmentByGuestDTO.getDate(),
                createAppointmentByGuestDTO.getTime(),
                createAppointmentByGuestDTO.getOfferingId()
            )
        ) {
            throw new IllegalArgumentException("A választott időpont nem elérhető.");
        }

        Appointment appointment = new Appointment();

        //set business

        Business business = businessRepository
            .findById(createAppointmentByGuestDTO.getBusinessId())
            .orElseThrow(() -> new EntityNotFoundException("No business found for login"));

        appointment.setBusiness(business);

        //set guest
        Guest guest = guestRepository
            .findByEmail(createAppointmentByGuestDTO.getEmail())
            .orElseGet(() -> {
                Guest newGuest = new Guest();
                newGuest.setEmail(createAppointmentByGuestDTO.getEmail());
                newGuest.setName(createAppointmentByGuestDTO.getName());
                newGuest.setPhoneNumber(createAppointmentByGuestDTO.getPhoneNumber());
                return guestRepository.save(newGuest);
            });

        appointment.setGuest(guest);
        Offering offering = offeringRepository
            .findById(createAppointmentByGuestDTO.getOfferingId())
            .orElseThrow(() -> new RuntimeException("No offering found for id: " + createAppointmentByGuestDTO.getOfferingId()));

        appointment.setOffering(offering);

        //set startDate and endDate
        ZonedDateTime startDate = ZonedDateTime.of(
            createAppointmentByGuestDTO.getDate(),
            createAppointmentByGuestDTO.getTime(),
            ZoneId.systemDefault()
        );

        ZonedDateTime endDate = startDate.plusMinutes(offering.getDurationMinutes());

        appointment.setStartDate(startDate);
        appointment.setEndDate(endDate);
        appointment.setStatus(AppointmentStatus.CONFIRMED);

        return appointmentMapper.toDto(appointmentRepository.save(appointment));
    }

    //checking the appointment is available return TRUE if yes else FALSE
    public boolean isSlotAvailable(Long businessId, LocalDate date, LocalTime time, Long offeringId) {
        // --- 0) explicit Budapest-zóna ---
        ZoneId zone = ZoneId.of("Europe/Budapest");

        // --- 1) Offering hossz lekérése ---
        Offering offering = offeringRepository
            .findById(offeringId)
            .orElseThrow(() -> new EntityNotFoundException("Offering not found: " + offeringId));
        Duration slotLength = Duration.ofMinutes(offering.getDurationMinutes());

        // --- 2) slotStart / slotEnd ---
        ZonedDateTime slotStart = ZonedDateTime.of(date, time, zone);
        ZonedDateTime slotEnd = slotStart.plus(slotLength);
        LOG.debug("  → slotStart = {}, slotEnd = {}", slotStart, slotEnd);

        // --- 3) múlt kizárása ---
        if (slotStart.isBefore(ZonedDateTime.now(zone))) {
            LOG.debug("  → reject: past slot");
            return false;
        }

        // --- 4) Ütköző foglalások ellenőrzése ---
        List<Appointment> appointments = appointmentRepository.findByBusinessIdAndDateRange(
            businessId,
            date.atStartOfDay(zone),
            date.plusDays(1).atStartOfDay(zone)
        );
        appointments.forEach(a -> LOG.debug("    existing appt: {} – {}", a.getStartDate(), a.getEndDate()));
        boolean overlaps = appointments.stream().anyMatch(a -> slotStart.isBefore(a.getEndDate()) && slotEnd.isAfter(a.getStartDate()));
        if (overlaps) {
            LOG.debug("  → reject: overlap detected");
            return false;
        }

        // --- 5) Munkaidő-sávok kigyűjtése ---
        Optional<CustomWorkingHours> customOpt = customWorkingHoursRepository.findByBusinessIdAndWorkDate(businessId, date);
        if (customOpt.isPresent()) {
            CustomWorkingHours cwh = customOpt.get();
            LOG.debug("  → using CUSTOM WH: {} – {}", cwh.getStartTime(), cwh.getEndTime());
            boolean ok = !slotStart.isBefore(cwh.getStartTime()) && !slotEnd.isAfter(cwh.getEndTime());
            LOG.debug("     → in custom hours? {}", ok);
            return ok;
        }

        int dow = date.getDayOfWeek().getValue();
        LOG.debug("  → default dayOfWeek = {}", dow);
        List<WorkingHours> whList = workingHoursRepository.findByBusinessIdAndDayOfWeek(businessId, dow);
        whList.forEach(wh -> LOG.debug("    default WH: {} – {}", wh.getStartTime(), wh.getEndTime()));

        // --- 6) Default sávok ellenőrzése ---
        for (WorkingHours wh : whList) {
            ZonedDateTime whStart = date.atTime(wh.getStartTime()).atZone(zone);
            ZonedDateTime whEnd = date.atTime(wh.getEndTime()).atZone(zone);
            boolean ok = !slotStart.isBefore(whStart) && !slotEnd.isAfter(whEnd);
            LOG.debug("    check against {}–{}: {}", whStart, whEnd, ok);
            if (ok) {
                LOG.debug("  → accept: fits in default working hours");
                return true;
            }
        }

        LOG.debug("  → reject: no working-hours slot fits");
        return false;
    }
}
