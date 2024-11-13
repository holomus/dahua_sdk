package org.example.dahuasdk.client.vhr.entity.load;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class PhotoDTO {
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("face_image")
    private FaceImage faceImage;
}
