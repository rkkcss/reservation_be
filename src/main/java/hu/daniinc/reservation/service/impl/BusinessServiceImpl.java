package hu.daniinc.reservation.service.impl;

import hu.daniinc.reservation.domain.Business;
import hu.daniinc.reservation.domain.BusinessEmployee;
import hu.daniinc.reservation.domain.User;
import hu.daniinc.reservation.domain.enumeration.BusinessTheme;
import hu.daniinc.reservation.domain.enumeration.OnboardingSteps;
import hu.daniinc.reservation.repository.BusinessEmployeeRepository;
import hu.daniinc.reservation.repository.BusinessRepository;
import hu.daniinc.reservation.service.BusinessService;
import hu.daniinc.reservation.service.CloudinaryImageUploader;
import hu.daniinc.reservation.service.UserService;
import hu.daniinc.reservation.service.dto.BusinessDTO;
import hu.daniinc.reservation.service.dto.OnboardingCompleteDTO;
import hu.daniinc.reservation.service.dto.SlugCheckResponseDTO;
import hu.daniinc.reservation.service.mapper.BusinessMapper;
import hu.daniinc.reservation.web.rest.errors.GeneralException;
import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service Implementation for managing {@link hu.daniinc.reservation.domain.Business}.
 */
@Service
@Transactional
public class BusinessServiceImpl implements BusinessService {

    private static final Logger LOG = LoggerFactory.getLogger(BusinessServiceImpl.class);

    private final BusinessRepository businessRepository;

    private final BusinessMapper businessMapper;

    private static final Set<String> RESERVED_SLUGS = Set.of(
        "admin",
        "administrator",
        "api",
        "app",
        "auth",
        "login",
        "register",
        "billing",
        "checkout",
        "help",
        "support",
        "test",
        "demo",
        "dev",
        "stage",
        "staging",
        "mail",
        "email",
        "smtp",
        "pop",
        "ftp",
        "www",
        "web",
        "blog",
        "status",
        "dashboard",
        "portal",
        "system",
        "booklyzz",
        "root",
        "user",
        "users",
        "account",
        "profile",
        "setting",
        "settings",
        "privacy",
        "terms",
        "about",
        "contact",
        "pricing",
        "pay",
        "payment"
    );
    private final UserService userService;
    private final BusinessEmployeeRepository businessEmployeeRepository;
    private final CloudinaryImageUploader cloudinaryImageUploader;

    public BusinessServiceImpl(
        BusinessRepository businessRepository,
        BusinessMapper businessMapper,
        UserService userService,
        BusinessEmployeeRepository businessEmployeeRepository,
        CloudinaryImageUploader cloudinaryImageUploader
    ) {
        this.businessRepository = businessRepository;
        this.businessMapper = businessMapper;
        this.userService = userService;
        this.businessEmployeeRepository = businessEmployeeRepository;
        this.cloudinaryImageUploader = cloudinaryImageUploader;
    }

    @Override
    public BusinessDTO save(BusinessDTO businessDTO) {
        LOG.debug("Request to save Business : {}", businessDTO);
        Business business = businessMapper.toEntity(businessDTO);
        business = businessRepository.save(business);
        return businessMapper.toDto(business);
    }

    @Override
    public BusinessDTO update(BusinessDTO businessDTO) {
        LOG.debug("Request to update Business : {}", businessDTO);
        Business business = businessMapper.toEntity(businessDTO);
        business = businessRepository.save(business);
        return businessMapper.toDto(business);
    }

