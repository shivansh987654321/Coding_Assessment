package com.assessment.platform.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DatabaseKeepAlive {

    private static final Logger log = LoggerFactory.getLogger(DatabaseKeepAlive.class);
    private final JdbcTemplate jdbcTemplate;

    public DatabaseKeepAlive(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Runs every 10 minutes to prevent Aiven free tier from suspending the database
    @Scheduled(fixedRateString = "600000")
    public void ping() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        } catch (Exception e) {
            log.warn("DB keepalive ping failed: {}", e.getMessage());
        }
    }
}
