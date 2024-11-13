package org.example.dahuasdk.client.dahua.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DahuaResult {
    private int code;
    private String output;

    public boolean isSuccess() {
        return code >= 200 && code < 300;
    }
}
