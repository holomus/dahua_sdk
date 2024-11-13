package org.example.dahuasdk.client.vhr.entity.save;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter

public class CommandResultData {
    @JsonProperty("status_code")
    private int statusCode;

    @JsonProperty("message")
    private String message;

    @JsonProperty("fail_codes")
    private List<Integer> failCodes;

    @JsonProperty("fail_messages")
    private List<Integer> failMessages;

    @JsonIgnore
    public boolean isPossibleInternalOrDeviceBusyError() {
        return statusCode == 403 || statusCode == 503;
    }
}
