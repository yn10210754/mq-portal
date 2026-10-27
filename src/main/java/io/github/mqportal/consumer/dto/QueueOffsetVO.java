package io.github.mqportal.consumer.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QueueOffsetVO {

    private String topic;

    private String brokerName;

    private int queueId;

    private long brokerOffset;

    private long consumerOffset;

    private long lag;

    private long lastTimestamp;
}
