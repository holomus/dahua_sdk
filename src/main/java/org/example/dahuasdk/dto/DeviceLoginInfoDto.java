package org.example.dahuasdk.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor

public class DeviceLoginInfoDto {
    String ip;
    int port;
    String userName;
    String password;
    String deviceId;
}
