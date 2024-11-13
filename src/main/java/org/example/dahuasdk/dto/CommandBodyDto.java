package org.example.dahuasdk.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import org.example.dahuasdk.services.CustomDateDeserializer;

import java.sql.Date;

@AllArgsConstructor

public class CommandBodyDto {
    public final String COMMAND_CODE_DEVICE_SET_UP = "dahua:device:set_up";
    public final String COMMAND_CODE_DEVICE_REMOVE = "dahua:device:remove";
    public final String COMMAND_CODE_PERSON_SET_UP = "dahua:person:set_up";
    public final String COMMAND_CODE_PERSON_REMOVE = "dahua:person:remove";
    public final String COMMAND_CODE_PERSON_SET_PHOTO = "dahua:person:set_photo";

    // device info
    private String login;
    private String pass;
    private String device_id;

    // person info
    private String user_id;
    private String name;
    private int user_status;
    private int user_type;

    @JsonDeserialize(using = CustomDateDeserializer.class)
    private Date stu_valid_begin_time;
    @JsonDeserialize(using = CustomDateDeserializer.class)
    private Date stu_valid_end_time;

    // photo info
    private String image_sha;
}
