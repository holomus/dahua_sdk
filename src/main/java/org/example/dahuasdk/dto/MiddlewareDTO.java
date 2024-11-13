package org.example.dahuasdk.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class MiddlewareDTO {
    private long id;
    private String host;
    private String token;
    private String username;
    private String password;
}