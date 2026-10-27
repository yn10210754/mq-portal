package io.github.mqportal.consumer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateGroupRequest {

    @NotBlank
    private String groupName;

    private boolean consumeBroadcastEnable = false;

    private int retryQueueNums = 1;

    private int retryMaxTimes = 16;
}
