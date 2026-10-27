package io.github.mqportal.metrics.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MetricPointVO {

    private long timestamp;

    private String topic;

    private String consumerGroup;

    private String metricType;

    private long value;
}
