package hu.daniinc.reservation.service;

import hu.daniinc.reservation.domain.enumeration.BusinessPermission;
import hu.daniinc.reservation.service.dto.BusinessEmployeeDTO;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BusinessEmployeeService {
    BusinessEmployeeDTO createBusinessEmployeeByBusinessId(Long businessId, BusinessEmployeeDTO businessEmployeeDTO);

    Set<BusinessEmployeeDTO> findAllByLoggedInUser();

    Page<BusinessEmployeeDTO> findAllByBusinessId(Long businessId, Pageable pageable);

    BusinessEmployeeDTO findByBusinessIdAndUserId(Long businessId, Long userId);

    BusinessEmployeeDTO updatePermissions(Long businessEmployeeId, Set<BusinessPermission> permissions);

    List<BusinessEmployeeDTO> findAllPublicByBusinessId(Long businessId);
}
