package org.example.dahuasdk.client.vhr.entity.load;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class Command {
    @JsonProperty("command_id")
    private String commandId;
    @JsonProperty("command_code")
    private String commandCode;
    @JsonProperty("delay")
    private Integer delay;
    @JsonProperty("command_body")
    private Object commandBody;
}
