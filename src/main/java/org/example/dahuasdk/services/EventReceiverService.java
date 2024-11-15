package org.example.dahuasdk.services;

import com.netsdk.lib.NetSDKLib;
import com.netsdk.lib.ToolKits;
import com.sun.jna.Pointer;
import org.example.dahuasdk.dto.EventDTO;
import org.springframework.stereotype.Service;

@Service
public class EventReceiverService {
    // TODO: think if you need to release handle on device disconnect
    private static NetSDKLib.LLong eventListeningStart(NetSDKLib netSDKInstance, int channelId, NetSDKLib.LLong loginHandle, String deviceId) {
        int bNeedPicture = 1; // include photo info in events

        NetSDKLib.LLong m_hAttachHandle = netSDKInstance.CLIENT_RealLoadPictureEx(loginHandle, channelId, NetSDKLib.EVENT_IVS_ALL,
                bNeedPicture, new AnalyzerDataCB(deviceId), null, null);
        if(m_hAttachHandle.longValue() != 0  ) {
            // TODO: Make normal logging
            System.out.println("CLIENT_RealLoadPictureEx Success  ChannelId : \n" + channelId);
        } else {
            // TODO: Make normal logging
            System.err.println("CLIENT_RealLoadPictureEx Failed!" + ToolKits.getErrorCodePrint());
            return null;
        }

        return m_hAttachHandle;
    }

    public static NetSDKLib.LLong eventListeningStart(NetSDKLib netSDKInstance, NetSDKLib.LLong loginHandle, String deviceId) {
        int channelId = -1; // All channels

        var eventListenerHandle = EventReceiverService.eventListeningStart(netSDKInstance, channelId, loginHandle, deviceId);

        if (eventListenerHandle == null) {
            channelId = 0; // First channel

            eventListenerHandle = EventReceiverService.eventListeningStart(netSDKInstance, channelId, loginHandle, deviceId);
        }

        return eventListenerHandle;
    }

    private record AnalyzerDataCB(String deviceId) implements NetSDKLib.fAnalyzerDataCallBack {
        public int invoke(NetSDKLib.LLong lAnalyzerHandle, int dwAlarmType,
                          Pointer pAlarmInfo, Pointer pBuffer, int dwBufSize,
                          Pointer dwUser, int nSequence, Pointer reserved) {
            if (lAnalyzerHandle.longValue() == 0 || pAlarmInfo == null) {
                return -1;
            }

            // ignore all events, except access control events
            if (dwAlarmType != NetSDKLib.EVENT_IVS_ACCESS_CTL) {
                return 0;
            }

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

            var event = new EventDTO(msg, this.deviceId);

            System.out.println(event);

            return 0;
        }
    }
}
