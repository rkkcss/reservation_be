package hu.daniinc.reservation.config;

import hu.daniinc.reservation.service.jobs.AutowiringSpringBeanJobFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Bean
    public AutowiringSpringBeanJobFactory autowiringSpringBeanJobFactory() {
        return new AutowiringSpringBeanJobFactory();
    }
}
