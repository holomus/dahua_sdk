package org.example.dahuasdk.sdkcallbacks;

import com.netsdk.lib.NetSDKLib;
import com.netsdk.lib.ToolKits;
import com.sun.jna.Pointer;
import lombok.RequiredArgsConstructor;
import org.example.dahuasdk.dto.EventDTO;
import org.example.dahuasdk.handlermanagers.EventListenerHandleManager;
import org.example.dahuasdk.services.EventProcessorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class EventListenerCallback implements NetSDKLib.fAnalyzerDataCallBack {
    private static final Logger log = LoggerFactory.getLogger(EventListenerCallback.class);
    private final EventProcessorService eventProcessor;
    private final EventListenerHandleManager listenerHandleManager;

    public int invoke(NetSDKLib.LLong lAnalyzerHandle, int dwAlarmType,
                      Pointer pAlarmInfo, Pointer pBuffer, int dwBufSize,
                      Pointer dwUser, int nSequence, Pointer reserved) {
        log.debug("Received event at {}", LocalDateTime.now());

        if (lAnalyzerHandle.longValue() == 0 || pAlarmInfo == null) {
            return -1;
        }

        // ignore all events, except access control events
        if (dwAlarmType != NetSDKLib.EVENT_IVS_ACCESS_CTL) {
            return 0;
        }

        var deviceId = listenerHandleManager.getDeviceId(lAnalyzerHandle);

        if (deviceId == null) {
            return -1;
        }

        int resultCode = 0;

        try {
            NetSDKLib.DEV_EVENT_ACCESS_CTL_INFO msg = new NetSDKLib.DEV_EVENT_ACCESS_CTL_INFO();
            ToolKits.GetPointerData(pAlarmInfo, msg);

            // TODO: add image processing logic
            // File path = new File("./GateSnapPicture/");
            // if (!path.exists()) {
            //     path.mkdir();
            // }
            //
            // String snapPicPath = path + "\\" + System.currentTimeMillis() + "GateSnapPicture.jpg";  // 保存图片地址
            // byte[] buffer = pBuffer.getByteArray(0, dwBufSize);
            // ByteArrayInputStream byteArrInputGlobal = new ByteArrayInputStream(buffer);
            //
            // try {
            //     BufferedImage gateBufferedImage = ImageIO.read(byteArrInputGlobal);
            //     if(gateBufferedImage != null) {
            //         ImageIO.write(gateBufferedImage, "jpg", new File(snapPicPath));
            //     }
            // } catch (IOException e2) {
            //     e2.printStackTrace();
            // }

            var event = new EventDTO(msg, deviceId);

            log.debug("Received event for deviceID: {}, eventTime: {}, personCode: {}, eventCode: {}", event.getDeviceId(), event.getUTCEventTime(), event.getPersonCode(), event.getEventCode());

            eventProcessor.processEvent(event);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            resultCode = -1;
        }

        return resultCode;
    }
}


