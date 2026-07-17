package hu.daniinc.reservation.service.impl;

import hu.daniinc.reservation.domain.Business;
import hu.daniinc.reservation.domain.BusinessEmployee;
import hu.daniinc.reservation.domain.EmployeeTimeOff;
import hu.daniinc.reservation.domain.User;
import hu.daniinc.reservation.domain.enumeration.BusinessPermission;
import hu.daniinc.reservation.domain.enumeration.TimeOffStatus;
import hu.daniinc.reservation.repository.BusinessEmployeeRepository;
import hu.daniinc.reservation.repository.BusinessRepository;
import hu.daniinc.reservation.repository.EmployeeTimeOffRepository;
import hu.daniinc.reservation.service.BusinessEmployeeService;
import hu.daniinc.reservation.service.EmployeeTimeOffService;
import hu.daniinc.reservation.service.UserService;
import hu.daniinc.reservation.service.dto.CreateTimeOffDTO;
import hu.daniinc.reservation.service.dto.EmployeeTimeOffDTO;
import hu.daniinc.reservation.service.mapper.EmployeeTimeOffMapper;
import hu.daniinc.reservation.web.rest.errors.BadRequestAlertException;
import hu.daniinc.reservation.web.rest.errors.GeneralException;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeTimeOffServiceImpl implements EmployeeTimeOffService {

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeTimeOffServiceImpl.class);

    private final EmployeeTimeOffRepository employeeTimeOffRepository;
    private final BusinessEmployeeRepository businessEmployeeRepository;
    private final BusinessRepository businessRepository;
    private final EmployeeTimeOffMapper employeeTimeOffMapper;
    private final UserService userService;
    private final BusinessEmployeeService businessEmployeeService;

    public EmployeeTimeOffServiceImpl(
        EmployeeTimeOffRepository employeeTimeOffRepository,
        BusinessEmployeeRepository businessEmployeeRepository,
        BusinessRepository businessRepository,
        EmployeeTimeOffMapper employeeTimeOffMapper,
        UserService userService,
        BusinessEmployeeService businessEmployeeService
    ) {
        this.employeeTimeOffRepository = employeeTimeOffRepository;
        this.businessEmployeeRepository = businessEmployeeRepository;
        this.businessRepository = businessRepository;
        this.employeeTimeOffMapper = employeeTimeOffMapper;
        this.userService = userService;
        this.businessEmployeeService = businessEmployeeService;
    }

    @Override
    @Transactional
    public EmployeeTimeOffDTO create(Long businessId, Long employeeId, CreateTimeOffDTO dto) {
        Business business = businessRepository
            .findById(businessId)
            .orElseThrow(() -> new EntityNotFoundException("Business not found with id: " + businessId));

        BusinessEmployee employee = businessEmployeeRepository
            .findByBusinessIdAndEmployeeId(businessId, employeeId)
            .orElseThrow(() -> new EntityNotFoundException("Employee not found"));

        validateDates(dto);

        ZoneId zone = ZoneId.of(business.getTimeZone() != null ? business.getTimeZone() : "Europe/Budapest");

        EmployeeTimeOff entity = employeeTimeOffMapper.toEntity(dto);
        entity.setBusinessEmployee(employee);
        computeInstantRange(entity, zone);

        checkNoOverlap(businessId, employeeId, entity.getStartInstant(), entity.getEndInstant(), null);

        EmployeeTimeOff saved = employeeTimeOffRepository.save(entity);
        return employeeTimeOffMapper.toDto(saved);
    }

    @Override
    @Transactional
    public EmployeeTimeOffDTO update(Long businessId, Long id, CreateTimeOffDTO dto) {
        EmployeeTimeOff existing = employeeTimeOffRepository
            .findByIdAndBusinessId(id, businessId)
            .orElseThrow(() -> new EntityNotFoundException("Time off not found with id: " + id));

        Business business = businessRepository
            .findById(businessId)
            .orElseThrow(() -> new EntityNotFoundException("Business not found with id: " + businessId));

        BusinessEmployee businessEmployee = businessEmployeeRepository
            .findByBusinessIdAndBusinessEmployeeId(businessId, dto.getBusinessEmployeeId())
            .orElseThrow(() -> new EntityNotFoundException("Employee not found"));

        BusinessEmployee requesterBusinessEmployee = businessEmployeeRepository
            .findByUserLoginAndBusinessId(businessId)
            .orElseThrow(() -> new EntityNotFoundException("Employee not found"));

        if (!requesterBusinessEmployee.hasPermission(BusinessPermission.EDIT_ALL_BOOKINGS)) {
            if (!businessEmployee.getId().equals(requesterBusinessEmployee.getId())) {
                throw new GeneralException("You can only modify your own time off!", "unauthorized-modification", HttpStatus.FORBIDDEN);
            }
        }

        validateDates(dto);

        ZoneId zone = ZoneId.of(business.getTimeZone() != null ? business.getTimeZone() : "Europe/Budapest");

        existing.setStartDate(dto.getStartDate());
        existing.setEndDate(dto.getEndDate());
        existing.setStartTime(dto.getStartTime());
        existing.setEndTime(dto.getEndTime());
        existing.setType(dto.getType());
        existing.setNote(dto.getNote());
        existing.setBusinessEmployee(businessEmployee);
        computeInstantRange(existing, zone);

        checkNoOverlap(businessId, businessEmployee.getId(), existing.getStartInstant(), existing.getEndInstant(), existing.getId());

        EmployeeTimeOff saved = employeeTimeOffRepository.save(existing);
        return employeeTimeOffMapper.toDto(saved);
    }

    private void checkNoOverlap(Long businessId, Long employeeId, Instant startInstant, Instant endInstant, Long excludeId) {
        List<EmployeeTimeOff> overlapping = employeeTimeOffRepository.findOverlapping(businessId, employeeId, startInstant, endInstant);

        boolean hasConflict = overlapping.stream().anyMatch(t -> !t.getId().equals(excludeId));

        if (hasConflict) {
            throw new BadRequestAlertException("This employee already has a time off in the given period!", null, "timeoff.overlap");
        }
    }

    @Override
    @Transactional
    public void delete(Long businessId, Long id) {
        EmployeeTimeOff existing = employeeTimeOffRepository
            .findByIdAndBusinessId(id, businessId)
            .orElseThrow(() -> new EntityNotFoundException("Time off not found with id: " + id));
        existing.setStatus(TimeOffStatus.DELETED);
        employeeTimeOffRepository.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeTimeOffDTO> findAllForBusinessAndEmployee(
        Long businessId,
        String employeeIdParam,
        LocalDate startDate,
        LocalDate endDate
    ) {
        Business business = businessRepository
            .findById(businessId)
            .orElseThrow(() -> new EntityNotFoundException("Business not found with id: " + businessId));

        ZoneId zone = business.getTimeZone() != null ? ZoneId.of(business.getTimeZone()) : ZoneId.of("Europe/Budapest");
        Instant rangeStart = startDate.atStartOfDay(zone).toInstant();
        Instant rangeEnd = endDate.plusDays(1).atStartOfDay(zone).toInstant();

        User user = userService
            .getUserWithAuthorities()
            .orElseThrow(() -> new GeneralException("No user logged in!", "no-user-logged-in", HttpStatus.NOT_FOUND));

        BusinessEmployee loggedInEmployee = businessEmployeeRepository
            .findByUserLoginAndBusinessId(businessId)
            .orElseThrow(() ->
                new GeneralException("No employee logged in for businessId: " + businessId, "no-employee-logged-in", HttpStatus.NOT_FOUND)
            );

        Long requestedEmployeeId = "all".equalsIgnoreCase(employeeIdParam) ? null : Long.parseLong(employeeIdParam);
        Long myEmployeeId = loggedInEmployee.getId();

        if (!loggedInEmployee.hasPermission(BusinessPermission.VIEW_ALL_SCHEDULE)) {
            if (requestedEmployeeId == null || !requestedEmployeeId.equals(myEmployeeId)) {
                LOG.warn("Unauthorized access attempt by {}: requested {}, allowed {}", user.getLogin(), employeeIdParam, myEmployeeId);
                requestedEmployeeId = myEmployeeId;
            }
        }

        return employeeTimeOffRepository
            .findOverlapping(businessId, requestedEmployeeId, rangeStart, rangeEnd)
            .stream()
            .map(employeeTimeOffMapper::toDto)
            .toList();
    }

    @Override
    public List<TimeOffRange> findOverlappingRanges(Long businessId, Long employeeId, Instant rangeStart, Instant rangeEnd) {
        return employeeTimeOffRepository
            .findOverlapping(businessId, employeeId, rangeStart, rangeEnd)
            .stream()
            .map(t -> new TimeOffRange(t.getStartInstant(), t.getEndInstant()))
            .toList();
    }

    private void validateDates(CreateTimeOffDTO dto) {
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new BadRequestAlertException("End date cannot be before start date!", null, "timeoff.invaliddaterange");
        }
        if (
            dto.getStartDate().isEqual(dto.getEndDate()) &&
            dto.getStartTime() != null &&
            dto.getEndTime() != null &&
            !dto.getEndTime().isAfter(dto.getStartTime())
        ) {
            throw new BadRequestAlertException("End time must be after start time!", null, "timeoff.invalidtimerange");
        }
    }

    private void computeInstantRange(EmployeeTimeOff entity, ZoneId zone) {
        Instant start = entity.getStartTime() != null
            ? entity.getStartDate().atTime(entity.getStartTime()).atZone(zone).toInstant()
            : entity.getStartDate().atStartOfDay(zone).toInstant();

        Instant end = entity.getEndTime() != null
            ? entity.getEndDate().atTime(entity.getEndTime()).atZone(zone).toInstant()
            : entity.getEndDate().plusDays(1).atStartOfDay(zone).toInstant();

        entity.setStartInstant(start);
        entity.setEndInstant(end);
    }
}
