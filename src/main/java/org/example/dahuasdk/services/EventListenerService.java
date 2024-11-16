package org.example.dahuasdk.services;

import com.netsdk.lib.NetSDKLib;
import com.netsdk.lib.ToolKits;
import lombok.RequiredArgsConstructor;
import org.example.dahuasdk.handlermanagers.EventListenerHandleManager;
import org.example.dahuasdk.sdkcallbacks.EventListenerCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class EventListenerService {
    private static final Logger log = LoggerFactory.getLogger(EventListenerService.class);
    private final EventProcessorService eventProcessor;
    private final EventListenerCallback eventCallback;
    private final EventListenerHandleManager listenerHandleManager;

    // TODO: think if you need to release handle on device disconnect
    private void eventListeningStart(NetSDKLib netSDKInstance, int channelId, NetSDKLib.LLong loginHandle, String deviceId) {
        int bNeedPicture = 1; // include photo info in events

        NetSDKLib.LLong eventListenerHandle = netSDKInstance.CLIENT_RealLoadPictureEx(loginHandle, channelId, NetSDKLib.EVENT_IVS_ALL,
                bNeedPicture, eventCallback, null, null);
        if(eventListenerHandle.longValue() != 0) {
            log.debug("CLIENT_RealLoadPictureEx Success. DeviceId: {}, ChannelId : {}", deviceId, channelId);
        } else {
            log.error("CLIENT_RealLoadPictureEx Failed! Error Code: {}", ToolKits.getErrorCodePrint());
            return;
        }

        listenerHandleManager.storeEventListenerHandle(eventListenerHandle, deviceId);
    }

    public void eventListeningStart(NetSDKLib netSDKInstance, NetSDKLib.LLong loginHandle, String deviceId) {
        int channelId = 0; // All channels
        NetSDKLib.LLong eventListenerHandle = null;

        try {
            eventListeningStart(netSDKInstance, channelId, loginHandle, deviceId);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            log.warn("Failed to start event listener for deviceId: {}", deviceId);
        }
    }
}