    @Override
    public Optional<BusinessDTO> partialUpdate(BusinessDTO businessDTO) {
        LOG.debug("Request to partially update Business : {}", businessDTO);

        return businessRepository
            .findById(businessDTO.getId())
            .map(existingBusiness -> {
                businessMapper.partialUpdate(existingBusiness, businessDTO);

                return existingBusiness;
            })
            .map(businessRepository::save)
            .map(businessMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BusinessDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Businesses");
        return businessRepository.findAll(pageable).map(businessMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BusinessDTO> findOne(Long id) {
        LOG.debug("Request to get Business : {}", id);
        return businessRepository.findById(id).map(businessMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Business : {}", id);
        businessRepository.deleteById(id);
    }

    @Override
    public BusinessDTO getBusinessByLoggedInUser(Long businessId) {
        LOG.debug("Request to get Business By LoggedInUser");
        return businessRepository
            .findBusinessByLoginAndBusinessId(businessId)
            .map(businessMapper::toDto)
            .orElseThrow(() -> new RuntimeException("No Business Found"));
    }

    @Override
    @CacheEvict(value = { "businessBySlug", "businessByCustomDomain" }, allEntries = true)
    public void changeBusinessLogo(String newLogo) {
        LOG.debug("Request to change Business Logo");

        Business business = businessRepository
            .findBusinessByLoginAndBusinessId(1L)
            .orElseThrow(() -> new EntityNotFoundException("No Business Found"));
        businessRepository.save(business);
    }

    @Override
    @Transactional
    @CacheEvict(value = { "businessBySlug", "businessByCustomDomain" }, allEntries = true)
    public void changeBusinessThemeById(Long businessId, BusinessTheme theme) {
        LOG.debug("Request to change Business Theme");

        Business business = businessRepository
            .findBusinessByLoginAndBusinessId(businessId)
            .orElseThrow(() -> new EntityNotFoundException("No Business Found"));

        business.setTheme(theme);
        businessRepository.save(business);
    }

    @Override
    @Cacheable(value = "businessBySlug", key = "#slug.toLowerCase()", unless = "#result == null")
    public BusinessDTO findBySlug(String slug) {
        Business result = businessRepository.findBySlugIgnoreCase(slug).orElseThrow(() -> new EntityNotFoundException("No Business Found"));
        return businessMapper.toDto(result);
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessDTO findByCustomDomain(String domain) {
        Business result = businessRepository
            .findByCustomDomainIgnoreCase(domain)
            .orElseThrow(() -> new EntityNotFoundException("No Business Found"));
        return businessMapper.toDto(result);
    }

    @Override
    public SlugCheckResponseDTO checkSlugAvailability(String rawSlug) {
        if (rawSlug == null || rawSlug.trim().isEmpty()) {
            return new SlugCheckResponseDTO(false, List.of());
        }

        String slug = rawSlug.trim().toLowerCase();

        // 1. Ha a slug teljesen szabad (nem tiltott ÉS nem foglalt a DB-ben)
        if (isSlugAvailable(slug)) {
            return new SlugCheckResponseDTO(true, List.of());
        }

        // 2. Ha nem szabad, legeneráljuk az alternatívákat
        List<String> suggestions = generateSlugSuggestions(slug);

        return new SlugCheckResponseDTO(false, suggestions);
    }

    @Override
    @Transactional
    public BusinessDTO uploadCoverImage(MultipartFile file, Long businessId) throws IOException {
        var business = businessRepository
            .findBusinessByLoginAndBusinessId(businessId)
            .orElseThrow(() -> new GeneralException("business-not-found", "business-not-found", HttpStatus.NOT_FOUND));

        var result = cloudinaryImageUploader.uploadAndReplace(
            file,
            "business/" + business.getId() + "/banner",
            business.getBannerPublicId()
        );

        business.setBannerPublicId(result.publicId());
        business.setBannerUrl(result.url());

        return businessMapper.toDto(businessRepository.save(business));
    }

    /**
     * Csekkolja, hogy a slug használható-e.
     */
    private boolean isSlugAvailable(String slug) {
        if (RESERVED_SLUGS.contains(slug)) {
            return false;
        }
        return !businessRepository.existsBySlug(slug);
    }

    private List<String> generateSlugSuggestions(String baseSlug) {
        List<String> suggestions = new ArrayList<>();

        // Számozott alternatívák (pl. test-1, komoly-barber-1)
        int counter = 1;
        while (suggestions.size() < 2 && counter <= 99) {
            String candidate = baseSlug + "-" + counter;
            if (isSlugAvailable(candidate)) {
                suggestions.add(candidate);
            }
            counter++;
        }

        // Gyakori utótagok (pl. test-app, komoly-barber-hu)
        List<String> suffixes = List.of("app", "hu", "official");
        for (String suffix : suffixes) {
            if (suggestions.size() >= 4) break;

            String candidate = baseSlug + "-" + suffix;
            if (isSlugAvailable(candidate)) {
                suggestions.add(candidate);
            }
        }

        return suggestions;
    }
}
