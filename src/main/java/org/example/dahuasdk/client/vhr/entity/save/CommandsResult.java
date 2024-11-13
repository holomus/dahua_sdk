package org.example.dahuasdk.client.vhr.entity.save;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CommandsResult {
    @JsonProperty("commands_list")
    private List<CommandResult> commands;

    public CommandsResult(CommandResult result) {
        commands = new ArrayList<>();
        commands.add(result);
    }

    public void addCommand(CommandResult command) {
        if (commands == null)
            commands = new ArrayList<>();

        commands.add(command);
    }
}