package io.github.mqportal.consumer.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ConsumeStatsVO {

    private String group;

    private long totalLag;

    private List<QueueOffsetVO> queues = new ArrayList<>();
}
