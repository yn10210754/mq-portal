package io.github.mqportal.message.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResendDeadLetterRequest {

    @NotBlank
    private String msgId;

    @NotBlank
    private String consumerGroup;

    @NotBlank
    private String brokerName;
}
