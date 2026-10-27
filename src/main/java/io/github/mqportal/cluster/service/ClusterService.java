package io.github.mqportal.cluster.service;

import io.github.mqportal.cluster.dto.BrokerVO;
import io.github.mqportal.cluster.dto.ClusterOverviewVO;
import io.github.mqportal.cluster.dto.ClusterVO;
import io.github.mqportal.common.BizException;
import org.apache.rocketmq.remoting.protocol.body.ClusterInfo;
import org.apache.rocketmq.remoting.protocol.body.KVTable;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ClusterService {

    private final MqAdminService mqAdmin;

    public ClusterService(MqAdminService mqAdmin) {
        this.mqAdmin = mqAdmin;
    }

    public ClusterOverviewVO overview() {
        ClusterInfo clusterInfo = mqAdmin.examineBrokerClusterInfo();
        Map<String, ClusterVO> clusterMap = new LinkedHashMap<>();
        if (clusterInfo.getBrokerAddrTable() == null) {
            return new ClusterOverviewVO(List.of());
        }
        for (BrokerData brokerData : clusterInfo.getBrokerAddrTable().values()) {
            ClusterVO cluster = clusterMap.computeIfAbsent(brokerData.getCluster(), ClusterVO::new);
            BrokerVO broker = new BrokerVO();
            broker.setCluster(brokerData.getCluster());
            broker.setBrokerName(brokerData.getBrokerName());
            broker.setBrokerAddrs(new LinkedHashMap<>(brokerData.getBrokerAddrs()));
            broker.setRuntimeStats(fetchRuntimeStats(brokerData));
            cluster.getBrokers().add(broker);
        }
        return new ClusterOverviewVO(new ArrayList<>(clusterMap.values()));
    }

    private Map<String, String> fetchRuntimeStats(BrokerData brokerData) {
        String masterAddr = brokerData.getBrokerAddrs().get(0L);
        if (masterAddr == null) {
            return Map.of();
        }
        try {
            KVTable kvTable = mqAdmin.getBrokerRuntimeStats(masterAddr);
            return kvTable.getTable() == null ? Map.of() : new LinkedHashMap<>(kvTable.getTable());
        } catch (BizException e) {
            return Map.of();
        }
    }
}
