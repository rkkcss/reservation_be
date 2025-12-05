package hu.daniinc.reservation.service.impl;

import hu.daniinc.reservation.domain.Business;
import hu.daniinc.reservation.domain.BusinessEmployee;
import hu.daniinc.reservation.domain.BusinessEmployeeInvite;
import hu.daniinc.reservation.domain.User;
import hu.daniinc.reservation.domain.enumeration.BasicEntityStatus;
import hu.daniinc.reservation.repository.BusinessEmployeeInviteRepository;
import hu.daniinc.reservation.repository.BusinessEmployeeRepository;
import hu.daniinc.reservation.repository.BusinessRepository;
import hu.daniinc.reservation.repository.UserRepository;
import hu.daniinc.reservation.service.BusinessEmployeeInviteService;
import hu.daniinc.reservation.service.MailService;
import hu.daniinc.reservation.service.UserService;
import hu.daniinc.reservation.service.dto.BusinessEmployeeActivateDTO;
import hu.daniinc.reservation.service.dto.BusinessEmployeeInviteDTO;
import hu.daniinc.reservation.service.mapper.BusinessEmployeeInviteMapper;
import hu.daniinc.reservation.service.mapper.BusinessEmployeeMapperImpl;
import hu.daniinc.reservation.web.rest.errors.BadRequestAlertException;
import hu.daniinc.reservation.web.rest.errors.GeneralException;
import hu.daniinc.reservation.web.rest.vm.ManagedUserVM;
import jakarta.servlet.http.HttpServletRequest;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.UUID;
import org.hibernate.SessionException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BusinessEmployeeInviteImpl implements BusinessEmployeeInviteService {

    private static final String ENTITY_NAME = "businessEmployeeInvite";

    private final BusinessEmployeeInviteRepository businessEmployeeInviteRepository;
    private final BusinessRepository businessRepository;
    private final UserService userService;
    private final BusinessEmployeeMapperImpl businessEmployeeMapperImpl;
    private final BusinessEmployeeInviteMapper businessEmployeeInviteMapper;
    private final MailService mailService;
    private final UserRepository userRepository;
    private final BusinessEmployeeRepository businessEmployeeRepository;

    public BusinessEmployeeInviteImpl(
        BusinessEmployeeInviteRepository businessEmployeeInviteRepository,
        BusinessRepository businessRepository,
        UserService userService,
        BusinessEmployeeMapperImpl businessEmployeeMapperImpl,
        BusinessEmployeeInviteMapper businessEmployeeInviteMapper,
        MailService mailService,
        UserRepository userRepository,
        BusinessEmployeeRepository businessEmployeeRepository
    ) {
        this.businessEmployeeInviteRepository = businessEmployeeInviteRepository;
        this.businessRepository = businessRepository;
        this.userService = userService;
        this.businessEmployeeMapperImpl = businessEmployeeMapperImpl;
        this.businessEmployeeInviteMapper = businessEmployeeInviteMapper;
        this.mailService = mailService;
        this.userRepository = userRepository;
        this.businessEmployeeRepository = businessEmployeeRepository;
    }

    @Override
    @Transactional
    public BusinessEmployeeInviteDTO inviteEmployee(Long businessId, BusinessEmployeeInviteDTO dto) {
        if (businessEmployeeInviteRepository.existsByEmailAndBusinessIdAndUsedFalse(dto.getEmail(), businessId)) {
            throw new BadRequestAlertException("Invitation exists!", ENTITY_NAME, "invalidBusinessEmployeeInvite");
        }

        Business business = businessRepository.findById(businessId).orElseThrow(() -> new IllegalArgumentException("Business not found"));

        User currentUser = userService.getUserWithAuthorities().orElseThrow(() -> new SessionException("User not found"));

        // create entity
        BusinessEmployeeInvite invite = new BusinessEmployeeInvite();
        invite.setBusiness(business);
        invite.setUsed(false);
        invite.setEmail(dto.getEmail());
        invite.setInvitedBy(currentUser);
        invite.setPermissions(dto.getPermissions());
        invite.setRole(dto.getRole());
        invite.setToken(UUID.randomUUID().toString());
        invite.setExpiresAt(dto.getExpiresAt() != null ? dto.getExpiresAt() : ZonedDateTime.now().plusDays(7));

        BusinessEmployeeInvite result = businessEmployeeInviteRepository.save(invite);

        // send email
        mailService.sendEmail(
            dto.getEmail(),
            "Felkérés csatlakozáshoz",
            "http://localhost:5173/employee-invite/" + invite.getToken(),
            false,
            false
        );

        return businessEmployeeInviteMapper.toDto(result);
    }

    @Override
    @Transactional
    public Page<BusinessEmployeeInviteDTO> getAllBusinessEmployeeInvitePendingPagination(Long businessId, Pageable pageable) {
        return businessEmployeeInviteRepository
            .findAllByPendingPagination(businessId, pageable)
            .map(businessEmployeeInviteMapper::toDtoWithoutToken);
    }

    @Override
    @Transactional
    public BusinessEmployeeActivateDTO getBusinessEmployeeByToken(String token) {
        BusinessEmployeeInvite invite = businessEmployeeInviteRepository
            .findOneByToken(token)
            .orElseThrow(() -> new GeneralException("Invalid token", "invalid-token", HttpStatus.BAD_REQUEST));

        if (invite.isUsed()) {
            throw new GeneralException("Token already used", "used-token", HttpStatus.CONFLICT);
        }

        if (invite.getExpiresAt().isBefore(ZonedDateTime.now())) {
            throw new GeneralException("Token expired", "expired-token", HttpStatus.GONE);
        }
        boolean userExists = userRepository.findOneByEmailIgnoreCase(invite.getEmail()).isPresent();

        BusinessEmployeeActivateDTO result = new BusinessEmployeeActivateDTO();
        result.setBusinessEmployeeInvite(businessEmployeeInviteMapper.toDtoWithoutToken(invite));
        result.setUserExists(userExists);

        return result;
    }

    @Override
    @Transactional
    public void activateBusinessEmployeeWithToken(String token, ManagedUserVM managedUserVM, HttpServletRequest request) {
        BusinessEmployeeInvite businessEmployeeInvite = businessEmployeeInviteRepository
            .findByToken(token)
            .orElseThrow(() -> new GeneralException("Can't find businessEmployee by token", "cant-find", HttpStatus.BAD_REQUEST));

        if (businessEmployeeInvite.isUsed()) {
            throw new GeneralException("Token already used", "used-token", HttpStatus.CONFLICT);
        }

        if (!businessEmployeeInvite.getEmail().equalsIgnoreCase(managedUserVM.getEmail())) {
            throw new GeneralException("Invalid email", "invitation-not-yours", HttpStatus.BAD_REQUEST);
        }
        //register user
        userService.registerWithInvitation(managedUserVM, managedUserVM.getPassword(), token, request);

        businessEmployeeInvite.setUsed(true);
        businessEmployeeInviteRepository.save(businessEmployeeInvite);
    }

    @Transactional
    public void activateAlreadyRegisteredBusinessEmployeeWithToken(String token) {
        BusinessEmployeeInvite invite = validateToken(token);

        User user = getUserByEmail(invite.getEmail());

        BusinessEmployee newEmployee = createBusinessEmployee(user, invite);

        businessEmployeeRepository.save(newEmployee);
    }

    private BusinessEmployeeInvite validateToken(String token) {
        BusinessEmployeeInvite invite = businessEmployeeInviteRepository
            .findByToken(token)
            .orElseThrow(() -> new GeneralException("Invalid token", "cant-find", HttpStatus.BAD_REQUEST));

        if (invite.isUsed() || invite.getExpiresAt().isBefore(ZonedDateTime.now())) {
            throw new GeneralException("Token already used or expired", "used-token-or-expired", HttpStatus.CONFLICT);
        }

        return invite;
    }

    private User getUserByEmail(String email) {
        return userRepository
            .findOneByEmailIgnoreCase(email)
            .orElseThrow(() -> new GeneralException("Invalid email", "cant-find", HttpStatus.BAD_REQUEST));
    }

    private BusinessEmployee createBusinessEmployee(User user, BusinessEmployeeInvite invite) {
        BusinessEmployee employee = new BusinessEmployee();
        employee.setUser(user);
        employee.setStatus(BasicEntityStatus.ACTIVE);
        employee.setRole(invite.getRole());
        employee.setPermissions(new HashSet<>(invite.getPermissions()));
        employee.setBusiness(invite.getBusiness());
        return employee;
    }
}
