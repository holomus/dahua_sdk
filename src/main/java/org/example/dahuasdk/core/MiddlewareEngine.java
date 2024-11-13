package org.example.dahuasdk.core;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.example.dahuasdk.dao.AppDAO;
import org.example.dahuasdk.entity.Middleware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@RequiredArgsConstructor
@Component
public class MiddlewareEngine {
    private static final Logger log = LoggerFactory.getLogger(MiddlewareEngine.class);
    private final ApplicationContext context;
    private final AppDAO dao;

    private Set<String> deviceWorkers;
    private ExecutorService deviceExecutorService;

    @PostConstruct
    public void init() {
        deviceWorkers = Collections.synchronizedSet(new HashSet<>());
        deviceExecutorService = Executors.newVirtualThreadPerTaskExecutor();
    }

    public void addDeviceWorker(String token, long deviceId) {
        Middleware middleware;

        System.out.println(token + ' ' + deviceId);

        try {
            middleware = dao.findMiddlewareByToken(token);
        } catch (Exception e) {
            log.warn("Invalid middleware token: {}", token);
            return;
        }

        try {
            if (deviceId == 0) {
                log.warn("Invalid middleware device id. middlewareId: {}, deviceId: {}", middleware.getId(), deviceId);
                return;
            }

            String workerKey = middleware.getId() + "_" + deviceId;

            // Ignore if DeviceWorker already exists
            if (deviceWorkers.contains(workerKey)) return;
            else deviceWorkers.add(workerKey);

            // Start DeviceWorker
            DeviceWorker deviceWorker = context.getBean(DeviceWorker.class);
            deviceWorker.setMiddleware(middleware);
            deviceWorker.setDeviceId(deviceId);

            deviceExecutorService.submit(deviceWorker);
        } catch (Exception e) {
            log.error("Error while adding DeviceWorker with middlewareId: {} and deviceId: {}", middleware.getId(), deviceId, e);
            removeDeviceWorker(middleware.getId(), deviceId);
        }
    }

    public void removeDeviceWorker(long middlewareId, long deviceId) {
        String workerKey = middlewareId + "_" + deviceId;
        deviceWorkers.remove(workerKey);
    }
}