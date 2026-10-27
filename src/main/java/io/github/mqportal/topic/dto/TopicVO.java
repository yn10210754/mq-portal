package io.github.mqportal.topic.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TopicVO {

    private String topic;

    private String owner;

    private String remark;

    private List<BrokerTopicVO> brokers = new ArrayList<>();
}
