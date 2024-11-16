package org.example.dahuasdk.services;

import com.netsdk.demo.module.LoginModule;
import com.netsdk.lib.NetSDKLib;
import com.netsdk.lib.ToolKits;
import com.sun.jna.Pointer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dahuasdk.DahuaSdkApplication;
import org.example.dahuasdk.dao.AppDAO;
import org.example.dahuasdk.dto.DeviceConnectionDTO;
import org.example.dahuasdk.entity.Device;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutoRegisterService {
    static final private NetSDKLib netsdk = NetSDKLib.NETSDK_INSTANCE;
    private final AppService appService;
    private final AppDAO dao;
    private final EventListenerService eventReceiver;
    private final EventLoaderService eventLoader;

    private class DisConnect implements NetSDKLib.fDisConnect {
        public void invoke(NetSDKLib.LLong m_hLoginHandle, String pchDVRIP, int nDVRPort, Pointer dwUser) {
            System.out.printf("Device[%s] Port[%d] LoginHandle[%d], DisConnect!\n", pchDVRIP, nDVRPort, m_hLoginHandle.longValue());

            for (Map.Entry<String, DeviceConnectionDTO> entry : deviceConnectionInfo.entrySet()) {
                String key = entry.getKey();
                DeviceConnectionDTO value = entry.getValue();

                if (Objects.equals(m_hLoginHandle, value.getLoginHandle())) {
                    value.setStatus("F");
                    deviceConnectionInfo.put(key, value);
                    break;
                }
            }
        }
    }

    private static class HaveReConnect implements NetSDKLib.fHaveReConnect {
        public void invoke(NetSDKLib.LLong m_hLoginHandle, String pchDVRIP, int nDVRPort, Pointer dwUser) {
            System.out.printf("ReConnect Device[%s] Port[%d]\n", pchDVRIP, nDVRPort);
        }
    }

    public class ServiceCB implements NetSDKLib.fServiceCallBack {
        private record DeviceWithHandle(Device device, LocalDateTime lastOnlineTime, NetSDKLib.LLong loginHandle) {}

        private void handleSerialReturnEvent(String pIp, int wPort, String deviceId) {
            CompletableFuture.supplyAsync(() -> {
                Device device = appService.findDeviceByDeviceId(deviceId);

                if (device == null) {
                    throw new IllegalArgumentException("Device not found for deviceId: " + deviceId);
                }

                var lastOnlineTime = device.getLastOnlineTime();

                log.info("Connecting to device: {}", deviceId);

                NetSDKLib.LLong loginHandle = DahuaSdkApplication.autoRegisterService.login(
                        device.getLogin(),
                        device.getPassword(),
                        device.getDeviceId(),
                        pIp.trim(),
                        wPort
                );

                if (loginHandle.longValue() == 0) {
                    throw new IllegalStateException("Failed to login to device: " + deviceId);
                }

                log.info("Login successful for deviceId: {} (IP: {}, Port: {})", deviceId, pIp.trim(), wPort);

                return new DeviceWithHandle(device, device.getLastOnlineTime(), loginHandle);
            }).thenApply(deviceWithHandle -> {
                // Update device connection info
                DeviceConnectionDTO deviceInfo = new DeviceConnectionDTO();
                deviceInfo.setLoginHandle(deviceWithHandle.loginHandle);
                deviceInfo.setStatus("O");

                // TODO: Safely update the shared map
                deviceConnectionInfo.put(deviceId, deviceInfo);

                return deviceWithHandle;
            }).thenApply(deviceWithHandle -> {
                eventReceiver.eventListeningStart(netsdk, deviceWithHandle.loginHandle, deviceId);

                return deviceWithHandle;
            }).thenApply(deviceWithHandle -> {
                deviceWithHandle.device.setLastOnlineTime(LocalDateTime.now());
                dao.saveDevice(deviceWithHandle.device);

                return deviceWithHandle;
            }).thenApply(deviceWithHandle -> {
                eventLoader.loadAccessRecords(netsdk, deviceWithHandle.loginHandle, deviceId, deviceWithHandle.lastOnlineTime, LocalDateTime.now());

                return deviceWithHandle;
            }).exceptionally(ex -> {
                log.error("Error during device handling for deviceId: {}", deviceId, ex);
                return null;
            });
        }


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
                e.printStackTrace();
            }

            switch (lCommand) {
                case NetSDKLib.EM_LISTEN_TYPE.NET_DVR_DISCONNECT: {
                    System.err.println("disconnected");
                    break;
                }
                case NetSDKLib.EM_LISTEN_TYPE.NET_DVR_SERIAL_RETURN: {
                    handleSerialReturnEvent(pIp, wPort, deviceId);
                    break;
                }
                default:
                    break;
            }

            return 0;
        }
    }

    // ---------------------  variables --------------------- //
    public NetSDKLib.LLong serverHanle;
    public static NetSDKLib.NET_DEVICEINFO_Ex m_stDeviceInfo = new NetSDKLib.NET_DEVICEINFO_Ex();
    public HashMap<String, DeviceConnectionDTO> deviceConnectionInfo = new HashMap<>();
    private static boolean bInit = false;
    private static boolean bLogopen = false;
    public final ServiceCB callback = new ServiceCB();
    public final DisConnect disConnect = new DisConnect();
    public final HaveReConnect haveReConnect = new HaveReConnect();

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

        NetSDKLib.LLong m_hLoginHandle = LoginModule.netsdk.CLIENT_LoginWithHighLevelSecurity(pstInParam, pstOutParam);
        return m_hLoginHandle;
    }

    public static void initSDK() {
        boolean result = NetSDKLib.NETSDK_INSTANCE.CLIENT_Init(null, null);
        if (!result) {
            System.out.println("SDK initialization failed");
        } else {
            System.out.println("SDK initialized successfully");
        }
    }

    public static void init(NetSDKLib.fDisConnect disConnect, NetSDKLib.fHaveReConnect haveReConnect) {
        bInit = netsdk.CLIENT_Init(disConnect, null);
        if (!bInit) {
            System.out.println("Initialize SDK failed");
            return;
        }

        NetSDKLib.LOG_SET_PRINT_INFO setLog = new NetSDKLib.LOG_SET_PRINT_INFO();
        File path = new File("./sdklog/");
        if (!path.exists()) {
            path.mkdir();
        }
        String logPath = path.getAbsoluteFile().getParent() + "\\sdklog\\" + ToolKits.getDate() + ".log";

        setLog.nPrintStrategy = 0;
        setLog.bSetFilePath = 1;
        System.arraycopy(logPath.getBytes(), 0, setLog.szLogFilePath, 0, logPath.getBytes().length);
        System.out.println(logPath);
        setLog.bSetPrintStrategy = 1;
        bLogopen = netsdk.CLIENT_LogOpen(setLog);
        if (!bLogopen) {
            System.err.println("Failed to open NetSDK log");
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
        System.out.println("SDK resources cleaned up.");
    }

    public void logout(NetSDKLib.LLong m_loginHandle) {
        if (m_loginHandle.longValue() != 0) {
            netsdk.CLIENT_Logout(m_loginHandle);
        }
    }

    public void startServer(String address, int port, NetSDKLib.fServiceCallBack callback) {
        serverHanle = netsdk.CLIENT_ListenServer(address, port, 1000, callback, null);

        if (0 == serverHanle.longValue()) {
            System.err.println("Failed to start server." + ToolKits.getErrorCodePrint());
        } else {
            System.out.printf("Start server, [Server address %s][Server port %d]\n", address, port);
        }
        serverHanle.longValue();
    }

    public boolean stopServer() {
        boolean bRet = false;

        if (serverHanle.longValue() != 0) {
            bRet = LoginModule.netsdk.CLIENT_StopListenServer(serverHanle);
            serverHanle.setValue(0);
            System.out.println("Stop server!");
        }

        return bRet;
    }
}
