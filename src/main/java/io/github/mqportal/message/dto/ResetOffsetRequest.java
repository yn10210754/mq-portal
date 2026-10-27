package io.github.mqportal.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetOffsetRequest {

    @NotBlank
    private String topic;

    @NotBlank
    private String group;

    @NotNull
    private Long timestamp;

    private boolean force = false;
}
