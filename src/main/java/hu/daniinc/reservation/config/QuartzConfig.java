package hu.daniinc.reservation.config;

import hu.daniinc.reservation.service.jobs.AutowiringSpringBeanJobFactory;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
public class QuartzConfig {

    private static final Logger log = LoggerFactory.getLogger(QuartzConfig.class);

    private final AutowiringSpringBeanJobFactory jobFactory;
    private final DataSource dataSource;

    public QuartzConfig(AutowiringSpringBeanJobFactory jobFactory, DataSource dataSource) {
        this.jobFactory = jobFactory;
        this.dataSource = dataSource;
    }

    @Bean
    public SchedulerFactoryBeanCustomizer schedulerFactoryBeanCustomizer() {
        return schedulerFactoryBean -> {
            schedulerFactoryBean.setJobFactory(jobFactory);
            schedulerFactoryBean.setOverwriteExistingJobs(true);
            schedulerFactoryBean.setAutoStartup(false);
            // A DataSource-t és TransactionManager-t a Spring Boot automatikusan beállítja
        };
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startScheduler(ApplicationReadyEvent event) {
        if (!validateQuartzTables()) {
            log.error("Quartz tables validation failed. Scheduler will not start.");
            return;
        }

        try {
            Scheduler scheduler = event.getApplicationContext().getBean(Scheduler.class);

            if (!scheduler.isStarted()) {
                log.info("Starting Quartz Scheduler...");
                scheduler.start();

                int jobCount = scheduler.getJobKeys(null).size();
                int triggerCount = scheduler.getTriggerKeys(null).size();

                log.info("✅ Quartz Scheduler started successfully - Jobs: {}, Triggers: {}", jobCount, triggerCount);
            }
        } catch (SchedulerException e) {
            log.error("❌ Failed to start Quartz Scheduler. Application continues without scheduler.", e);
        }
    }

    private boolean validateQuartzTables() {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "qrtz_%", null);

            int tableCount = 0;
            while (tables.next()) {
                tableCount++;
                log.debug("Found Quartz table: {}", tables.getString("TABLE_NAME"));
            }

            if (tableCount < 11) {
                log.error("❌ Quartz tables incomplete! Found {} tables, expected at least 11", tableCount);
                log.error("Run the application with spring.quartz.jdbc.initialize-schema=always to create tables");
                return false;
            }

            log.info("✅ Quartz tables validated: {} tables found", tableCount);
            return true;
        } catch (SQLException e) {
            log.error("❌ Failed to validate Quartz tables", e);
            return false;
        }
    }
}
