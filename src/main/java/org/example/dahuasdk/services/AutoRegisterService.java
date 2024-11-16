package org.example.dahuasdk.services;

import com.netsdk.demo.module.LoginModule;
import com.netsdk.lib.NetSDKLib;
import com.netsdk.lib.ToolKits;
import com.sun.jna.Pointer;
import lombok.RequiredArgsConstructor;
import org.example.dahuasdk.DahuaSdkApplication;
import org.example.dahuasdk.dao.AppDAO;
import org.example.dahuasdk.dto.DeviceConnectionDTO;
import org.example.dahuasdk.entity.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor

public class AutoRegisterService {
    private static final Logger log = LoggerFactory.getLogger(AutoRegisterService.class);
    private final AppService appService;
    private final AppDAO dao;
    private static class DisConnect implements NetSDKLib.fDisConnect {
        public void invoke(NetSDKLib.LLong m_hLoginHandle, String pchDVRIP, int nDVRPort, Pointer dwUser) {
            log.info("Device[{}] Port[{}] LoginHandle[{}], DisConnect!", pchDVRIP, nDVRPort, m_hLoginHandle.longValue());

            for (Map.Entry<String, DeviceConnectionDTO> entry : deviceConnectionInfoService.getData().entrySet()) {
                String key = entry.getKey();
                DeviceConnectionDTO value = entry.getValue();

                if (Objects.equals(m_hLoginHandle, value.getLoginHandle())) {
                    value.setStatus("F");
                    deviceConnectionInfoService.put(key, value);
                    break;
                }
            }
        }
    }
    private static class HaveReConnect implements NetSDKLib.fHaveReConnect {
        public void invoke(NetSDKLib.LLong m_hLoginHandle, String pchDVRIP, int nDVRPort, Pointer dwUser) {
            log.info("ReConnect Device[{}] Port[{}]", pchDVRIP, nDVRPort);
        }
    }
    private class ServiceCB implements NetSDKLib.fServiceCallBack {
        @Override
        public int invoke(NetSDKLib.LLong lHandle, final String pIp, final int wPort,
                          int lCommand, Pointer pParam, int dwParamLen,
                          Pointer dwUserData) {

            byte[] buffer = new byte[dwParamLen];
            pParam.read(0, buffer, 0, dwParamLen);
            String deviceId = "";
            try {
                deviceId = new String(buffer, "GBK").trim();
            } catch (UnsupportedEncodingException e) {
                log.error(e.getMessage());
            }

            switch (lCommand) {
                case NetSDKLib.EM_LISTEN_TYPE.NET_DVR_DISCONNECT: {
                    log.error("disconnected");
                    break;
                }
                case NetSDKLib.EM_LISTEN_TYPE.NET_DVR_SERIAL_RETURN: {
                    String finalDeviceId = deviceId;

                    new SwingWorker<Boolean, String>() {
                        @Override
                        protected Boolean doInBackground() {
                            Device device = appService.findDeviceByDeviceId(finalDeviceId);

                            NetSDKLib.LLong loginHandle = login(
                                    device.getLogin(),
                                    device.getPassword(),
                                    device.getDeviceId(),
                                    pIp.trim(),
                                    wPort
                            );

                            DeviceConnectionDTO deviceInfo = new DeviceConnectionDTO();
                            deviceInfo.setLoginHandle(loginHandle);

                            if (loginHandle.longValue() != 0) {
                                log.info("Login to device successfully. DeviceId[{}], Ip[{}], Port[{}], LoginHandel[{}] ", finalDeviceId, pIp, wPort, loginHandle);

                                deviceInfo.setStatus("O");
                                device.setLastOnlineTime(new Date());
                                dao.saveDevice(device);

                                deviceConnectionInfoService.put(finalDeviceId, deviceInfo);
                                return true;
                            }

                            log.error("Login to device failed. DeviceId[{}], Ip[{}], Port[{}], LoginHandel[{}] ", finalDeviceId, pIp, wPort, loginHandle);

                            deviceInfo.setStatus("F");
                            deviceConnectionInfoService.put(finalDeviceId, deviceInfo);
                            return false;
                        }
                    }.execute();
                    break;
                }
                default:
                    break;
            }

            return 0;
        }
    }

