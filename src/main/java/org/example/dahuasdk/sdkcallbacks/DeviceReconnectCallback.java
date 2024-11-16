package org.example.dahuasdk.sdkcallbacks;

import com.netsdk.lib.NetSDKLib;
import com.sun.jna.Pointer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class DeviceReconnectCallback implements NetSDKLib.fHaveReConnect {
    private static final Logger log = LoggerFactory.getLogger(DeviceReconnectCallback.class);

    public void invoke(NetSDKLib.LLong m_hLoginHandle, String pchDVRIP, int nDVRPort, Pointer dwUser) {
        log.info("ReConnect Device[{}] Port[{}]", pchDVRIP, nDVRPort);
    }
}
