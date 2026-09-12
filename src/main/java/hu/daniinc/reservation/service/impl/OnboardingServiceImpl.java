package hu.daniinc.reservation.service.impl;

import hu.daniinc.reservation.domain.Business;
import hu.daniinc.reservation.domain.BusinessEmployee;
import hu.daniinc.reservation.domain.User;
import hu.daniinc.reservation.domain.enumeration.BusinessTheme;
import hu.daniinc.reservation.domain.enumeration.OnboardingSteps;
import hu.daniinc.reservation.repository.BusinessEmployeeRepository;
import hu.daniinc.reservation.repository.BusinessRepository;
import hu.daniinc.reservation.service.*;
import hu.daniinc.reservation.service.dto.*;
import hu.daniinc.reservation.service.mapper.BusinessEmployeeMapper;
import hu.daniinc.reservation.service.mapper.BusinessMapper;
import hu.daniinc.reservation.web.rest.errors.GeneralException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OnboardingServiceImpl implements OnboardingService {

    private final BusinessRepository businessRepository;
    private final BusinessService businessService;
    private final UserService userService;
    private final BusinessEmployeeRepository businessEmployeeRepository;
    private final BusinessMapper businessMapper;
    private final WorkingHoursService workingHoursService;
    private final CloudinaryImageUploader cloudinaryImageUploader;
    private final BusinessEmployeeMapper businessEmployeeMapper;

    public OnboardingServiceImpl(
        BusinessRepository businessRepository,
        BusinessService businessService,
        UserService userService,
        BusinessEmployeeRepository businessEmployeeRepository,
        BusinessMapper businessMapper,
        WorkingHoursService workingHoursService,
        CloudinaryImageUploader cloudinaryImageUploader,
        BusinessEmployeeMapper businessEmployeeMapper
    ) {
        this.businessRepository = businessRepository;
        this.businessService = businessService;
        this.userService = userService;
        this.businessEmployeeRepository = businessEmployeeRepository;
        this.businessMapper = businessMapper;
        this.workingHoursService = workingHoursService;
        this.cloudinaryImageUploader = cloudinaryImageUploader;
        this.businessEmployeeMapper = businessEmployeeMapper;
    }

    @Override
    public OnboardingSteps getCurrentOnboardingStep(Long businessId) {
        if (businessId == null) {
            return OnboardingSteps.BUSINESS_DETAILS;
        }
        Business business = businessRepository
            .getByLoggedInUserAndBusinessId(businessId)
            .orElseThrow(() -> new EntityNotFoundException("No Business Found"));
        return business.getOnboardingStep();
    }

    @Override
    @Transactional
    public Map<String, Object> onboardingStepComplete(BusinessDTO businessDTO, Long businessId) {
        User user = userService.getUserWithAuthorities().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        var business = new Business();
        if (businessId != null) {
            business = businessRepository
                .getByLoggedInUserAndBusinessId(businessId)
                .orElseThrow(() -> new EntityNotFoundException("No Business Found"));
        }

        businessMapper.partialUpdate(business, businessDTO);
        business.setOnboardingStep(OnboardingSteps.BUSINESS_THEME);
        business.setOwner(user);

        Business savedBusiness = businessRepository.save(business);

        BusinessEmployee ownerEmployee = BusinessEmployee.owner(savedBusiness, user);
        businessEmployeeRepository.save(ownerEmployee);

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            request.setAttribute("tenantBusinessId", savedBusiness.getId());
        }

        return Map.of("business", businessMapper.toDto(savedBusiness), "businessEmployee", businessEmployeeMapper.toDto(ownerEmployee));
    }

    @Override
    @Transactional
    public void onBoardingThemeComplete(BusinessTheme businessTheme, Long businessId) {
        Business business = businessRepository
            .getByLoggedInUserAndBusinessId(businessId)
            .orElseThrow(() -> new GeneralException("No Business Found", "business.not.found", HttpStatus.NOT_FOUND));
        business.setTheme(businessTheme);
        business.setOnboardingStep(OnboardingSteps.WORKING_HOURS);
        businessRepository.save(business);
    }

    @Override
    @Transactional
    public void onboardingWorkingHours(List<WorkingHoursDTO> newHours, Long businessId) {
        BusinessEmployee businessEmployee = businessEmployeeRepository
            .findByUserLoginAndBusinessId(businessId)
            .orElseThrow(() -> new GeneralException("No Business Found", "business.not.found", HttpStatus.NOT_FOUND));
        workingHoursService.updateWorkingHours(businessId, businessEmployee.getId(), newHours);

        Business business = businessRepository
            .findBusinessByLoginAndBusinessId(businessId)
            .orElseThrow(() -> new GeneralException("No Business Found", "business.not.found", HttpStatus.NOT_FOUND));
        business.setOnboardingStep(OnboardingSteps.BUSINESS_IMAGES);
        businessRepository.save(business);
    }

    @Override
    @Transactional
    public void onboardingImagesUpload(OnboardingImagesRequestDTO onboardingImagesRequestDTO, Long businessId) throws IOException {
        var business = businessRepository
            .getByLoggedInUserAndBusinessId(businessId)
            .orElseThrow(() -> new GeneralException("No Business Found", "business.not.found", HttpStatus.NOT_FOUND));
        var bannerImage = cloudinaryImageUploader.uploadAndReplace(
            onboardingImagesRequestDTO.getBannerImage(),
            "business/" + business.getId() + "/banner",
            business.getBannerPublicId()
        );
        var logoImage = cloudinaryImageUploader.uploadAndReplace(
            onboardingImagesRequestDTO.getLogoImage(),
            "business/" + business.getId() + "/logo",
            business.getLogoPublicId()
        );

        business.setBannerUrl(bannerImage.url());
        business.setBannerPublicId(bannerImage.publicId());

        business.setLogo(logoImage.url());
        business.setLogoPublicId(logoImage.publicId());
        business.setOnboardingStep(OnboardingSteps.COMPLETED);
        business.setOnboardingCompleted(true);

        businessRepository.save(business);
    }

    @Override
    @Transactional
    public BusinessDTO onboardingSkipStep(Long businessId) {
        var business = businessRepository
            .getByLoggedInUserAndBusinessId(businessId)
            .orElseThrow(() -> new GeneralException("No Business Found", "business.not.found", HttpStatus.NOT_FOUND));

        var currentStep = business.getOnboardingStep();

        var nextStep =
            switch (currentStep) {
                case BUSINESS_THEME -> OnboardingSteps.WORKING_HOURS;
                case WORKING_HOURS -> OnboardingSteps.BUSINESS_IMAGES;
                case BUSINESS_IMAGES -> OnboardingSteps.COMPLETED;
                default -> throw new GeneralException("Can't skip the step.", "cant.skip.step", HttpStatus.BAD_REQUEST);
            };

        business.setOnboardingStep(nextStep);

        if (nextStep == OnboardingSteps.COMPLETED) {
            business.setOnboardingCompleted(true);
        }

        return businessMapper.toDto(businessRepository.save(business));
    }
}
