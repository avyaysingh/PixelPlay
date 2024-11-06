package com.stream.stream_backend.Health;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Endpoint(id = "configdetails")
public class ConfigDetailsEndpoint {

    private final Environment environment;

    public ConfigDetailsEndpoint(Environment environment) {
        this.environment = environment;
    }

    @ReadOperation
    public Map<String, Object> showConfigDetails() {
        Map<String, Object> details = new HashMap<>();

        details.put("activeProfiles", environment.getActiveProfiles());
        details.put("applicationName", environment.getProperty("spring.application.name"));
        details.put("serverPort", environment.getProperty("server.port"));
        details.put("jwtSecretKey", environment.getProperty("jwt.secret-key"));
        details.put("dataSourceUrl", environment.getProperty("spring.datasource.url"));

        return details;
    }
}
