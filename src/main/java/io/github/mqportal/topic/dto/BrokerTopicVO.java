package io.github.mqportal.topic.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class BrokerTopicVO {

    private String cluster;

    private String brokerName;

    private Map<Long, String> brokerAddrs = new LinkedHashMap<>();

    private int readQueueNums;

    private int writeQueueNums;

    private int perm;

    private long maxOffset;

    private long lastUpdateTimestamp;
}
