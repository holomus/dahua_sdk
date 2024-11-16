package org.example.dahuasdk.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netsdk.lib.NetSDKLib;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.example.dahuasdk.client.vhr.VHRClient;
import org.example.dahuasdk.client.vhr.entity.load.*;
import org.example.dahuasdk.client.vhr.entity.save.CommandResult;
import org.example.dahuasdk.client.vhr.entity.save.CommandResultData;
import org.example.dahuasdk.client.vhr.entity.save.CommandsResult;
import org.example.dahuasdk.dao.AppDAO;
import org.example.dahuasdk.entity.Device;
import org.example.dahuasdk.entity.Middleware;
import org.example.dahuasdk.services.AppService;
import org.example.dahuasdk.handlemanagers.DeviceLoginHandleManager;
import org.example.dahuasdk.services.PersonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

@RequiredArgsConstructor
@Setter
@Component
@Scope("prototype")

public class CommandExecutor {
    private static final Logger log = LoggerFactory.getLogger(CommandExecutor.class);
    private final DeviceLoginHandleManager deviceConnectionInfoService;
    private final AppService appService;
    private final VHRClient vhrClient;
    private final ObjectMapper objectMapper;
    private ExecutorService executor;
    private PersonService personService = new PersonService();
    private NetSDKLib.LLong loginHandle;
    private Middleware middleware;
    private final AppDAO appDAO;
    private long deviceVhrId;

    @PostConstruct
    public void init() {
        executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    @PreDestroy
    public void destroy() {
        executor.shutdown();
    }

    public boolean executeCommands(Commands commands) {
        if (commands.getCommands() == null || commands.getCommands().isEmpty()) return false;

        if (appDAO.existsDeviceByMiddlewareIdAndVhrId(middleware.getId(), deviceVhrId)) {
            Device device = appDAO.findDeviceByMiddlewareIdAndVhrId(middleware.getId(), deviceVhrId);

            loginHandle = deviceConnectionInfoService.get(device.getDeviceId()).getLoginHandle();
        }

        executeCommandsConcurrently(commands.getCommands());
        return true;
    }

    public CommandResult executeCommand(Command command) {
        try {
            List<Integer> failcodes = new ArrayList<>();

            switch (command.getCommandCode()) {
                case "dahua:device:set_up":
                    addDevice(command.getCommandBody());
                    break;
                case "dahua:device:remove":
                    removeDevice();
                    break;
                case "dahua:person:set_up":
                    failcodes = addPerson(command.getCommandBody());
                    break;
                case "dahua:person:remove":
                    failcodes = removePerson(command.getCommandBody());
                    break;
                case "dahua:person:add_photo":
                    failcodes = setPhoto(command.getCommandBody(), NetSDKLib.NET_EM_ACCESS_CTL_FACE_SERVICE.NET_EM_ACCESS_CTL_FACE_SERVICE_INSERT);
                    break;
                case "dahua:person:edit_photo":
                    failcodes = setPhoto(command.getCommandBody(), NetSDKLib.NET_EM_ACCESS_CTL_FACE_SERVICE.NET_EM_ACCESS_CTL_FACE_SERVICE_UPDATE);
                    break;
                case "dahua:person:remove_photo":
                    failcodes = setPhoto(command.getCommandBody(), NetSDKLib.NET_EM_ACCESS_CTL_FACE_SERVICE.NET_EM_ACCESS_CTL_FACE_SERVICE_REMOVE);
                    break;
                default:
                    log.error("Invalid command code mode: {}", command.getCommandCode());
            }

            return new CommandResult(command.getCommandId(), getCommandData(failcodes));
        } catch (Exception e) {
            log.error("Error while execute command. command_id = {}, message = {}", command.getCommandId(), e.getMessage());
            return makeErrorResult(command.getCommandId(), e.getMessage());
        }
    }

    public CommandResult makeErrorResult(String commandId, String errorMessage) {
        CommandResult commandResult =  new CommandResult(commandId);
        CommandResultData commandResultData = new CommandResultData();
        commandResultData.setStatusCode(400);
        commandResultData.setMessage(errorMessage);

        return commandResult;
    }

    public static String getFailCodeMessage(int code) {
        return switch (code) {
            case 0 -> "No errors";
            case 2 -> "Parameter error";
            case 3 -> "Invalid password";
            case 4 -> "Invalid message data";
            case 5 -> "Invalid face data";
            case 6 -> "Invalid card data";
            case 7 -> "Invalid person data";
            case 11 -> "Insert limit reached";
            case 12 -> "Maximum insert speed reached";
            case 13 -> "Failed to clear information data";
            case 14 -> "Failed to clear face data";
            case 15 -> "Failed to clear card data";
            case 19 -> "Exceeded the maximum number of personal information records";
            case 20 -> "Exceeded personal maximum number of card records";
            case 21 -> "Maximum photo size exceeded";
            case 22 -> "Invalid user ID (customer not found)";
            case 24 -> "Face photo already exists";
            case 25 -> "Maximum number of face photos exceeded";
            case 26 -> "Invalid photo format";
            case 27 -> "Exceeded the limit on number of administrators";
            case 35 -> "Picture quality is too low";
            default -> "Unknown error";
        };
    }

    private CommandResultData getCommandData(List<Integer> failcodes) {
        CommandResultData commandResultData = new CommandResultData();
        commandResultData.setStatusCode(200);
        List<String> failCodeMessages = new ArrayList<>();

        for (Integer failcode : failcodes) {
            if (failcode != 0) {
                commandResultData.setStatusCode(400);
            }

            failCodeMessages.add(getFailCodeMessage(failcode));
        }

        commandResultData.setFailCodes(failcodes);
        commandResultData.setFailMessages(failCodeMessages);

        return commandResultData;
    }

    private void executeCommandsConcurrently(List<Command> commands) {
        CompletionService<CommandResult> completionService = new ExecutorCompletionService<>(executor);
        CountDownLatch latch = new CountDownLatch(commands.size());

        commands.forEach(command -> completionService.submit(() -> {
            try {
                CommandResult commandResult = executeCommand(command);
                vhrClient.saveCommands(middleware, new CommandsResult(commandResult));

                return null;
            } finally {
                latch.countDown();
            }
        }));

        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("Error occurred while waiting for command completion", e);
            Thread.currentThread().interrupt();
        }
    }

