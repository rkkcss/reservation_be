package hu.daniinc.reservation.service.impl;

import hu.daniinc.reservation.domain.BusinessEmployee;
import hu.daniinc.reservation.domain.User;
import hu.daniinc.reservation.domain.enumeration.BusinessPermission;
import hu.daniinc.reservation.domain.enumeration.BusinessRole;
import hu.daniinc.reservation.repository.BusinessEmployeeRepository;
import hu.daniinc.reservation.repository.BusinessRepository;
import hu.daniinc.reservation.service.BusinessEmployeeService;
import hu.daniinc.reservation.service.UserService;
import hu.daniinc.reservation.service.dto.BusinessEmployeeDTO;
import hu.daniinc.reservation.service.mapper.BusinessEmployeeMapper;
import hu.daniinc.reservation.service.mapper.UserMapper;
import hu.daniinc.reservation.web.rest.errors.BadRequestAlertException;
import hu.daniinc.reservation.web.rest.errors.NotFoundException;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.hibernate.SessionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BusinessEmployeeServiceImpl implements BusinessEmployeeService {

    private static final Logger LOG = LoggerFactory.getLogger(BusinessEmployeeServiceImpl.class);
    private final UserService userService;
    private final BusinessRepository businessRepository;
    private final BusinessEmployeeRepository businessEmployeeRepository;
    private final BusinessEmployeeMapper businessEmployeeMapper;
    private final UserMapper userMapper;

    public BusinessEmployeeServiceImpl(
        UserService userService,
        BusinessRepository businessRepository,
        BusinessEmployeeRepository businessEmployeeRepository,
        BusinessEmployeeMapper businessEmployeeMapper,
        UserMapper userMapper
    ) {
        this.userService = userService;
        this.businessRepository = businessRepository;
        this.businessEmployeeRepository = businessEmployeeRepository;
        this.businessEmployeeMapper = businessEmployeeMapper;
        this.userMapper = userMapper;
    }

    @Override
    public BusinessEmployeeDTO createBusinessEmployeeByBusinessId(Long businessId, BusinessEmployeeDTO businessEmployeeDTO) {
        return null;
    }

    @Override
    public Set<BusinessEmployeeDTO> findAllByLoggedInUser() {
        return businessEmployeeRepository.findAllByUserLogin().stream().map(businessEmployeeMapper::toDto).collect(Collectors.toSet());
    }

    @Override
    public Page<BusinessEmployeeDTO> findAllByBusinessId(Long businessId, Pageable pageable) {
        return businessEmployeeRepository.findAllByBusinessId(businessId, pageable).map(businessEmployeeMapper::toDto);
    }

    @Override
    public BusinessEmployeeDTO findByBusinessIdAndUserId(Long businessId, Long userId) {
        BusinessEmployee result = businessEmployeeRepository
            .findByBusinessIdAndEmployeeId(businessId, userId)
            .orElseThrow(() -> new NotFoundException("Employee", userId));
        return businessEmployeeMapper.toDto(result);
    }

    @Override
    public BusinessEmployeeDTO updatePermissions(Long businessEmployeeId, Set<BusinessPermission> permissions) {
        BusinessEmployee businessEmployee = businessEmployeeRepository
            .findById(businessEmployeeId)
            .stream()
            .findFirst()
            .orElseThrow(() -> new EntityNotFoundException("Business employee not found"));
        businessEmployee.setPermissions(permissions);

        //check user is not a logged-in user
        User user = userService.getUserWithAuthorities().orElseThrow(() -> new SessionException("No user logged in"));

        if (Objects.equals(user.getLogin(), businessEmployee.getUser().getLogin())) {
            LOG.debug("Employee tried to edit it's own permissions {}", user.getLogin());
            throw new BadRequestAlertException("Can't edit your own permissions!", "businessEmployee", "cant-edit-own-permissions");
        }

        if (businessEmployee.getRole().equals(BusinessRole.OWNER)) {
            LOG.debug(
                "Employee tried to edit the OWNER permissions login: {}, business employee ID: {}",
                user.getLogin(),
                businessEmployee.getId()
            );
            throw new BadRequestAlertException("Can't edit owner permissions!", "businessEmployee", "cant-edit-owner-permissions");
        }

        return businessEmployeeMapper.toDto(businessEmployeeRepository.save(businessEmployee));
    }

    @Override
    public List<BusinessEmployeeDTO> findAllPublicByBusinessId(Long businessId) {
        return businessEmployeeRepository
            .findAllPublicByBusinessId(businessId)
            .stream()
            .map(businessEmployeeMapper::toDto)
            .collect(Collectors.toList());
    }
}
