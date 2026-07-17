package hu.daniinc.reservation.service.jobs;

import hu.daniinc.reservation.domain.enumeration.AppointmentStatus;
import hu.daniinc.reservation.repository.AppointmentRepository;
import hu.daniinc.reservation.service.EmailService;
import hu.daniinc.reservation.service.impl.FakeMailService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class AppointmentReminderJob implements Job {

    private static final Logger LOG = LoggerFactory.getLogger(AppointmentReminderJob.class);

    @Autowired
    private EmailService emailService;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOG.info("🚀 AppointmentReminderJob ELINDULT!");

        try {
            Long appointmentId = context.getJobDetail().getJobDataMap().getLong("appointmentId");
            LOG.info("📧 Emlékeztető küldése az appointmentId-hez: {}", appointmentId);

            appointmentRepository
                .findByIdWithDetails(appointmentId)
                .ifPresentOrElse(
                    app -> {
                        LOG.info(
                            "✅ Appointment megtalálva: id={}, status={}, email={}",
                            app.getId(),
                            app.getStatus(),
                            app.getGuest().getEmail()
                        );

                        if (app.getStatus() != AppointmentStatus.CANCELLED) {
                            LOG.info("📨 Email küldése indul...");
                            emailService.sendAppointmentReminder(app.getGuest(), app);
                            LOG.info("✅ Email sikeresen elküldve!");
                        } else {
                            LOG.warn("⚠️ Az appointment törölve lett, email NEM kerül kiküldésre");
                        }
                    },
                    () -> LOG.error("❌ Appointment nem található: {}", appointmentId)
                );
        } catch (Exception e) {
            LOG.error("❌ Hiba az AppointmentReminderJob futtatása közben", e);
            throw new JobExecutionException(e);
        }

        LOG.info("🏁 AppointmentReminderJob BEFEJEZVE");
    }
}
