package io.github.mqportal.message.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendMessageRequest {

    @NotBlank
    private String topic;

    @NotBlank
    private String body;

    private String tags;

    private String keys;

    @Min(1)
    @Max(100)
    private int count = 1;
}
