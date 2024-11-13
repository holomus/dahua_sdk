package org.example.dahuasdk;

import lombok.RequiredArgsConstructor;
import org.example.dahuasdk.services.AutoRegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@RequiredArgsConstructor

public class DahuaSdkApplication {
    public static AutoRegisterService autoRegisterService;

    @Autowired
    public void setAutoRegisterService(AutoRegisterService autoRegisterService) {
        DahuaSdkApplication.autoRegisterService = autoRegisterService;
    }

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(DahuaSdkApplication.class, args);

        autoRegisterService = context.getBean(AutoRegisterService.class);

        autoRegisterService.init(autoRegisterService.disConnect, autoRegisterService.haveReConnect);
        autoRegisterService.startServer(
                "192.168.50.227",
                9500,
                autoRegisterService.callback
        );
    }
}
