package hu.daniinc.reservation.service;

import hu.daniinc.reservation.domain.User;

public interface EmailService {
    void sendEmail(String to, String subject, String content, boolean isMultipart, boolean isHtml);
    void sendRegistrationEmail(User to);
}
