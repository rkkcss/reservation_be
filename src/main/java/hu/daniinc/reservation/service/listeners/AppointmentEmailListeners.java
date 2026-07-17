package hu.daniinc.reservation.service.listeners;

import hu.daniinc.reservation.domain.Appointment;
import hu.daniinc.reservation.domain.enumeration.AppointmentStatus;
import hu.daniinc.reservation.repository.AppointmentRepository;
import hu.daniinc.reservation.service.EmailService;
import hu.daniinc.reservation.service.quartz.AppointmentReminderService;
import hu.daniinc.reservation.web.rest.errors.GeneralException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AppointmentEmailListeners {

    private static final Logger LOG = LoggerFactory.getLogger(AppointmentEmailListeners.class);

    private final EmailService emailService;
    private final AppointmentReminderService appointmentReminderService;

    public AppointmentEmailListeners(EmailService emailService, AppointmentReminderService appointmentReminderService) {
        this.emailService = emailService;
        this.appointmentReminderService = appointmentReminderService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAppointmentCreated(Appointment appointment) {
        try {
            emailService.sendAppointmentReminder(appointment.getGuest(), appointment);

            if (appointment.getStatus() == AppointmentStatus.CONFIRMED) {
                appointmentReminderService.scheduleEmailReminder(appointment);
            }
        } catch (Exception e) {
            LOG.error("Failed to send email for appointment id={}", appointment.getId(), e);
        }
    }
}
