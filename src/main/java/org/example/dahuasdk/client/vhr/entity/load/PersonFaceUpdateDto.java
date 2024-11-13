package org.example.dahuasdk.client.vhr.entity.load;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class PersonFaceUpdateDto {
    String userId;
    byte[] faceImage;
}
