package hu.daniinc.reservation.service.dto;

import java.math.BigDecimal;
import java.util.Objects;

public class GuestStatisticDTO {

    private Long allAppointmentCount;
    private Long cancelledAppointmentCount;
    private Long didNotComeCount;
    private Long appearedAppointmentCount;
    private BigDecimal allSpentMoney;

    public GuestStatisticDTO() {}

    public GuestStatisticDTO(
        Long allAppointmentCount,
        Long cancelledAppointmentCount,
        Long didNotComeCount,
        Long appearedAppointmentCount,
        BigDecimal allSpentMoney
    ) {
        this.allAppointmentCount = allAppointmentCount;
        this.cancelledAppointmentCount = cancelledAppointmentCount;
        this.didNotComeCount = didNotComeCount;
        this.appearedAppointmentCount = appearedAppointmentCount;
        this.allSpentMoney = allSpentMoney;
    }

    public Long getAllAppointmentCount() {
        return allAppointmentCount;
    }

    public void setAllAppointmentCount(Long allAppointmentCount) {
        this.allAppointmentCount = allAppointmentCount;
    }

    public Long getCancelledAppointmentCount() {
        return cancelledAppointmentCount;
    }

    public void setCancelledAppointmentCount(Long cancelledAppointmentCount) {
        this.cancelledAppointmentCount = cancelledAppointmentCount;
    }

    public Long getDidNotComeCount() {
        return didNotComeCount;
    }

    public void setDidNotComeCount(Long didNotComeCount) {
        this.didNotComeCount = didNotComeCount;
    }

    public Long getAppearedAppointmentCount() {
        return appearedAppointmentCount;
    }

    public void setAppearedAppointmentCount(Long appearedAppointmentCount) {
        this.appearedAppointmentCount = appearedAppointmentCount;
    }

    public BigDecimal getAllSpentMoney() {
        return allSpentMoney;
    }

    public void setAllSpentMoney(BigDecimal allSpentMoney) {
        this.allSpentMoney = allSpentMoney;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        GuestStatisticDTO that = (GuestStatisticDTO) o;
        return (
            Objects.equals(allAppointmentCount, that.allAppointmentCount) &&
            Objects.equals(cancelledAppointmentCount, that.cancelledAppointmentCount) &&
            Objects.equals(didNotComeCount, that.didNotComeCount) &&
            Objects.equals(appearedAppointmentCount, that.appearedAppointmentCount) &&
            Objects.equals(allSpentMoney, that.allSpentMoney)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(allAppointmentCount, cancelledAppointmentCount, didNotComeCount, appearedAppointmentCount, allSpentMoney);
    }
}
