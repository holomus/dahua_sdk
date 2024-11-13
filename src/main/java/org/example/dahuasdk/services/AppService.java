package org.example.dahuasdk.services;

import org.example.dahuasdk.dto.MiddlewareDTO;
import org.example.dahuasdk.entity.Device;

import java.util.List;

public interface AppService {
    void createMiddleware(MiddlewareDTO middlewareDTO);

    List<MiddlewareDTO> getMiddlewares();

    MiddlewareDTO getMiddleware(long id);

    void updateMiddleware(MiddlewareDTO middlewareDTO);

    void deleteMiddleware(long id);

    void createDevice(
            long vhrDeviceId,
            long middlewareId,
            String login,
            String password,
            String deviceId,
            String deviceName);

    List<Device> getDevices(long clientId);

    void updateDevice(long id, String deviceIndex, long vhrId);

    void deleteDevice(long id);

    void deleteDeviceByMiddlewareIdAndVhrId(long middlewareId, long vhrId);

    Device findDeviceByMiddlewareIdAndVhrId(long middlewareId, long vhrId);
}
