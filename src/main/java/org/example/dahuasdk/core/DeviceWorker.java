package org.example.dahuasdk.core;

import com.netsdk.lib.NetSDKLib;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.example.dahuasdk.DahuaSdkApplication;
import org.example.dahuasdk.client.vhr.VHRClient;
import org.example.dahuasdk.client.vhr.entity.load.Commands;
import org.example.dahuasdk.dao.AppDAO;
import org.example.dahuasdk.entity.Device;
import org.example.dahuasdk.entity.Middleware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Setter
@Component
@Scope("prototype")

public class DeviceWorker implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(DeviceWorker.class);
    private final MiddlewareEngine middlewareEngine;
    private final VHRClient vhrClient;
    private final AppDAO appDAO;
    private final CommandExecutor commandExecutor;

    private Middleware middleware;
    private long deviceId;

    @Override
    public void run() {
        try {
            Device device = appDAO.findDeviceByMiddlewareIdAndVhrId(middleware.getId(), deviceId);
//
            NetSDKLib.LLong loginHandle = DahuaSdkApplication.autoRegisterService.login(
                    device.getLogin(),
                    device.getPassword(),
                    device.getDeviceId()
            );

            commandExecutor.setMiddleware(middleware);
            commandExecutor.setDeviceId(deviceId);
            commandExecutor.setLoginHandle(loginHandle);

            long count = 10;
            while (true && count > 0) {
                // load commands from VHR
                Commands commands = vhrClient.loadCommands(middleware, deviceId);

                // execute commands, return if failed
                if (!commandExecutor.executeCommands(commands)) {
                    DahuaSdkApplication.autoRegisterService.logout(loginHandle);
                    return;
                }
                count --;
            }

        } catch (Exception e) {
            log.error("Error occurred while executing commands for device, middlewareId: {}, deviceId: {}", middleware.getId(), deviceId, e);
        } finally {
            middlewareEngine.removeDeviceWorker(middleware.getId(), deviceId);
        }
    }
}