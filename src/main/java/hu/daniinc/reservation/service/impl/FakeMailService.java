package hu.daniinc.reservation.service.impl;

import hu.daniinc.reservation.domain.Appointment;
import hu.daniinc.reservation.domain.Guest;
import hu.daniinc.reservation.domain.User;
import hu.daniinc.reservation.service.AppointmentLinkService;
import hu.daniinc.reservation.service.CalendarLinkGenerator;
import hu.daniinc.reservation.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import tech.jhipster.config.JHipsterConstants;
import tech.jhipster.config.JHipsterProperties;

/**
 * Service for sending emails asynchronously.
 * <p>
 * We use the {@link Async} annotation to send emails asynchronously.
 */
@Service
@Profile(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT)
public class FakeMailService implements EmailService {

    private static final Logger LOG = LoggerFactory.getLogger(FakeMailService.class);

    private static final String USER = "user";

    private static final String BASE_URL = "baseUrl";

    private final JHipsterProperties jHipsterProperties;

    private final JavaMailSender javaMailSender;

    private final MessageSource messageSource;

    private final SpringTemplateEngine templateEngine;
    private final AppointmentLinkService appointmentLinkService;

    @Value("${jhipster.mail.base-url}")
    private String baseUrl;

    public FakeMailService(
        JHipsterProperties jHipsterProperties,
        JavaMailSender javaMailSender,
        MessageSource messageSource,
        SpringTemplateEngine templateEngine,
        AppointmentLinkService appointmentLinkService
    ) {
        this.jHipsterProperties = jHipsterProperties;
        this.javaMailSender = javaMailSender;
        this.messageSource = messageSource;
        this.templateEngine = templateEngine;
        this.appointmentLinkService = appointmentLinkService;
    }

    @Async
    @Override
    public void sendEmail(String to, String subject, String content, boolean isMultipart, boolean isHtml) {
        sendEmailSync(to, subject, content, isMultipart, isHtml);
    }

    @Override
    public void sendRegistrationEmail(User to) {
        Context context = new Context();
        context.setVariable("user", to);
        context.setVariable("baseUrl", baseUrl);

        String content = templateEngine.process("mail/activationEmail", context);
        String subject = "Sikeres regisztráció (LOCAL TEST)";

        this.sendEmail(to.getEmail(), subject, content, false, true);
    }

    @Override
    public void sendAppointmentReservedEmail(Appointment appointment) {}

    private void sendEmailSync(String to, String subject, String content, boolean isMultipart, boolean isHtml) {
        LOG.debug(
            "Send email[multipart '{}' and html '{}'] to '{}' with subject '{}' and content={}",
            isMultipart,
            isHtml,
            to,
            subject,
            content
        );

        // Prepare message using a Spring helper
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, isMultipart, StandardCharsets.UTF_8.name());
            message.setTo(to);
            message.setFrom(jHipsterProperties.getMail().getFrom());
            message.setSubject(subject);
            message.setText(content, isHtml);
            javaMailSender.send(mimeMessage);
            LOG.debug("Sent email to User '{}'", to);
        } catch (MailException | MessagingException e) {
            LOG.warn("Email could not be sent to user '{}'", to, e);
        }
    }

    @Async
    public void sendEmailFromTemplate(User user, String templateName, String titleKey) {
        sendEmailFromTemplateSync(user, templateName, titleKey);
    }

    private void sendEmailFromTemplateSync(User user, String templateName, String titleKey) {
        if (user.getEmail() == null) {
            LOG.debug("Email doesn't exist for user '{}'", user.getLogin());
            return;
        }
        Locale locale = Locale.forLanguageTag(user.getLangKey());
        Context context = new Context(locale);
        context.setVariable(USER, user);
        context.setVariable(BASE_URL, jHipsterProperties.getMail().getBaseUrl());
        String content = templateEngine.process(templateName, context);
        String subject = messageSource.getMessage(titleKey, null, locale);
        sendEmailSync(user.getEmail(), subject, content, false, true);
    }

    @Async
    public void sendActivationEmail(User user) {
        LOG.debug("Sending activation email to '{}'", user.getEmail());
        sendEmailFromTemplateSync(user, "mail/activationEmail", "email.activation.title");
    }

    @Async
    public void sendCreationEmail(User user) {
        LOG.debug("Sending creation email to '{}'", user.getEmail());
        sendEmailFromTemplateSync(user, "mail/creationEmail", "email.activation.title");
    }

    @Async
    public void sendPasswordResetMail(User user) {
        LOG.debug("Sending password reset email to '{}'", user.getEmail());
        sendEmailFromTemplateSync(user, "mail/passwordResetEmail", "email.reset.title");
    }

    @Override
    @Async
    public void sendPasswordChanged(User user) {
        LOG.debug("User password changed successfully '{}'", user.getEmail());
        Context context = new Context();
        context.setVariable("user", user);
        //TODO: create normal email for that
        //String content = templateEngine.process("mail/passwordReset", context);
        this.sendEmail(
                user.getEmail(),
                "Sikeres jelszó változtatás!",
                "Sikeresen megváltoztattad a jelszavadat! Ez rendszer üzenet, ne válaszolj rá!",
                false,
                false
            );
    }

    @Override
    public void sendEmailCancelled(Appointment appointment) {
        LOG.debug("Guest cancelled an appointment ID '{}'", appointment.getId());
        Context context = new Context();
        context.setVariable("appointment", appointment);
        String content = templateEngine.process("mail/appointmentCancelledEmail", context);

        this.sendEmail(appointment.getGuest().getEmail(), "Cancelled an appointment!", content, false, true);
    }

    @Async
    @Override
    public void sendAppointmentReminder(Guest guest, Appointment appointment) {
        LOG.debug("Emlékeztető email küldése a vendégnek: {}", guest.getEmail());

        // 1. Thymeleaf context and google calendar link
        Context context = new Context();
        context.setVariable("appointment", appointment);
        context.setVariable("guest", guest);
        context.setVariable(
            "appointmentCancelLink",
            appointmentLinkService.generateModificationLinkWithQueryParam(
                appointment.getBusinessEmployee().getBusiness().getSlug(),
                appointment.getModifierToken()
            )
        );
        context.setVariable(
            "googleCalendarLink",
            CalendarLinkGenerator.generateCalendarLink(
                appointment.getOffering().getTitle(),
                LocalDateTime.ofInstant(appointment.getStartDate(), ZoneId.systemDefault()),
                appointment.getOffering().getDurationMinutes(),
                "",
                appointment.getBusinessEmployee().getBusiness().getAddress()
            )
        );

        String content = templateEngine.process("mail/appointmentReminder", context);
        String subject = messageSource.getMessage("email.reminder.title", null, Locale.getDefault());

        // 2. generate .ics for IOS invitation
        ByteArrayResource icsFile = CalendarLinkGenerator.generateICSalAttachment(appointment);

        // 3. create email + attachment
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
            messageHelper.setTo(guest.getEmail());
            messageHelper.setFrom(jHipsterProperties.getMail().getFrom());
            messageHelper.setSubject(subject);
            messageHelper.setText(content, true);

            messageHelper.addAttachment("invite.ics", icsFile, "text/calendar; charset=UTF-8");

            javaMailSender.send(mimeMessage);
            LOG.debug("Sent appointment reminder with iCal attachment to '{}'", guest.getEmail());
        } catch (MailException | MessagingException e) {
            LOG.warn("Email reminder with attachment could not be sent to '{}'", guest.getEmail(), e);
        }
    }
}
