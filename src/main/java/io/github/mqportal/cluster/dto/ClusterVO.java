package io.github.mqportal.cluster.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ClusterVO {

    private String name;

    private List<BrokerVO> brokers = new ArrayList<>();

    public ClusterVO() {
    }

    public ClusterVO(String name) {
        this.name = name;
    }
}
