package org.example.dahuasdk.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class WebhookDTO {
    private String type;
    private long timestamp;
    private WebhookData data;

    @Override
    public String toString() {
        return "WebhookDTO{" +
                "type='" + type + '\'' +
                ", timestamp=" + timestamp +
                ", data=" + data +
                '}';
    }
}