    private void addDevice(Object commandBody) {
        DeviceDTO device = objectMapper.convertValue(commandBody, DeviceDTO.class);

        try {
            appService.createDevice(
                    deviceVhrId,
                    middleware.getId(),
                    device.getLogin(),
                    device.getPassword(),
                    device.getDeviceId(),
                    device.getDeviceName()
            );
        } catch (Exception e) {
            log.error("Error occurred while adding device to middleware, middlewareId: {}, deviceId: {}", middleware.getId(), deviceVhrId, e);
        }
    }

    private void removeDevice() {
        try {
            appService.deleteDeviceByMiddlewareIdAndVhrId(middleware.getId(), deviceVhrId);
        } catch (Exception e) {
            log.error("Error occurred while removing device from middleware, middlewareId: {}, deviceId: {}", middleware.getId(), deviceVhrId, e);
        }
    }

    private List<Integer> addPerson(Object commandBody) throws Exception {
        PersonDTO person = objectMapper.convertValue(commandBody, PersonDTO.class);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        try {
            person.setStuValidBeginTime(dateFormat.parse(person.getStuValidBeginTimeStr()));
            person.setStuValidEndTime(dateFormat.parse(person.getStuValidEndTimeStr()));
        } catch (Exception e) {
            throw new Exception("Person stu valid date formats is invalid", e);
        }

        return personService.savePerson(List.of(person), loginHandle);
    }

    private record UserId(@JsonProperty("user_id") String user_id) {
    }

    private List<Integer> removePerson(Object commandBody) throws Exception {
        UserId userId = objectMapper.convertValue(commandBody, UserId.class);

        return personService.removePerson(new String[]{userId.user_id}, loginHandle);
    }

    private List<Integer> setPhoto(Object commandBody, int emtype) throws Exception {
        PhotoDTO photoDTO = objectMapper.convertValue(commandBody, PhotoDTO.class);
        Photo photo = vhrClient.loadPhoto(middleware, photoDTO.getFaceImage());

        List<PersonFaceUpdateDTO> personFaces = new ArrayList<>();
        PersonFaceUpdateDTO personFaceUpdateDto = new PersonFaceUpdateDTO();

        personFaceUpdateDto.setUserId(photoDTO.getUserId());
        personFaceUpdateDto.setFaceImage(photo.bytes());

        personFaces.add(personFaceUpdateDto);

        return personService.insertPersonFace(personFaces, loginHandle, emtype);
    }
}
