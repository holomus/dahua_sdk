package org.example.dahuasdk.dto;

import com.netsdk.lib.NetSDKLib;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DeviceConnectionDTO {
    private String ip;
    private int port;
    private NetSDKLib.LLong loginHandle;
    private String status;
}
