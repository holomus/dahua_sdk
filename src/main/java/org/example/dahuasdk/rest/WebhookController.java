package org.example.dahuasdk.rest;

import lombok.RequiredArgsConstructor;
import org.example.dahuasdk.core.MiddlewareEngine;
import org.example.dahuasdk.dto.WebhookDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/webhook/v1")
public class WebhookController {
    private final MiddlewareEngine engine;

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleMissingRequestHeaderException(MissingRequestHeaderException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .header("Content-Type", "application/json")
                .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), e.getMessage()));
    }

    @PostMapping("/device")
    public String notifyDeviceChange(@RequestHeader(name = "Token") String token, @RequestBody WebhookDTO webhookDTO) {
        engine.addDeviceWorker(token, webhookDTO.getData().getDeviceId());
        return "OK";
    }

    public record ErrorResponse(int code, String description) {
    }
}