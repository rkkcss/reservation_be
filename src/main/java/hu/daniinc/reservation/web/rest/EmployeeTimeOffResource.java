package hu.daniinc.reservation.web.rest;

import hu.daniinc.reservation.domain.enumeration.BusinessPermission;
import hu.daniinc.reservation.security.annotation.RequiredBusinessPermission;
import hu.daniinc.reservation.security.annotation.TenantBusiness;
import hu.daniinc.reservation.service.EmployeeTimeOffService;
import hu.daniinc.reservation.service.dto.CreateTimeOffDTO;
import hu.daniinc.reservation.service.dto.EmployeeTimeOffDTO;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/time-offs")
public class EmployeeTimeOffResource {

    private final EmployeeTimeOffService employeeTimeOffService;

    public EmployeeTimeOffResource(EmployeeTimeOffService employeeTimeOffService) {
        this.employeeTimeOffService = employeeTimeOffService;
    }

    @PostMapping("/employee/{employeeId}")
    @RequiredBusinessPermission({ BusinessPermission.EDIT_OWN_BOOKINGS, BusinessPermission.EDIT_ALL_BOOKINGS })
    public ResponseEntity<EmployeeTimeOffDTO> create(
        @TenantBusiness Long businessId,
        @PathVariable Long employeeId,
        @Valid @RequestBody CreateTimeOffDTO dto
    ) {
        EmployeeTimeOffDTO result = employeeTimeOffService.create(businessId, employeeId, dto);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    @RequiredBusinessPermission({ BusinessPermission.EDIT_OWN_BOOKINGS, BusinessPermission.EDIT_ALL_BOOKINGS })
    public ResponseEntity<EmployeeTimeOffDTO> update(
        @TenantBusiness Long businessId,
        @PathVariable Long id,
        @Valid @RequestBody CreateTimeOffDTO dto
    ) {
        EmployeeTimeOffDTO result = employeeTimeOffService.update(businessId, id, dto);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    @RequiredBusinessPermission({ BusinessPermission.EDIT_OWN_BOOKINGS, BusinessPermission.EDIT_ALL_BOOKINGS })
    public ResponseEntity<Void> delete(@TenantBusiness Long businessId, @PathVariable Long employeeId, @PathVariable Long id) {
        employeeTimeOffService.delete(businessId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("")
    public ResponseEntity<List<EmployeeTimeOffDTO>> findAllForBusiness(
        @TenantBusiness Long businessId,
        @RequestParam(defaultValue = "all") String employeeId,
        @RequestParam LocalDate startDate,
        @RequestParam LocalDate endDate
    ) {
        List<EmployeeTimeOffDTO> result = employeeTimeOffService.findAllForBusinessAndEmployee(businessId, employeeId, startDate, endDate);
        return ResponseEntity.ok(result);
    }
}
