package io.github.mqportal.consumer.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupVO {

    private String groupName;

    private boolean consumeEnable;

    private boolean consumeFromMinEnable;

    private boolean consumeBroadcastEnable;

    private int retryQueueNums;

    private int retryMaxTimes;

    private long brokerId;

    private long whichBrokerWhenConsumeSlowly;

    private boolean notifyConsumerIdsChangedEnable;
}
