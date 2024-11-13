package org.example.dahuasdk.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netsdk.lib.NetSDKLib;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.example.dahuasdk.DahuaSdkApplication;
import org.example.dahuasdk.client.vhr.VHRClient;
import org.example.dahuasdk.client.vhr.entity.load.*;
import org.example.dahuasdk.client.vhr.entity.save.CommandResult;
import org.example.dahuasdk.client.vhr.entity.save.CommandResultData;
import org.example.dahuasdk.client.vhr.entity.save.CommandsResult;
import org.example.dahuasdk.dao.AppDAO;
import org.example.dahuasdk.entity.Device;
import org.example.dahuasdk.entity.Middleware;
import org.example.dahuasdk.services.AppService;
import org.example.dahuasdk.services.PersonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;

@RequiredArgsConstructor
@Setter
@Component
@Scope("prototype")
public class CommandExecutor {
    private static final Logger log = LoggerFactory.getLogger(CommandExecutor.class);
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

            loginHandle = DahuaSdkApplication
                    .autoRegisterService
                    .deviceConnectionInfo
                    .get(device.getDeviceId())
                    .getLoginHandle();
        }

        executeCommandsConcurrently(commands.getCommands());
        return true;
    }

    public CommandResult executeCommand(Command command) {
        List<Integer> failcodes = new ArrayList<>();

        System.out.println("\ncommandCode = " + command.getCommandCode() + '\n');

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
            case "dahua:person:set_photo":
                failcodes = setPhoto(command.getCommandBody());
                break;
            default:
                log.error("Invalid command code mode: {}", command.getCommandCode());
        }

        CommandResultData commandResultData = new CommandResultData();
        commandResultData.setStatusCode(200);
        commandResultData.setFailCodes(failcodes);

        return new CommandResult(command.getCommandId(), commandResultData);
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

        List<CommandResult> failedCommandResults = new ArrayList<CommandResult>();

        for (int i = 0; i < commands.size(); i++) {
            try {
                Future<CommandResult> future = completionService.poll(1, TimeUnit.SECONDS);
                if (future != null && future.get() != null)
                    failedCommandResults.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error occurred while getting command result", e);
            }
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

    private List<Integer> addPerson(Object commandBody) {
        PersonDTO person = objectMapper.convertValue(commandBody, PersonDTO.class);

        try {
            return personService.savePerson(List.of(person), loginHandle);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return new ArrayList<>();
    }

    private record UserId(@JsonProperty("user_id") String user_id){
    }

    private List<Integer> removePerson(Object commandBody) {
        UserId userId = objectMapper.convertValue(commandBody, UserId.class);

        try {
            return personService.removePerson(new String[]{userId.user_id}, loginHandle);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return new ArrayList<>();
    }

    private List<Integer> setPhoto(Object commandBody) {
        PhotoDTO photoDTO = objectMapper.convertValue(commandBody, PhotoDTO.class);
        Photo photo = vhrClient.loadPhoto(middleware, photoDTO.getFaceImage());

        List<PersonFaceUpdateDto> personFaces = new ArrayList<>();

        PersonFaceUpdateDto personFaceUpdateDto = new PersonFaceUpdateDto();

        personFaceUpdateDto.setUserId(photoDTO.getUserId());
        personFaceUpdateDto.setFaceImage(photo.bytes());

        personFaces.add(personFaceUpdateDto);

        return personService.savePersonFace(personFaces, loginHandle);
    }
}
