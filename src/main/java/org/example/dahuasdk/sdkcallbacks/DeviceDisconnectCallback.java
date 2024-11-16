package org.example.dahuasdk.sdkcallbacks;

import com.netsdk.lib.NetSDKLib;
import com.sun.jna.Pointer;
import lombok.RequiredArgsConstructor;
import org.example.dahuasdk.handlemanagers.DeviceLoginHandleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class DeviceDisconnectCallback implements NetSDKLib.fDisConnect {
    private static final Logger log = LoggerFactory.getLogger(DeviceDisconnectCallback.class);
    private final DeviceLoginHandleManager deviceConnectionInfoService;

    public void invoke(NetSDKLib.LLong m_hLoginHandle, String pchDVRIP, int nDVRPort, Pointer dwUser) {
        log.info("Device[{}] Port[{}] LoginHandle[{}], DisConnect!", pchDVRIP, nDVRPort, m_hLoginHandle.longValue());

        deviceConnectionInfoService.removeByHandle(m_hLoginHandle);
    }
}
