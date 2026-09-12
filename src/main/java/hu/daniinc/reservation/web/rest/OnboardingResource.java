package hu.daniinc.reservation.web.rest;

import hu.daniinc.reservation.domain.enumeration.BusinessRole;
import hu.daniinc.reservation.domain.enumeration.BusinessTheme;
import hu.daniinc.reservation.domain.enumeration.OnboardingSteps;
import hu.daniinc.reservation.security.annotation.RequireBusinessRole;
import hu.daniinc.reservation.security.annotation.TenantBusiness;
import hu.daniinc.reservation.service.OnboardingService;
import hu.daniinc.reservation.service.dto.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/onboarding")
public class OnboardingResource {

    private static final Logger LOG = LoggerFactory.getLogger(OnboardingResource.class);
    private final OnboardingService onboardingService;

    public OnboardingResource(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @PostMapping("/step/details")
    public ResponseEntity<Map<String, Object>> onboardingComplete(
        @TenantBusiness(required = false) Long businessId,
        @RequestBody BusinessDTO businessDTO
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(onboardingService.onboardingStepComplete(businessDTO, businessId));
    }

    @PostMapping("/step/theme")
    @RequireBusinessRole(BusinessRole.OWNER)
    public ResponseEntity<Void> onboardingThemeComplete(@RequestBody BusinessTheme businessTheme, @TenantBusiness Long businessId) {
        onboardingService.onBoardingThemeComplete(businessTheme, businessId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/step")
    public ResponseEntity<?> getOnboardingStep(@TenantBusiness(required = false) Long businessId) {
        LOG.debug("REST request to get onboarding step");
        return ResponseEntity.ok(onboardingService.getCurrentOnboardingStep(businessId));
    }

    @PostMapping("/step/working-hours")
    @RequireBusinessRole(BusinessRole.OWNER)
    public ResponseEntity<?> onBoardingWorkingHours(@TenantBusiness Long businessId, @RequestBody List<WorkingHoursDTO> newHours) {
        onboardingService.onboardingWorkingHours(newHours, businessId);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/step/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequireBusinessRole(BusinessRole.OWNER)
    public ResponseEntity<?> onboardingImages(
        @TenantBusiness Long businessId,
        @RequestParam(value = "bannerImage") MultipartFile bannerImage,
        @RequestParam(value = "logoImage") MultipartFile logoImage
    ) throws IOException {
        OnboardingImagesRequestDTO dto = new OnboardingImagesRequestDTO(bannerImage, logoImage);
        onboardingService.onboardingImagesUpload(dto, businessId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/step/skip")
    @RequireBusinessRole(BusinessRole.OWNER)
    public ResponseEntity<BusinessDTO> onboardingSkip(@TenantBusiness Long businessId) {
        return ResponseEntity.ok(onboardingService.onboardingSkipStep(businessId));
    }
}
