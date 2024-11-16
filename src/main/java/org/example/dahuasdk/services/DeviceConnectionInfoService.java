package org.example.dahuasdk.services;

import org.example.dahuasdk.dto.DeviceConnectionDTO;

import java.util.concurrent.ConcurrentHashMap;

public class DeviceConnectionInfoService {
    private static final DeviceConnectionInfoService INSTANCE = new DeviceConnectionInfoService();

    private final ConcurrentHashMap<String, DeviceConnectionDTO> deviceConnectionInfo = new ConcurrentHashMap<>();

    private DeviceConnectionInfoService() {}

    public static DeviceConnectionInfoService getInstance() {
        return INSTANCE;
    }

    public void put(String deviceId, DeviceConnectionDTO connectionInfo) {
        deviceConnectionInfo.put(deviceId, connectionInfo);
    }

    public DeviceConnectionDTO get(String deviceId) {
        return deviceConnectionInfo.get(deviceId);
    }

    public ConcurrentHashMap<String, DeviceConnectionDTO> getData() {
        return deviceConnectionInfo;
    }
}
