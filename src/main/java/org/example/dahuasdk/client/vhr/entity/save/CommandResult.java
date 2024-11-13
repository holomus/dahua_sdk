package org.example.dahuasdk.client.vhr.entity.save;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
public class CommandResult {
    @JsonProperty("command_id")
    private final String commandId;
    @JsonProperty("command_result")
    private CommandResultData commandResult;
}