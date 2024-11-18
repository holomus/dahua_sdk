package org.example.dahuasdk.handlemanagers;

import com.netsdk.lib.NetSDKLib;
import lombok.RequiredArgsConstructor;
import org.example.dahuasdk.dto.DeviceConnectionDTO;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Service
public class DeviceLoginHandleManager {
    private final ConcurrentHashMap<String, DeviceConnectionDTO> deviceConnectionInfo = new ConcurrentHashMap<>();

    public void put(String deviceId, DeviceConnectionDTO connectionInfo) {
        deviceConnectionInfo.put(deviceId, connectionInfo);
    }

    public DeviceConnectionDTO get(String deviceId) {
        return deviceConnectionInfo.get(deviceId);
    }

    public void remove(String deviceId) {
        deviceConnectionInfo.remove(deviceId);
    }

    public void removeByHandle(NetSDKLib.LLong loginHandle) {
        for (Map.Entry<String, DeviceConnectionDTO> entry : deviceConnectionInfo.entrySet()) {
            String key = entry.getKey();
            DeviceConnectionDTO value = entry.getValue();

            if (Objects.equals(loginHandle, value.getLoginHandle())) {
                remove(key);
                break;
            }
        }
    }
}
