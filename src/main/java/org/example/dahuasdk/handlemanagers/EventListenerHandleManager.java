package org.example.dahuasdk.handlemanagers;

import com.netsdk.lib.NetSDKLib;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Service
public class EventListenerHandleManager {
    private final ConcurrentHashMap<NetSDKLib.LLong, String> deviceHandlers = new ConcurrentHashMap<>();

    public void storeEventListenerHandle(NetSDKLib.LLong listenerHandle, String deviceId) {
        deviceHandlers.put(listenerHandle, deviceId);
    }

    public String getDeviceId(NetSDKLib.LLong listenerHandle) {
        return deviceHandlers.get(listenerHandle);
    }
}
