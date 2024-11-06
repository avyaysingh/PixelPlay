package com.stream.stream_backend.Health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import com.stream.stream_backend.repositories.VideoRepository;

@Component
public class DatabaseMonitoringService implements HealthIndicator {

    private final String DATABASE_NAME = "Video MetaData Database";

    @Autowired
    private VideoRepository videoRepository;

    @Override
    public Health health() {
        if (isDatabaseHealthy()) {
            return Health.up().withDetail(DATABASE_NAME, "is up and running").build();
        } else {
            return Health.down().withDetail(DATABASE_NAME, "is down and running").build();
        }
    }

    private boolean isDatabaseHealthy() {
        try {
            videoRepository.findById("13a7e3d4-4903-4bd4-877b-b031b08a9d0e");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
