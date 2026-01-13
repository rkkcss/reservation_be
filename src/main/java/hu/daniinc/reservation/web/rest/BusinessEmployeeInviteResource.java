package hu.daniinc.reservation.web.rest;

import hu.daniinc.reservation.domain.BusinessEmployeeInvite;
import hu.daniinc.reservation.domain.enumeration.BusinessPermission;
import hu.daniinc.reservation.security.annotation.RequiredBusinessPermission;
import hu.daniinc.reservation.service.BusinessEmployeeInviteService;
import hu.daniinc.reservation.service.dto.BusinessEmployeeActivateDTO;
import hu.daniinc.reservation.service.dto.BusinessEmployeeInviteDTO;
import hu.daniinc.reservation.service.dto.OfferingDTO;
import hu.daniinc.reservation.service.impl.BusinessEmployeeInviteImpl;
import hu.daniinc.reservation.web.rest.vm.ManagedUserVM;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;

@RestController
@RequestMapping("/api/employee-invite")
public class BusinessEmployeeInviteResource {

    private final BusinessEmployeeInviteService businessEmployeeInviteService;

    public BusinessEmployeeInviteResource(BusinessEmployeeInviteService businessEmployeeInviteService) {
        this.businessEmployeeInviteService = businessEmployeeInviteService;
    }

    @PostMapping("/{businessId}")
    @RequiredBusinessPermission(BusinessPermission.MANAGE_EMPLOYEES)
    public ResponseEntity<BusinessEmployeeInviteDTO> createBusinessEmployeeInvite(
        @PathVariable(value = "businessId") Long businessId,
        @Valid @RequestBody final BusinessEmployeeInviteDTO businessEmployeeInviteDTO
    ) {
        if (businessId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(
            businessEmployeeInviteService.inviteEmployee(businessId, businessEmployeeInviteDTO)
        );
    }

    @GetMapping("/business/{businessId}/pending")
    @RequiredBusinessPermission(BusinessPermission.MANAGE_EMPLOYEES)
    public ResponseEntity<List<BusinessEmployeeInviteDTO>> getPendingBusinessEmployeeInvites(
        @PathVariable(value = "businessId") Long businessId,
        Pageable pageable
    ) {
        if (businessId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        Page<BusinessEmployeeInviteDTO> page = businessEmployeeInviteService.getAllBusinessEmployeeInvitePendingPagination(
            businessId,
            pageable
        );
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/activate")
    public ResponseEntity<BusinessEmployeeActivateDTO> getBusinessEmployeeInviteByToken(@RequestParam(value = "token") String token) {
        if (token == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.status(HttpStatus.OK).body(businessEmployeeInviteService.getBusinessEmployeeByToken(token));
    }

    @PostMapping("/activate")
    public ResponseEntity<Void> activateBusinessEmployeeInvite(
        @RequestParam("token") String token,
        @Valid @RequestBody(required = false) ManagedUserVM managedUserVM,
        HttpServletRequest request
    ) {
        if (managedUserVM == null) {
            businessEmployeeInviteService.activateAlreadyRegisteredBusinessEmployeeWithToken(token);
        } else {
            businessEmployeeInviteService.activateBusinessEmployeeWithToken(token, managedUserVM, request);
        }

        return ResponseEntity.noContent().build();
    }
}
