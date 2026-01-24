package hu.daniinc.reservation.config;

import hu.daniinc.reservation.service.jobs.AutowiringSpringBeanJobFactory;
import java.util.Properties;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class QuartzConfig {

    private final AutowiringSpringBeanJobFactory jobFactory;
    private final DataSource dataSource;
    private final PlatformTransactionManager transactionManager;

    public QuartzConfig(AutowiringSpringBeanJobFactory jobFactory, DataSource dataSource, PlatformTransactionManager transactionManager) {
        this.jobFactory = jobFactory;
        this.dataSource = dataSource;
        this.transactionManager = transactionManager;
    }

    @Bean
    public SchedulerFactoryBeanCustomizer schedulerFactoryBeanCustomizer() {
        return schedulerFactoryBean -> {
            schedulerFactoryBean.setJobFactory(jobFactory);
            schedulerFactoryBean.setDataSource(dataSource);
            schedulerFactoryBean.setTransactionManager(transactionManager);
            schedulerFactoryBean.setOverwriteExistingJobs(true);
            schedulerFactoryBean.setWaitForJobsToCompleteOnShutdown(true);
            schedulerFactoryBean.setAutoStartup(true);

            // Quartz properties
            Properties quartzProperties = new Properties();
            quartzProperties.put("org.quartz.jobStore.dataSource", "quartzDS");
            schedulerFactoryBean.setQuartzProperties(quartzProperties);
        };
    }
}
