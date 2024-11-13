package org.example.dahuasdk.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.example.dahuasdk.entity.Device;
import org.example.dahuasdk.entity.Middleware;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class AppDAOImpl implements AppDAO {
    private final EntityManager entityManager;

    @Override
    public void createMiddleware(Middleware middleware) {
        entityManager.persist(middleware);
    }

    @Override
    public List<Middleware> findAllMiddleware() {
        TypedQuery<Middleware> query = entityManager.createQuery("FROM Middleware", Middleware.class);
        return query.getResultList();
    }

    @Override
    public Middleware findMiddlewareById(long id) {
        return entityManager.find(Middleware.class, id);
    }

    @Override
    public Middleware findMiddlewareByToken(String token) {
        TypedQuery<Middleware> query = entityManager.createQuery("FROM Middleware m WHERE m.token = :token", Middleware.class);
        query.setParameter("token", token);
        return query.getSingleResult();
    }

    @Override
    public void updateMiddleware(Middleware middleware) {
        Middleware middlewareRecord = findMiddlewareById(middleware.getId());
        middlewareRecord.setHost(middleware.getHost());
        middlewareRecord.setToken(middleware.getToken());
        middlewareRecord.setCredentials(middleware.getCredentials());
        entityManager.merge(middlewareRecord);
    }

    @Override
    public void deleteMiddlewareById(long id) {
        Middleware middleware = findMiddlewareById(id);
        entityManager.remove(middleware);
    }

    @Override
    public void createDevice(Device device) {
        entityManager.persist(device);
    }

    @Override
    public List<Device> findAllDevicesByMiddlewareId(long middlewareId) {
        TypedQuery<Device> query = entityManager.createQuery("FROM Device WHERE middleware.id = :id", Device.class);
        query.setParameter("id", middlewareId);
        return query.getResultList();
    }

    @Override
    public Device findDeviceById(long id) {
        return entityManager.find(Device.class, id);
    }

    @Override
    public void updateDevice(Device device) {
        entityManager.merge(device);
    }

    @Override
    public void deleteDeviceById(long id) {
        Device device = findDeviceById(id);
        entityManager.remove(device);
    }

    @Override
    public void deleteDeviceByMiddlewareIdAndVhrId(long middlewareId, long vhrDeviceId) {
        TypedQuery<Device> query = entityManager.createQuery("FROM Device WHERE middleware.id = :middlewareId AND vhrDeviceId = :vhrDeviceId", Device.class);
        query.setParameter("middlewareId", middlewareId);
        query.setParameter("vhrDeviceId", vhrDeviceId);
        Device device = query.getSingleResult();
        entityManager.remove(device);
    }

    @Override
    public Device findDeviceByMiddlewareIdAndVhrId(long middlewareId, long vhrDeviceId) {
        TypedQuery<Device> query = entityManager.createQuery("FROM Device WHERE middleware.id = :middlewareId AND vhrDeviceId = :vhrDeviceId", Device.class);
        query.setParameter("middlewareId", middlewareId);
        query.setParameter("vhrDeviceId", vhrDeviceId);

        return query.getSingleResult();
    }
}
