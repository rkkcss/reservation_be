package hu.daniinc.reservation.service.dto;

public class TopOfferingStatisticDTO {

    private String offeringName;
    private Long offeringCount;

    public TopOfferingStatisticDTO() {}

    public TopOfferingStatisticDTO(String offeringName, Long offeringCount) {
        this.offeringName = offeringName;
        this.offeringCount = offeringCount;
    }

    public String getOfferingName() {
        return offeringName;
    }

    public void setOfferingName(String offeringName) {
        this.offeringName = offeringName;
    }

    public Long getOfferingCount() {
        return offeringCount;
    }

    public void setOfferingCount(Long offeringCount) {
        this.offeringCount = offeringCount;
    }
}
