package org.example.dahuasdk.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.dahuasdk.services.CustomDateDeserializer;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class PersonInfoDto {
    String userId;
    String name;
    int userType;
    int userStatus;

    @JsonDeserialize(using = CustomDateDeserializer.class)
    Date stuValidBeginTime;


    @JsonDeserialize(using = CustomDateDeserializer.class)
    Date stuValidEndTime;
}
