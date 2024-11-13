package org.example.dahuasdk.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class WebhookData {
    private long id;
    @JsonProperty("device_id")
    private long deviceId;

    @Override
    public String toString() {
        return "WebhookData{" +
                "id=" + id +
                ", deviceId=" + deviceId +
                '}';
    }
}