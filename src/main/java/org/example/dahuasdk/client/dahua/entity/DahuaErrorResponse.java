package org.example.dahuasdk.client.dahua.entity;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class DahuaErrorResponse {
    private String errorMsg;
    private long statusCode;
}
