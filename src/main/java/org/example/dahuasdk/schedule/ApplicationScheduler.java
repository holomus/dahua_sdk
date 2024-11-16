
package org.example.dahuasdk.schedule;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.*;
import org.example.dahuasdk.client.vhr.VHRClient;
import org.example.dahuasdk.config.VhrProperties;
import org.example.dahuasdk.dao.AppDAO;
import org.example.dahuasdk.dto.DeviceConnectionDTO;
import org.example.dahuasdk.entity.Device;
import org.example.dahuasdk.entity.Middleware;
import org.example.dahuasdk.services.DeviceConnectionInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
public class ApplicationScheduler {
    private static final Logger log = LoggerFactory.getLogger(ApplicationScheduler.class);
    private final AppDAO dao;
    private final VhrProperties vhrProperties;
    private final VHRClient vhrClient;
    private ScheduledExecutorService scheduledExecutorService;
    private final DeviceConnectionInfoService deviceConnectionInfoService = DeviceConnectionInfoService.getInstance();

    @PostConstruct
    public void schedule() {
        try {
            scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutorService.scheduleWithFixedDelay(this::run, 0, vhrProperties.getJobInterval(), TimeUnit.SECONDS);
            log.info("Application jobs stated with initial delay of {} seconds and delay of {} seconds", 0, vhrProperties.getJobInterval());
        } catch (Exception e) {
            log.error("Error occurred while scheduling application jobs", e);
        }
    }

    @PreDestroy
    public void destroy() {
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
            log.info("Application jobs stopped");
        }
    }

    private void run() {
        try {
            List<Middleware> middlewares = dao.findAllMiddleware();
            middlewares.forEach(middleware -> {
                List<Device> deviceInfos = dao.findAllDevicesByMiddlewareId(middleware.getId());
                vhrClient.sendHealthCheck(middleware);
                sendDeviceStatuses(deviceInfos, middleware);
            });
        } catch (Exception e) {
            log.error("Health check job failed", e);
        }
    }

    private void sendDeviceStatuses(List<Device> deviceInfos, Middleware middleware) {
        DeviceStatuses deviceStatuses = prepareDeviceStatuses(deviceInfos, middleware);
        vhrClient.sendDeviceStatuses(middleware, deviceStatuses);
    }

    private DeviceStatuses prepareDeviceStatuses(List<Device> deviceStatuses, Middleware middleware) {
        List<Device> devices = dao.findAllDevicesByMiddlewareId(middleware.getId());
        DeviceStatuses devicesInfo = new DeviceStatuses();
        ConcurrentHashMap<String, DeviceConnectionDTO> deviceConnectionInfo = deviceConnectionInfoService.getData();

        for (Device device : devices) {
            devicesInfo.addDeviceInfo(device.getVhrDeviceId(),
                    deviceConnectionInfo.containsKey(
                            device.getDeviceId())
                            ? deviceConnectionInfo.get(device.getDeviceId()).getStatus()
                            : "U");
        }

        return devicesInfo;
    }

    private record DeviceStatus(@JsonProperty("device_id") long deviceId, @JsonProperty("status") String status) {
    }

    @AllArgsConstructor
    @Getter
    @Setter
    public static class DeviceStatuses {
        @JsonProperty("devices")
        private List<DeviceStatus> devicesStatuses;

        public DeviceStatuses() {
            devicesStatuses = new ArrayList<>();
        }

        void addDeviceInfo(long deviceId, String status) {
            if (devicesStatuses == null)
                devicesStatuses = new ArrayList<>();

            devicesStatuses.add(new DeviceStatus(deviceId, status));
        }
    }
}
