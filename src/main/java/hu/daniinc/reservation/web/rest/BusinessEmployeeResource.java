package hu.daniinc.reservation.web.rest;

import hu.daniinc.reservation.domain.enumeration.BusinessPermission;
import hu.daniinc.reservation.domain.enumeration.BusinessRole;
import hu.daniinc.reservation.security.annotation.RequireBusinessRole;
import hu.daniinc.reservation.security.annotation.RequiredBusinessPermission;
import hu.daniinc.reservation.service.BusinessEmployeeService;
import hu.daniinc.reservation.service.UserService;
import hu.daniinc.reservation.service.dto.BusinessEmployeeDTO;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;

@RestController
@RequestMapping("/api/business-employee")
public class BusinessEmployeeResource {

    private static final Logger LOG = LoggerFactory.getLogger(BusinessEmployeeResource.class);

    private static final String ENTITY_NAME = "businessEmployee";
    private final UserService userService;
    private final BusinessEmployeeService businessEmployeeService;

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    public BusinessEmployeeResource(UserService userService, BusinessEmployeeService businessEmployeeService) {
        this.userService = userService;
        this.businessEmployeeService = businessEmployeeService;
    }

    @RequireBusinessRole(value = { BusinessRole.OWNER, BusinessRole.MANAGER })
    @PostMapping("/business/{businessId}")
    public ResponseEntity<BusinessEmployeeDTO> createBusinessEmployee(
        @PathVariable Long businessId,
        @RequestBody BusinessEmployeeDTO businessEmployeeDTO
    ) {
        if (businessEmployeeDTO == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok(businessEmployeeService.createBusinessEmployeeByBusinessId(businessId, businessEmployeeDTO));
    }

    //get current logged in user joined businesses
    @GetMapping("/current")
    public ResponseEntity<Set<BusinessEmployeeDTO>> getLoggedInEmployeeOptions() {
        return ResponseEntity.ok(businessEmployeeService.findAllByLoggedInUser());
    }

    //get business employees by business ID
    @RequiredBusinessPermission(BusinessPermission.MANAGE_EMPLOYEES)
    @GetMapping("/business/{businessId}/employees")
    public ResponseEntity<List<BusinessEmployeeDTO>> getBusinessEmployees(@PathVariable Long businessId, Pageable pageable) {
        if (businessId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Page<BusinessEmployeeDTO> page = businessEmployeeService.findAllByBusinessId(businessId, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/business/{businessId}/employee/{employeeId}")
    public ResponseEntity<BusinessEmployeeDTO> getBusinessEmployee(@PathVariable Long businessId, @PathVariable Long employeeId) {
        if (employeeId == null || businessId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return ResponseEntity.status(HttpStatus.OK).body(businessEmployeeService.findByBusinessIdAndUserId(businessId, employeeId));
    }

    //update business employee permissions
    @RequiredBusinessPermission(value = BusinessPermission.MANAGE_EMPLOYEES, businessIdParam = "businessEmployeeId")
    @PatchMapping("/{businessEmployeeId}/permissions")
    public ResponseEntity<BusinessEmployeeDTO> updateBusinessEmployeePermissions(
        @PathVariable Long businessEmployeeId,
        @RequestBody Set<BusinessPermission> permissions
    ) {
        if (businessEmployeeId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return ResponseEntity.status(HttpStatus.OK).body(businessEmployeeService.updatePermissions(businessEmployeeId, permissions));
    }

    //return all the public employees
    @GetMapping("/public/business/{businessId}")
    public ResponseEntity<List<BusinessEmployeeDTO>> getPublicEmployees(@PathVariable Long businessId) {
        if (businessId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok(businessEmployeeService.findAllPublicByBusinessId(businessId));
    }
}
