package org.example.dahuasdk.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.dahuasdk.dao.AppDAO;
import org.example.dahuasdk.dto.DTOUtil;
import org.example.dahuasdk.dto.MiddlewareDTO;
import org.example.dahuasdk.entity.Device;
import org.example.dahuasdk.entity.Middleware;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AppServiceImpl implements AppService {
    private final AppDAO dao;
    private final DTOUtil util;

    @Override
    @Transactional
    public void createMiddleware(MiddlewareDTO middlewareDTO) {
        dao.createMiddleware(util.toMiddlewareEntity(middlewareDTO));
    }

    @Override
    public List<MiddlewareDTO> getMiddlewares() {
        return dao.findAllMiddleware().stream()
                .map(util::toMiddlewareDTO)
                .toList();
    }

    @Override
    public MiddlewareDTO getMiddleware(long id) {
        return util.toMiddlewareDTO(dao.findMiddlewareById(id));
    }

    @Override
    @Transactional
    public void updateMiddleware(MiddlewareDTO middlewareDTO) {
        dao.updateMiddleware(util.toMiddlewareEntity(middlewareDTO));
    }

    @Override
    @Transactional
    public void deleteMiddleware(long id) {
        dao.deleteMiddlewareById(id);
    }

    @Override
    @Transactional
    public void createDevice(
            long vhrDeviceId,
            long middlewareId,
            String login,
            String password,
            String deviceId,
            String deviceName
    ) {
        Middleware middlewareRecord = dao.findMiddlewareById(middlewareId);
        Device device = new Device();

        device.setVhrDeviceId(vhrDeviceId);
        device.setVhrDeviceId(vhrDeviceId);
        device.setLogin(login);
        device.setPassword(password);
        device.setDeviceId(deviceId);
        device.setDeviceName(deviceName);

        device.setMiddleware(middlewareRecord);
        dao.createDevice(device);
    }

    @Override
    public List<Device> getDevices(long clientId) {
        return dao.findAllDevicesByMiddlewareId(clientId);
    }

    @Override
    @Transactional
    public void updateDevice(long id, String deviceIndex, long vhrDeviceId) {
        Device device = dao.findDeviceById(id);
        device.setVhrDeviceId(vhrDeviceId);
        dao.updateDevice(device);
    }

    @Override
    @Transactional
    public void deleteDevice(long id) {
        dao.deleteDeviceById(id);
    }

    @Override
    @Transactional
    public void deleteDeviceByMiddlewareIdAndVhrId(long middlewareId, long vhrId) {
        dao.deleteDeviceByMiddlewareIdAndVhrId(middlewareId, vhrId);
    }

    @Override
    public Device findDeviceByMiddlewareIdAndVhrId(long middlewareId, long vhrId) {
        return dao.findDeviceByMiddlewareIdAndVhrId(middlewareId, vhrId);
    }
}
