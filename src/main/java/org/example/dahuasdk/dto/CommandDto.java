package org.example.dahuasdk.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class CommandDto {
    private String command_id;
    private String command_code;
    private Long delay;
    private CommandBodyDto command_body;
}
