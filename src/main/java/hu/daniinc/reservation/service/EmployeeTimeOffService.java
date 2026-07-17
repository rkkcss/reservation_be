package hu.daniinc.reservation.service;

import hu.daniinc.reservation.service.dto.CreateTimeOffDTO;
import hu.daniinc.reservation.service.dto.EmployeeTimeOffDTO;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public interface EmployeeTimeOffService {
    EmployeeTimeOffDTO create(Long businessId, Long employeeId, CreateTimeOffDTO dto);
    EmployeeTimeOffDTO update(Long businessId, Long id, CreateTimeOffDTO dto);
    void delete(Long businessId, Long id);

    /**
     * Visszaadja az összes átfedő EmployeeTimeOff Instant-tartományát
     * egy adott dolgozóra és időszakra - erre épül a slot-ellenőrzés.
     */
    List<TimeOffRange> findOverlappingRanges(Long businessId, Long employeeId, Instant rangeStart, Instant rangeEnd);

    List<EmployeeTimeOffDTO> findAllForBusinessAndEmployee(Long businessId, String employeeId, LocalDate startDate, LocalDate endDate);

    record TimeOffRange(Instant start, Instant end) {}
}
