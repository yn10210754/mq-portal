package io.github.mqportal.message.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendResultVO {

    private String msgId;

    private String sendStatus;

    private Integer queueId;

    private Long queueOffset;
}
