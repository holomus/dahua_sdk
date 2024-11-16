package org.example.dahuasdk.client.vhr.entity.load;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class PersonFaceUpdateDTO {
    String userId;
    byte[] faceImage;
}
