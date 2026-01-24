package hu.daniinc.reservation.service.impl;

import hu.daniinc.reservation.domain.Appointment;
import hu.daniinc.reservation.domain.Guest;
import hu.daniinc.reservation.domain.User;
import hu.daniinc.reservation.service.EmailService;
import java.time.Instant;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import tech.jhipster.config.JHipsterConstants;

@Service
@Profile(JHipsterConstants.SPRING_PROFILE_PRODUCTION)
public class MailApiService implements EmailService {

    private final SpringTemplateEngine templateEngine;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.api.url}")
    private String apiUrl;

    @Value("${jhipster.mail.base-url}")
    private String baseUrl;

    public MailApiService(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Override
    public void sendRegistrationEmail(User user) {
        Context context = new Context();
        context.setVariable("user", user);
        context.setVariable("baseUrl", baseUrl);

        String content = templateEngine.process("mail/activationEmail", context);
        String subject = "Sikeres regisztráció";

        this.sendEmail(user.getEmail(), subject, content, false, false);
    }

    @Override
    public void sendAppointmentReminder(Guest guest, Appointment appointment) {}

    @Override
    public void sendEmail(String toEmail, String subject, String content, boolean isMultipart, boolean isHtml) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("sender", Map.of("name", "BooklyApp", "email", "no-reply@booklyapp.me"));
        body.put("to", List.of(Map.of("email", toEmail)));
        body.put("subject", subject);
        body.put("htmlContent", content);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(apiUrl, request, String.class);
        } catch (Exception e) {
            System.err.println("Brevo API hiba: " + e.getMessage());
        }
    }
}
