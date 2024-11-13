package org.example.dahuasdk.client.dahua.entity;

public class DahuaUtil {
    public static DahuaErrorResponse makeErrorResponse(String errorMsg, String subStatusCode) {
        return DahuaErrorResponse.builder()
                .errorMsg(errorMsg)
                .statusCode(8)
                .build();
    }
}
