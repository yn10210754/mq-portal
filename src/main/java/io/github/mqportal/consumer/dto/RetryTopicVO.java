package io.github.mqportal.consumer.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RetryTopicVO {

    private String topic;

    private String type;

    private boolean exists;

    private long totalMessages;
}
