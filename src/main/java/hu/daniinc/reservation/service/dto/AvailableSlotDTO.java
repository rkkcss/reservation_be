package hu.daniinc.reservation.service.dto;

import java.time.ZonedDateTime;

public class AvailableSlotDTO {

    private ZonedDateTime start;
    private ZonedDateTime end;

    public AvailableSlotDTO(ZonedDateTime start, ZonedDateTime end) {
        this.start = start;
        this.end = end;
    }

    public ZonedDateTime getStart() {
        return start;
    }

    public ZonedDateTime getEnd() {
        return end;
    }
}
