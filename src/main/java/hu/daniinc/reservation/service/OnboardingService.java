package hu.daniinc.reservation.service;

import hu.daniinc.reservation.domain.enumeration.BusinessTheme;
import hu.daniinc.reservation.domain.enumeration.OnboardingSteps;
import hu.daniinc.reservation.service.dto.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface OnboardingService {
    OnboardingSteps getCurrentOnboardingStep(Long businessId);

    Map<String, Object> onboardingStepComplete(BusinessDTO businessDTO, Long businessId);

    void onBoardingThemeComplete(BusinessTheme businessTheme, Long businessId);

    void onboardingWorkingHours(List<WorkingHoursDTO> newHours, Long businessId);

    void onboardingImagesUpload(OnboardingImagesRequestDTO onboardingImagesRequestDTO, Long businessId) throws IOException;

    BusinessDTO onboardingSkipStep(Long businessId);
}
