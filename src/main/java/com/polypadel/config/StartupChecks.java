package com.polypadel.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Perform some startup validations and fail fast if critical configuration is missing or using defaults.
 */
@Component
public class StartupChecks implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupChecks.class);

    private final Environment env;

    @Value("${spring.datasource.username:}")
    private String dbUser;

    @Value("${spring.datasource.password:}")
    private String dbPassword;

    public StartupChecks(Environment env) {
        this.env = env;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        boolean dev = env.getActiveProfiles().length == 0 || java.util.Arrays.asList(env.getActiveProfiles()).contains("dev");
        if (!dev) {
            if ("polypadel".equals(dbUser) && "changeme".equals(dbPassword)) {
                throw new IllegalStateException("Default DB credentials are in use in non-dev profile; please set SPRING_DATASOURCE_USERNAME/PASSWORD appropriately");
            }
        } else {
            log.info("StartupChecks: dev profile detected - skipping some production-only validations");
        }
    }
}
