package io.github.mqportal.cluster.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class BrokerVO {

    private String cluster;

    private String brokerName;

    private Map<Long, String> brokerAddrs = new LinkedHashMap<>();

    private Map<String, String> runtimeStats = new LinkedHashMap<>();
}
