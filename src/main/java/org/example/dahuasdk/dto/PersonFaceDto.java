package org.example.dahuasdk.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class PersonFaceDto {
    String userId;
    List<byte[]> faceImages;
}
