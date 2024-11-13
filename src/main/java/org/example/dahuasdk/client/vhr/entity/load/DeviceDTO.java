package org.example.dahuasdk.client.vhr.entity.load;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class DeviceDTO {
    @JsonProperty("deviceId")
    private String deviceId;
    @JsonProperty("device_name")
    private String deviceName;
    @JsonProperty("login")
    private String login;
    @JsonProperty("password")
    private String password;
}
