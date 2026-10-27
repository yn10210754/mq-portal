package io.github.mqportal.topic.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTopicRequest {

    @NotNull
    private Integer readQueueNums;

    @NotNull
    private Integer writeQueueNums;

    @NotNull
    private Integer perm;

    private String owner;

    private String remark;
}