    // ---------------------  variables --------------------- //
    private final NetSDKLib netsdk = NetSDKLib.NETSDK_INSTANCE;
    public NetSDKLib.LLong serverHanle;
    public NetSDKLib.NET_DEVICEINFO_Ex m_stDeviceInfo = new NetSDKLib.NET_DEVICEINFO_Ex();
    public final ServiceCB callback = new ServiceCB();
    private final DisConnect disConnect = new DisConnect();
    private final HaveReConnect haveReConnect = new HaveReConnect();
    private static  final DeviceConnectionInfoService deviceConnectionInfoService = DeviceConnectionInfoService.getInstance();

    public NetSDKLib.LLong login(
            String m_strUser,
            String m_strPassword,
            String deviceIds,
            String m_strIp,
            int m_nPort
    ) {
        Pointer deviceId = ToolKits.GetGBKStringToPointer(deviceIds);

        NetSDKLib.NET_IN_LOGIN_WITH_HIGHLEVEL_SECURITY pstInParam = new NetSDKLib.NET_IN_LOGIN_WITH_HIGHLEVEL_SECURITY();

        pstInParam.nPort = m_nPort;
        pstInParam.szIP = m_strIp.getBytes();
        pstInParam.szPassword = m_strPassword.getBytes();
        pstInParam.szUserName = m_strUser.getBytes();
        pstInParam.emSpecCap = 2;
        pstInParam.pCapParam = deviceId;

        NetSDKLib.NET_OUT_LOGIN_WITH_HIGHLEVEL_SECURITY pstOutParam = new NetSDKLib.NET_OUT_LOGIN_WITH_HIGHLEVEL_SECURITY();
        pstOutParam.stuDeviceInfo = m_stDeviceInfo;

        return LoginModule.netsdk.CLIENT_LoginWithHighLevelSecurity(pstInParam, pstOutParam);
    }

    public void initSDK() {
        boolean bInit = netsdk.CLIENT_Init(disConnect, null);
        if (!bInit) {
            log.error("Initialize SDK failed");
            return;
        }

        NetSDKLib.LOG_SET_PRINT_INFO setLog = new NetSDKLib.LOG_SET_PRINT_INFO();
        File path = new File("./sdklog/");
        if (!path.exists()) {
            boolean mkdir = path.mkdir();
        }
        String logPath = path.getAbsoluteFile().getParent() + "\\sdklog\\" + ToolKits.getDate() + ".log";

        setLog.nPrintStrategy = 0;
        setLog.bSetFilePath = 1;
        System.arraycopy(logPath.getBytes(), 0, setLog.szLogFilePath, 0, logPath.getBytes().length);
        setLog.bSetPrintStrategy = 1;
        boolean isLogOpen = netsdk.CLIENT_LogOpen(setLog);

        if (!isLogOpen) {
            log.error("Failed to open NetSDK log");
        }

        netsdk.CLIENT_SetAutoReconnect(haveReConnect, null);

        int waitTime = 5000;
        int tryTimes = 1;
        netsdk.CLIENT_SetConnectTime(waitTime, tryTimes);

        NetSDKLib.NET_PARAM netParam = new NetSDKLib.NET_PARAM();
        netParam.nConnectTime = 10000;
        netParam.nGetConnInfoTime = 3000;
        netParam.nGetDevInfoTime = 3000;
        netsdk.CLIENT_SetNetworkParam(netParam);
    }

    public void cleanSdk() {
        netsdk.CLIENT_Cleanup();
        log.info("SDK resources cleaned up.");
    }

    public void logout(NetSDKLib.LLong m_loginHandle) {
        if (m_loginHandle.longValue() != 0) {
            netsdk.CLIENT_Logout(m_loginHandle);
        }
    }

    public void startServer(String address, int port) {
        serverHanle = netsdk.CLIENT_ListenServer(address, port, 1000, callback, null);

        if (0 == serverHanle.longValue()) {
            log.error("Failed to start server. {}",  ToolKits.getErrorCodePrint());
        } else {
            log.info("Start server, [Server address {}][Server port {}]\n", address, port);
        }
        serverHanle.longValue();
    }

    public boolean stopServer() {
        boolean bRet = false;

        if (serverHanle.longValue() != 0) {
            bRet = LoginModule.netsdk.CLIENT_StopListenServer(serverHanle);
            serverHanle.setValue(0);
            log.info("Stop server!");
        }

        return bRet;
    }
}
