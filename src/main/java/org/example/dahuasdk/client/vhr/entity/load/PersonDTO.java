package org.example.dahuasdk.client.vhr.entity.load;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.dahuasdk.services.CustomDateDeserializer;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class PersonDTO {
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("name")
    private String name;
    @JsonProperty("user_type")
    private int userType;
    @JsonProperty("user_status")
    private int userStatus;

    @JsonProperty("stu_valid_begin_time")
    private String stuValidBeginTimeStr;
    private Date stuValidBeginTime;

    @JsonProperty("stu_valid_end_time")
    private String stuValidEndTimeStr;
    private Date stuValidEndTime;
}
