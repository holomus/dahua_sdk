package org.example.dahuasdk.services;

import com.netsdk.demo.module.LoginModule;
import com.netsdk.lib.NetSDKLib;
import com.netsdk.lib.ToolKits;
import com.sun.jna.Pointer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dahuasdk.dto.DeviceConnectionDTO;
import org.example.dahuasdk.dto.DeviceLoginInfoDto;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

@Slf4j
@Service

public class AutoRegisterService {
    final private NetSDKLib netsdk = NetSDKLib.NETSDK_INSTANCE;
    public NetSDKLib.LLong serverHanle;
    public static NetSDKLib.NET_DEVICEINFO_Ex m_stDeviceInfo = new NetSDKLib.NET_DEVICEINFO_Ex();
    private HashMap<String, DeviceConnectionDTO> deviceConnectionInfo = new HashMap<>();


    public AutoRegisterService() {
        this.serverHanle = new NetSDKLib.LLong(0);
        this.callback = new ServiceCB();
        this.deviceConnectionInfo = new HashMap<>();
    }

    public class ServiceCB implements NetSDKLib.fServiceCallBack {
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
                    System.out.println("disconnected");
                    break;
                }
                case NetSDKLib.EM_LISTEN_TYPE.NET_DVR_SERIAL_RETURN: {
                    DeviceConnectionDTO deviceInfo = new DeviceConnectionDTO();
                    deviceInfo.setIp(pIp.trim());
                    deviceInfo.setPort(wPort);


                    System.out.println("ip = " + pIp.trim() + " port + " + wPort + " device_id = " + deviceId);
                    deviceConnectionInfo.put(deviceId, deviceInfo);

                    new SwingWorker<Boolean, String>() {
                        @Override
                        protected Boolean doInBackground() {
                            System.out.println("Connected");
                            return true;
                        }

                        @Override
                        protected void done() {
                            try {
                                if (get()) {
                                    System.out.println("(done) Connected");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
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

    public ServiceCB callback = new ServiceCB();


    public NetSDKLib.LLong login(
            String m_strUser,
            String m_strPassword,
            String deviceIds
    ) {
        DeviceConnectionDTO deviceConnectionDTO = deviceConnectionInfo.get(deviceIds);
        Pointer deviceId = ToolKits.GetGBKStringToPointer(deviceIds);
        String m_strIp = deviceConnectionDTO.getIp();
        int m_nPort= deviceConnectionDTO.getPort();

        NetSDKLib.NET_IN_LOGIN_WITH_HIGHLEVEL_SECURITY pstInParam = new NetSDKLib.NET_IN_LOGIN_WITH_HIGHLEVEL_SECURITY();

        pstInParam.nPort = m_nPort;
        pstInParam.szIP = m_strIp.getBytes();
        pstInParam.szPassword = m_strPassword.getBytes();
        pstInParam.szUserName = m_strUser.getBytes();
        pstInParam.emSpecCap = 2;
        pstInParam.pCapParam=deviceId;

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
