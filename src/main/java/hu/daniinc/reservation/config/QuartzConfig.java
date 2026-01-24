package hu.daniinc.reservation.config;

import hu.daniinc.reservation.service.jobs.AutowiringSpringBeanJobFactory;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import javax.sql.DataSource;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.quartz.QuartzDataSource;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class QuartzConfig {

    private static final Logger log = LoggerFactory.getLogger(QuartzConfig.class);

    private final AutowiringSpringBeanJobFactory jobFactory;
    private final DataSource dataSource;
    private final PlatformTransactionManager transactionManager;

    public QuartzConfig(AutowiringSpringBeanJobFactory jobFactory, DataSource dataSource, PlatformTransactionManager transactionManager) {
        this.jobFactory = jobFactory;
        this.dataSource = dataSource;
        this.transactionManager = transactionManager;
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();

        factory.setJobFactory(jobFactory);
        factory.setDataSource(dataSource);
        factory.setTransactionManager(transactionManager);
        factory.setOverwriteExistingJobs(true);
        factory.setWaitForJobsToCompleteOnShutdown(true);
        factory.setAutoStartup(false);

        // Quartz properties
        Properties properties = new Properties();
        properties.setProperty("org.quartz.scheduler.instanceName", "ReservationScheduler");
        properties.setProperty("org.quartz.scheduler.instanceId", "AUTO");
        properties.setProperty("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
        properties.setProperty("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate");
        properties.setProperty("org.quartz.jobStore.tablePrefix", "qrtz_");
        properties.setProperty("org.quartz.jobStore.isClustered", "true");
        properties.setProperty("org.quartz.jobStore.clusterCheckinInterval", "20000");
        properties.setProperty("org.quartz.jobStore.useProperties", "true");
        properties.setProperty("org.quartz.jobStore.misfireThreshold", "60000");
        properties.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        properties.setProperty("org.quartz.threadPool.threadCount", "5");

        factory.setQuartzProperties(properties);

        return factory;
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
