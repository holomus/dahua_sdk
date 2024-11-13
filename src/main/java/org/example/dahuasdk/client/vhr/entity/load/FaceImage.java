package org.example.dahuasdk.client.vhr.entity.load;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FaceImage {
    @JsonProperty("image_sha")
    private String photoSha;
    @JsonProperty("width")
    private String width;
    @JsonProperty("height")
    private String height;
    @JsonProperty("format")
    private String format;
    @JsonProperty("quality")
    private String quality;
}
