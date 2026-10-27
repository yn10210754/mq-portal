package io.github.mqportal.topic.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTopicRequest {

    @NotBlank
    private String topic;

    private int readQueueNums = 8;

    private int writeQueueNums = 8;

    private int perm = 6;

    private String owner;

    private String remark;
}
