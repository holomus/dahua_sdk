package org.example.dahuasdk.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "dahua")
public class DahuaProperties {
    private String username;
    private String password;
    private int connectTimeout;
    private int readTimeout;
    private int writeTimeout;
}
