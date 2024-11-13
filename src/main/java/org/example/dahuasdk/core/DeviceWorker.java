package org.example.dahuasdk.core;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.example.dahuasdk.client.vhr.VHRClient;
import org.example.dahuasdk.client.vhr.entity.load.Commands;
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
    private final CommandExecutor commandExecutor;

    private Middleware middleware;
    private long deviceVhrId;

    @Override
    public void run() {
        try {
            commandExecutor.setMiddleware(middleware);
            commandExecutor.setDeviceVhrId(deviceVhrId);

            while (true) {
                Commands commands = vhrClient.loadCommands(middleware, deviceVhrId);

                if (!commandExecutor.executeCommands(commands)) {
                    return;
                }
            }
        } catch (Exception e) {
            log.error("Error occurred while executing commands for device, middlewareId: {}, deviceId: {}", middleware.getId(), deviceVhrId, e);
        } finally {
            middlewareEngine.removeDeviceWorker(middleware.getId(), deviceVhrId);
        }
    }
}