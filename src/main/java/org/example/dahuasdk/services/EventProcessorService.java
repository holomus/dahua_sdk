package org.example.dahuasdk.services;

import lombok.RequiredArgsConstructor;
import org.example.dahuasdk.client.vhr.VHRClient;
import org.example.dahuasdk.dto.EventDTO;
import org.example.dahuasdk.entity.Middleware;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Service
public class EventProcessorService {
    private final VHRClient vhrClient;

    public void processEvent(EventDTO event) {
        vhrClient.sendEvents(Middleware.builder().build(), List.of(event));
    }

    public void processEvents(List<EventDTO> events) {
        for (var event: events) {
            processEvent(event);
        }
    }
}
