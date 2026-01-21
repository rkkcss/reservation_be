package hu.daniinc.reservation.service.quartz;

import hu.daniinc.reservation.domain.Appointment;
import hu.daniinc.reservation.service.jobs.AppointmentReminderJob;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentReminderService {

    private static final Logger LOG = LoggerFactory.getLogger(AppointmentReminderService.class);

    private final Scheduler scheduler;

    public AppointmentReminderService(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void scheduleEmailReminder(Appointment appointment) {
        try {
            LOG.info("🔔 scheduleEmailReminder meghívva az appointment-hez: {}", appointment.getId());

            // test: start after 1 min
            Instant triggerTime = Instant.now().plus(1, ChronoUnit.MINUTES);
            LOG.warn("⚠️ TESZT MÓD: Job 1 perc múlvára ütemezve ({})", triggerTime);

            String jobId = "appointment-reminder-" + appointment.getId();

            JobDetail jobDetail = JobBuilder.newJob(AppointmentReminderJob.class)
                .withIdentity(jobId, "appointment-jobs")
                .usingJobData("appointmentId", appointment.getId())
                .storeDurably(false)
                .requestRecovery()
                .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger-" + jobId, "appointment-triggers")
                .startAt(Date.from(triggerTime))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();

            scheduler.scheduleJob(jobDetail, trigger);

            boolean exists = scheduler.checkExists(jobDetail.getKey());
            LOG.info("🔍 Job létezik az adatbázisban? {}", exists);

            // ÚJ: Trigger részletek
            Trigger storedTrigger = scheduler.getTrigger(trigger.getKey());
            if (storedTrigger != null) {
                LOG.info("📅 Trigger következő futási ideje: {}", storedTrigger.getNextFireTime());
                LOG.info("📊 Trigger állapota: {}", scheduler.getTriggerState(trigger.getKey()));
            }

            LOG.info("✅ Email emlékeztető sikeresen ütemezve!");
            LOG.info("   └─ Időpont ID: {}", appointment.getId());
            LOG.info("   └─ Trigger idő: {}", triggerTime);
            LOG.info("   └─ Vendég email: {}", appointment.getGuest().getEmail());
        } catch (SchedulerException e) {
            LOG.error("❌ Hiba a Quartz ütemezés közben a foglaláshoz: {}", appointment.getId(), e);
        }
    }
}
