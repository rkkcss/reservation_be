package hu.daniinc.reservation.service.dto;

import org.springframework.web.multipart.MultipartFile;

public class OnboardingImagesRequestDTO {

    private MultipartFile bannerImage;

    private MultipartFile logoImage;

    public OnboardingImagesRequestDTO(MultipartFile bannerImage, MultipartFile logoImage) {
        this.bannerImage = bannerImage;
        this.logoImage = logoImage;
    }

    public MultipartFile getBannerImage() {
        return bannerImage;
    }

    public void setBannerImage(MultipartFile bannerImage) {
        this.bannerImage = bannerImage;
    }

    public MultipartFile getLogoImage() {
        return logoImage;
    }

    public void setLogoImage(MultipartFile logoImage) {
        this.logoImage = logoImage;
    }
}
