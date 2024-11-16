package org.example.dahuasdk;

import lombok.RequiredArgsConstructor;
import org.example.dahuasdk.services.AutoRegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.beans.factory.annotation.Value;

@SpringBootApplication
@EnableScheduling
@RequiredArgsConstructor

public class DahuaSdkApplication {
    @Value("${dahua.server.address}")
    private String serverAddress;
    @Value("${dahua.server.port}")
    private int serverPort;

    private final AutoRegisterService autoRegisterService;

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(DahuaSdkApplication.class, args);
        DahuaSdkApplication application = context.getBean(DahuaSdkApplication.class);
        application.autoRegisterService.initSDK();
        application.autoRegisterService.startServer(application.serverAddress, application.serverPort);
    }
}
