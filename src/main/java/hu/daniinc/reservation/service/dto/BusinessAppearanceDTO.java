package hu.daniinc.reservation.service.dto;

import hu.daniinc.reservation.domain.enumeration.BusinessTheme;

public class BusinessAppearanceDTO {

    private String logo;

    private BusinessTheme theme;

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public BusinessTheme getTheme() {
        return theme;
    }

    public void setTheme(BusinessTheme theme) {
        this.theme = theme;
    }
}
