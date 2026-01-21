package hu.daniinc.reservation.service;

import hu.daniinc.reservation.domain.Guest;
import hu.daniinc.reservation.domain.User;
import java.time.Instant;

public interface EmailService {
    void sendEmail(String to, String subject, String content, boolean isMultipart, boolean isHtml);
    void sendRegistrationEmail(User to);

    void sendAppointmentReminder(Guest guest, Instant startDate);
}
