package org.example.dahuasdk;

import org.example.dahuasdk.services.AutoRegisterService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DahuaSdkApplication {
    public final static AutoRegisterService autoRegisterService = new AutoRegisterService();
    public static void main(String[] args) {
        autoRegisterService.initSDK();

        autoRegisterService.startServer(
                "192.168.50.227",
                9500,
                autoRegisterService.callback
        );

        SpringApplication.run(DahuaSdkApplication.class, args);
    }
}
