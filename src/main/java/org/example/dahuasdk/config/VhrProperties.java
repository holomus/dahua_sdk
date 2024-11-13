package org.example.dahuasdk.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "vhr")
public class VhrProperties {
    private int jobInterval;
    private int connectTimeout;
    private int readTimeout;
    private int writeTimeout;
    private String loadCommandsUri;
    private String saveCommandsResultUri;
    private String loadPhotoUri;
    private String updateDevicesStatusUri;
    private String middlewareHealthCheckUri;
    private String middlewareRequestHeaderName;

    public String loadCommandsUrl(String host) {
        return host + loadCommandsUri;
    }

    public String saveCommandsResultUrl(String host) {
        return host + saveCommandsResultUri;
    }

    public String loadPhotoUri(String host, String sha, String width, String height, String format, String quality) {
        return host + loadPhotoUri + "?sha=" + sha + "&width=" + width + "&height=" + height + "&format=" + format + "&quality=" + quality;
    }

    public String updateDevicesStatusUri(String host) {
        return host + updateDevicesStatusUri;
    }

    public String middlewareHealthCheckUri(String host) {
        return host + middlewareHealthCheckUri;
    }

    public String middlewareRequestHeaderName() {
        return middlewareRequestHeaderName == null || middlewareRequestHeaderName.isBlank() ? "vhr-gateway" : middlewareRequestHeaderName;
    }
}
