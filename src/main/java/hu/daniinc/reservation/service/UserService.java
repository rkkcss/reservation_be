package hu.daniinc.reservation.service;

import hu.daniinc.reservation.config.Constants;
import hu.daniinc.reservation.domain.*;
import hu.daniinc.reservation.domain.enumeration.BasicEntityStatus;
import hu.daniinc.reservation.domain.enumeration.BusinessRole;
import hu.daniinc.reservation.domain.enumeration.BusinessTheme;
import hu.daniinc.reservation.repository.*;
import hu.daniinc.reservation.security.AuthoritiesConstants;
import hu.daniinc.reservation.security.DomainUserDetailsService;
import hu.daniinc.reservation.security.SecurityUtils;
import hu.daniinc.reservation.service.dto.AdminUserDTO;
import hu.daniinc.reservation.service.dto.UserDTO;
import hu.daniinc.reservation.web.rest.errors.GeneralException;
import hu.daniinc.reservation.web.rest.vm.ManagedUserVM;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tech.jhipster.security.RandomUtil;

/**
 * Service class for managing users.
 */
@Service
@Transactional
public class UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final PersistentTokenRepository persistentTokenRepository;

    private final AuthorityRepository authorityRepository;

    private final CacheManager cacheManager;
    private final BusinessRepository businessRepository;
    private final BusinessEmployeeInviteRepository businessEmployeeInviteRepository;
    private final BusinessEmployeeRepository businessEmployeeRepository;
    private final DomainUserDetailsService userDetailsService;

    @Value("${app.onboarding-version}")
    private Integer onboardingVersion;

    public UserService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        PersistentTokenRepository persistentTokenRepository,
        AuthorityRepository authorityRepository,
        CacheManager cacheManager,
        BusinessRepository businessRepository,
        BusinessEmployeeInviteRepository businessEmployeeInviteRepository,
        BusinessEmployeeRepository businessEmployeeRepository,
        DomainUserDetailsService userDetailsService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.persistentTokenRepository = persistentTokenRepository;
        this.authorityRepository = authorityRepository;
        this.cacheManager = cacheManager;
        this.businessRepository = businessRepository;
        this.businessEmployeeInviteRepository = businessEmployeeInviteRepository;
        this.businessEmployeeRepository = businessEmployeeRepository;
        this.userDetailsService = userDetailsService;
    }

    @Transactional
    public Optional<User> activateRegistration(String key) {
        LOG.debug("Activating user for activation key {}", key);
        return userRepository
            .findOneByActivationKey(key)
            .map(user -> {
                // activate given user for the registration key.
                user.setActivated(true);
                user.setActivationKey(null);

                Business business = new Business();
                business.setName(user.getFirstName() + "'s Business");
                business.setOwner(user);
                business.setAddress(user.getFirstName() + "'s Address");
                business.setMaxWeeksInAdvance(0);
                business.setTheme(BusinessTheme.DEFAULT);
                business.setAppointmentApprovalRequired(Boolean.FALSE);
                businessRepository.save(business);

                BusinessEmployee be = BusinessEmployee.owner(business, user);
                businessEmployeeRepository.save(be);

                this.clearUserCaches(user);
                LOG.debug("Activated user: {}", user);
                return user;
            });
    }

    public Optional<User> completePasswordReset(String newPassword, String key) {
        LOG.debug("Reset user password for reset key {}", key);
        return userRepository
            .findOneByResetKey(key)
            .filter(user -> user.getResetDate().isAfter(Instant.now().minus(1, ChronoUnit.DAYS)))
            .map(user -> {
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setResetKey(null);
                user.setResetDate(null);
                this.clearUserCaches(user);
                return user;
            });
    }

    public Optional<User> requestPasswordReset(String mail) {
        return userRepository
            .findOneByEmailIgnoreCase(mail)
            .filter(User::isActivated)
            .map(user -> {
                user.setResetKey(RandomUtil.generateResetKey());
                user.setResetDate(Instant.now());
                this.clearUserCaches(user);
                return user;
            });
    }

    @Transactional
    public User registerUser(AdminUserDTO userDTO, String password) {
        Optional<User> existingUserByLogin = userRepository.findOneByLogin(userDTO.getLogin().toLowerCase());
        existingUserByLogin.ifPresent(user -> {
            boolean removed = removeNonActivatedUser(user);
            if (!removed) {
                throw new UsernameAlreadyUsedException();
            }
        });

        Optional<User> existingUserByEmail = userRepository.findOneByEmailIgnoreCase(userDTO.getEmail());
        existingUserByEmail.ifPresent(user -> {
            boolean removed = removeNonActivatedUser(user);
            if (!removed) {
                throw new EmailAlreadyUsedException();
            }
        });

        User newUser = new User();
        String encryptedPassword = passwordEncoder.encode(password);
        newUser.setLogin(userDTO.getLogin().toLowerCase());
        newUser.setPassword(encryptedPassword);
        newUser.setFirstName(userDTO.getFirstName());
        newUser.setLastName(userDTO.getLastName());
        newUser.setEmail(userDTO.getEmail().toLowerCase());
        newUser.setImageUrl(userDTO.getImageUrl());
        newUser.setLangKey(userDTO.getLangKey());
        newUser.setActivated(false);
        newUser.setActivationKey(RandomUtil.generateActivationKey());

        // Authorities
        Set<Authority> authorities = new HashSet<>();
        authorityRepository.findById(AuthoritiesConstants.USER).ifPresent(authorities::add);
        newUser.setAuthorities(authorities);

        userRepository.save(newUser);

        this.clearUserCaches(newUser);
        LOG.debug("Created Information for User: {}", newUser);
        return newUser;
    }

    @Transactional
    public User registerWithInvitation(AdminUserDTO userDTO, String password, String token, HttpServletRequest request) {
        userRepository
            .findOneByLogin(userDTO.getLogin().toLowerCase())
            .ifPresent(existingUser -> {
                boolean removed = removeNonActivatedUser(existingUser);
                if (!removed) {
                    throw new UsernameAlreadyUsedException();
                }
            });
        userRepository
            .findOneByEmailIgnoreCase(userDTO.getEmail())
            .ifPresent(existingUser -> {
                boolean removed = removeNonActivatedUser(existingUser);
                if (!removed) {
                    throw new EmailAlreadyUsedException();
                }
            });
        User newUser = new User();
        String encryptedPassword = passwordEncoder.encode(password);
        newUser.setLogin(userDTO.getLogin().toLowerCase());
        newUser.setPassword(encryptedPassword);
        newUser.setFirstName(userDTO.getFirstName());
        newUser.setLastName(userDTO.getLastName());
        newUser.setEmail(userDTO.getEmail().toLowerCase());
        newUser.setImageUrl(userDTO.getImageUrl());
        newUser.setLangKey(userDTO.getLangKey());
        //because register through e-mail link
        newUser.setActivated(true);

        Set<Authority> authorities = new HashSet<>();
        authorityRepository.findById(AuthoritiesConstants.USER).ifPresent(authorities::add);
        newUser.setAuthorities(authorities);

        userRepository.save(newUser);

        //Assign to Business
        BusinessEmployeeInvite businessEmployeeInvite = businessEmployeeInviteRepository
            .findByToken(token)
            .orElseThrow(() -> new GeneralException("BusinessEmployeeInvite is not found!", "entity-not-found", HttpStatus.NOT_FOUND));

        //Create new businessEmployee and assign it to the business
        BusinessEmployee businessEmployee = new BusinessEmployee();
        businessEmployee.setPermissions(new HashSet<>(businessEmployeeInvite.getPermissions()));
        businessEmployee.setBusiness(businessEmployeeInvite.getBusiness());
        businessEmployee.setUser(newUser);
        businessEmployee.setRole(businessEmployeeInvite.getRole());
        businessEmployee.setStatus(BasicEntityStatus.ACTIVE);
        businessEmployeeRepository.save(businessEmployee);

        this.clearUserCaches(newUser);
        LOG.debug("Creating user by invitation: {}", newUser);

        UserDetails userDetails = userDetailsService.loadUserByUsername(newUser.getLogin());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities()
        );

        // Bind the authentication to the current session
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Force session creation
        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

        return newUser;
    }

    private boolean removeNonActivatedUser(User existingUser) {
        if (existingUser.isActivated()) {
            return false;
        }
        userRepository.delete(existingUser);
        userRepository.flush();
        this.clearUserCaches(existingUser);
        return true;
    }

    public User createUser(AdminUserDTO userDTO) {
        User user = new User();
        user.setLogin(userDTO.getLogin().toLowerCase());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail().toLowerCase());
        }
        user.setImageUrl(userDTO.getImageUrl());
        if (userDTO.getLangKey() == null) {
            user.setLangKey(Constants.DEFAULT_LANGUAGE); // default language
        } else {
            user.setLangKey(userDTO.getLangKey());
        }
        String encryptedPassword = passwordEncoder.encode(RandomUtil.generatePassword());
        user.setPassword(encryptedPassword);
        user.setResetKey(RandomUtil.generateResetKey());
        user.setResetDate(Instant.now());
        user.setActivated(true);
        if (userDTO.getAuthorities() != null) {
            Set<Authority> authorities = userDTO
                .getAuthorities()
                .stream()
                .map(authorityRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
            user.setAuthorities(authorities);
        }
        userRepository.save(user);
        this.clearUserCaches(user);
        LOG.debug("Created Information for User: {}", user);
        return user;
    }

    /**
     * Update all information for a specific user, and return the modified user.
     *
     * @param userDTO user to update.
     * @return updated user.
     */
    public Optional<AdminUserDTO> updateUser(AdminUserDTO userDTO) {
        return Optional.of(userRepository.findById(userDTO.getId()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(user -> {
                this.clearUserCaches(user);
                user.setLogin(userDTO.getLogin().toLowerCase());
                user.setFirstName(userDTO.getFirstName());
                user.setLastName(userDTO.getLastName());
                if (userDTO.getEmail() != null) {
                    user.setEmail(userDTO.getEmail().toLowerCase());
                }
                user.setImageUrl(userDTO.getImageUrl());
                user.setActivated(userDTO.isActivated());
                user.setLangKey(userDTO.getLangKey());
                Set<Authority> managedAuthorities = user.getAuthorities();
                managedAuthorities.clear();
                userDTO
                    .getAuthorities()
                    .stream()
                    .map(authorityRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .forEach(managedAuthorities::add);
                userRepository.save(user);
                this.clearUserCaches(user);
                LOG.debug("Changed Information for User: {}", user);
                return user;
            })
            .map(AdminUserDTO::new);
    }

    public void deleteUser(String login) {
        userRepository
            .findOneByLogin(login)
            .ifPresent(user -> {
                userRepository.delete(user);
                this.clearUserCaches(user);
                LOG.debug("Deleted User: {}", user);
            });
    }

    /**
     * Update basic information (first name, last name, email, language) for the current user.
     *
     * @param firstName first name of user.
     * @param lastName  last name of user.
     * @param email     email id of user.
     * @param langKey   language key.
     * @param imageUrl  image URL of user.
     */
    public void updateUser(String firstName, String lastName, String email, String langKey, String imageUrl) {
        SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(user -> {
                user.setFirstName(firstName);
                user.setLastName(lastName);
                if (email != null) {
                    user.setEmail(email.toLowerCase());
                }
                user.setLangKey(langKey);
                user.setImageUrl(imageUrl);
                userRepository.save(user);
                this.clearUserCaches(user);
                LOG.debug("Changed Information for User: {}", user);
            });
    }

    @Transactional
    public void changePassword(String currentClearTextPassword, String newPassword) {
        SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(user -> {
                String currentEncryptedPassword = user.getPassword();
                if (!passwordEncoder.matches(currentClearTextPassword, currentEncryptedPassword)) {
                    throw new InvalidPasswordException();
                }
                String encryptedPassword = passwordEncoder.encode(newPassword);
                user.setPassword(encryptedPassword);
                this.clearUserCaches(user);
                LOG.debug("Changed password for User: {}", user);
            });
    }

    @Transactional(readOnly = true)
    public Page<AdminUserDTO> getAllManagedUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(AdminUserDTO::new);
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getAllPublicUsers(Pageable pageable) {
        return userRepository.findAllByIdNotNullAndActivatedIsTrue(pageable).map(UserDTO::new);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthoritiesByLogin(String login) {
        return userRepository.findOneWithAuthoritiesByLogin(login);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthorities() {
        return SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneWithAuthoritiesByLogin);
    }

    /**
     * Persistent Token are used for providing automatic authentication, they should be automatically deleted after
     * 30 days.
     * <p>
     * This is scheduled to get fired every day, at midnight.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void removeOldPersistentTokens() {
        LocalDate now = LocalDate.now();
        persistentTokenRepository
            .findByTokenDateBefore(now.minusMonths(1))
            .forEach(token -> {
                LOG.debug("Deleting token {}", token.getSeries());
                User user = token.getUser();
                user.getPersistentTokens().remove(token);
                persistentTokenRepository.delete(token);
            });
    }

    /**
     * Not activated users should be automatically deleted after 3 days.
     * <p>
     * This is scheduled to get fired every day, at 01:00 (am).
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void removeNotActivatedUsers() {
        userRepository
            .findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(Instant.now().minus(3, ChronoUnit.DAYS))
            .forEach(user -> {
                LOG.debug("Deleting not activated user {}", user.getLogin());
                userRepository.delete(user);
                this.clearUserCaches(user);
            });
    }

    /**
     * Gets a list of all the authorities.
     * @return a list of all the authorities.
     */
    @Transactional(readOnly = true)
    public List<String> getAuthorities() {
        return authorityRepository.findAll().stream().map(Authority::getName).toList();
    }

    private void clearUserCaches(User user) {
        Objects.requireNonNull(cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE)).evictIfPresent(user.getLogin());
        if (user.getEmail() != null) {
            Objects.requireNonNull(cacheManager.getCache(UserRepository.USERS_BY_EMAIL_CACHE)).evictIfPresent(user.getEmail());
        }
    }

    public void changeName(UserDTO userDTO) {
        User loggedInUser = this.getUserWithAuthorities().orElseThrow(() -> new RuntimeException("User not found!"));
        if (userDTO.getFirstName() == null && userDTO.getFirstName().length() < 2) {
            throw new RuntimeException("First name cannot less than 2 characters or null!");
        }

        if (userDTO.getLastName() == null && userDTO.getLastName().length() < 2) {
            throw new RuntimeException("Last name cannot less than 2 characters or null!");
        }
        loggedInUser.setLastName(userDTO.getLastName());
        loggedInUser.setFirstName(userDTO.getFirstName());
        userRepository.save(loggedInUser);
    }

    @Transactional
    public void changeLogin(String login) {
        if (login == null || login.length() < 2 || login.length() > 50) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Login must be between 2 and 50 characters!");
        }

        User loggedInUser =
            this.getUserWithAuthorities().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));

        if (userRepository.existsByLogin(login) && !loggedInUser.getLogin().equals(login)) {
            throw new RuntimeException("This login name is already taken!");
        }

        loggedInUser.setLogin(login);
        userRepository.save(loggedInUser);
    }

    @Transactional
    public void increaseOnboardingVersion() {
        User user =
            this.getUserWithAuthorities()
                .orElseThrow(() -> new GeneralException("Logged in user not found", "user-not-found", HttpStatus.NOT_FOUND));
        int current = user.getOnboardingVersion() != null ? user.getOnboardingVersion() : 0;

        if (current < onboardingVersion) {
            user.setOnboardingVersion(current + 1);
            userRepository.save(user);
            LOG.debug("Onboarding növelve: {} -> {} a felhasználónál: {}", current, current + 1, user.getLogin());
        } else {
            LOG.debug("A felhasználó ({}) már elérte a maximális onboarding verziót: {}", user.getLogin(), onboardingVersion);
        }
    }
}
