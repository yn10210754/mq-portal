package io.github.mqportal.cluster.service;

import io.github.mqportal.common.BizException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.rocketmq.client.QueryResult;
import org.apache.rocketmq.common.TopicConfig;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.protocol.admin.ConsumeStats;
import org.apache.rocketmq.remoting.protocol.admin.TopicStatsTable;
import org.apache.rocketmq.remoting.protocol.body.ClusterInfo;
import org.apache.rocketmq.remoting.protocol.body.ConsumeMessageDirectlyResult;
import org.apache.rocketmq.remoting.protocol.body.ConsumerConnection;
import org.apache.rocketmq.remoting.protocol.body.KVTable;
import org.apache.rocketmq.remoting.protocol.body.SubscriptionGroupWrapper;
import org.apache.rocketmq.remoting.protocol.body.TopicList;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;
import org.apache.rocketmq.remoting.protocol.subscription.SubscriptionGroupConfig;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MqAdminService {

    private static final Logger log = LoggerFactory.getLogger(MqAdminService.class);

    private final DefaultMQAdminExt admin;

    private long timeoutMillis = 3000;

    public MqAdminService(DefaultMQAdminExt admin) {
        this.admin = admin;
    }

    public void setTimeoutMillis(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    @PostConstruct
    public void start() {
        try {
            admin.start();
        } catch (Exception e) {
            log.warn("failed to start RocketMQ admin client: {}", e.getMessage());
        }
    }

    @PreDestroy
    public void shutdown() {
        admin.shutdown();
    }

    public ClusterInfo examineBrokerClusterInfo() {
        return invoke("examineBrokerClusterInfo", admin::examineBrokerClusterInfo);
    }

    public KVTable getBrokerRuntimeStats(String brokerAddr) {
        return invoke("fetchBrokerRuntimeStats", () -> admin.fetchBrokerRuntimeStats(brokerAddr));
    }

    public TopicList fetchAllTopicList() {
        return invoke("fetchAllTopicList", admin::fetchAllTopicList);
    }

    public TopicRouteData examineTopicRouteInfo(String topic) {
        return invoke("examineTopicRouteInfo", () -> admin.examineTopicRouteInfo(topic));
    }

    public TopicStatsTable examineTopicStats(String topic) {
        return invoke("examineTopicStats", () -> admin.examineTopicStats(topic));
    }

    public SubscriptionGroupWrapper getAllSubscriptionGroup(String brokerAddr) {
        return invoke("getAllSubscriptionGroup", () -> admin.getAllSubscriptionGroup(brokerAddr, timeoutMillis));
    }

    public ConsumerConnection examineConsumerConnectionInfo(String group) {
        return invoke("examineConsumerConnectionInfo", () -> admin.examineConsumerConnectionInfo(group));
    }

    public ConsumeStats examineConsumeStats(String group) {
        return invoke("examineConsumeStats", () -> admin.examineConsumeStats(group));
    }

    public QueryResult queryMessage(String topic, String key, int maxNum, long begin, long end) {
        return invoke("queryMessage", () -> admin.queryMessage(topic, key, maxNum, begin, end));
    }

    public MessageExt viewMessage(String msgId) {
        return invoke("viewMessage", () -> admin.viewMessage(msgId));
    }

    public ConsumeMessageDirectlyResult consumeMessageDirectly(String msgId, String consumerGroup, String brokerName) {
        return invoke("consumeMessageDirectly", () -> admin.consumeMessageDirectly(msgId, consumerGroup, brokerName));
    }

    public void resetOffsetByTimestamp(String topic, String group, long timestamp, boolean isForce) {
        invokeVoid("resetOffsetByTimestamp", () -> admin.resetOffsetByTimestamp(topic, group, timestamp, isForce));
    }

    public void createOrUpdateTopic(String topic, int readQueueNums, int writeQueueNums, int perm) {
        invokeVoid("createOrUpdateTopic", () -> {
            TopicConfig config = new TopicConfig(topic, readQueueNums, writeQueueNums, perm);
            for (String addr : masterBrokerAddrs()) {
                admin.createAndUpdateTopicConfig(addr, config);
            }
        });
    }

    public void deleteTopic(String topic) {
        invokeVoid("deleteTopic", () -> {
            Set<String> masters = masterBrokerAddrs();
            admin.deleteTopicInBroker(masters, topic);
            ClusterInfo cluster = examineBrokerClusterInfo();
            List<String> nameServerAddrs = admin.getNameServerAddressList();
            if (cluster.getClusterAddrTable() != null) {
                for (String clusterName : cluster.getClusterAddrTable().keySet()) {
                    admin.deleteTopicInNameServer(new HashSet<>(nameServerAddrs), clusterName, topic);
                }
            }
        });
    }

    public void createOrUpdateSubscriptionGroup(String group, boolean consumeBroadcastEnable,
                                                int retryQueueNums, int retryMaxTimes) {
        invokeVoid("createOrUpdateSubscriptionGroup", () -> {
            SubscriptionGroupConfig config = new SubscriptionGroupConfig();
            config.setGroupName(group);
            config.setConsumeBroadcastEnable(consumeBroadcastEnable);
            config.setRetryQueueNums(retryQueueNums);
            config.setRetryMaxTimes(retryMaxTimes);
            for (String addr : masterBrokerAddrs()) {
                admin.createAndUpdateSubscriptionGroupConfig(addr, config);
            }
        });
    }

    public void deleteSubscriptionGroup(String group) {
        invokeVoid("deleteSubscriptionGroup", () -> {
            for (String addr : masterBrokerAddrs()) {
                admin.deleteSubscriptionGroup(addr, group, true);
            }
        });
    }

    public Set<String> masterBrokerAddrs() {
        ClusterInfo cluster = examineBrokerClusterInfo();
        if (cluster.getBrokerAddrTable() == null) {
            return Set.of();
        }
        return cluster.getBrokerAddrTable().values().stream()
                .map(BrokerData::getBrokerAddrs)
                .flatMap(addrs -> addrs.entrySet().stream())
                .filter(entry -> entry.getKey() == 0L)
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
    }

    public String firstMasterBrokerAddr() {
        return masterBrokerAddrs().stream().findFirst().orElse(null);
    }

    private <T> T invoke(String action, MqCall<T> call) {
        try {
            return call.call();
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException(500, "RocketMQ operation failed: " + action + " - " + e.getMessage());
        }
    }

    private void invokeVoid(String action, MqVoidCall call) {
        invoke(action, () -> {
            call.call();
            return null;
        });
    }

    @FunctionalInterface
    private interface MqCall<T> {
        T call() throws Exception;
    }

    @FunctionalInterface
    private interface MqVoidCall {
        void call() throws Exception;
    }
}
