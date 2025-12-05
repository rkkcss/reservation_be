package hu.daniinc.reservation.service;

import hu.daniinc.reservation.service.dto.BusinessEmployeeActivateDTO;
import hu.daniinc.reservation.service.dto.BusinessEmployeeInviteDTO;
import hu.daniinc.reservation.web.rest.vm.ManagedUserVM;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

public interface BusinessEmployeeInviteService {
    BusinessEmployeeInviteDTO inviteEmployee(Long businessId, BusinessEmployeeInviteDTO businessEmployeeInviteDTO);

    Page<BusinessEmployeeInviteDTO> getAllBusinessEmployeeInvitePendingPagination(Long businessId, Pageable pageable);

    BusinessEmployeeActivateDTO getBusinessEmployeeByToken(String token);

    void activateBusinessEmployeeWithToken(String token, ManagedUserVM managedUserVM, HttpServletRequest request);

    void activateAlreadyRegisteredBusinessEmployeeWithToken(String token);
}
