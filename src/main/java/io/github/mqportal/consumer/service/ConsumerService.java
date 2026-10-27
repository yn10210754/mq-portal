package io.github.mqportal.consumer.service;

import io.github.mqportal.cluster.service.MqAdminService;
import io.github.mqportal.common.BizException;
import io.github.mqportal.consumer.dto.ConnectionVO;
import io.github.mqportal.consumer.dto.ConsumeStatsVO;
import io.github.mqportal.consumer.dto.CreateGroupRequest;
import io.github.mqportal.consumer.dto.GroupVO;
import io.github.mqportal.consumer.dto.QueueOffsetVO;
import io.github.mqportal.consumer.dto.RetryTopicVO;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.protocol.admin.ConsumeStats;
import org.apache.rocketmq.remoting.protocol.admin.OffsetWrapper;
import org.apache.rocketmq.remoting.protocol.admin.TopicOffset;
import org.apache.rocketmq.remoting.protocol.admin.TopicStatsTable;
import org.apache.rocketmq.remoting.protocol.body.ConsumerConnection;
import org.apache.rocketmq.remoting.protocol.body.SubscriptionGroupWrapper;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;
import org.apache.rocketmq.remoting.protocol.subscription.SubscriptionGroupConfig;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ConsumerService {

    private final MqAdminService mqAdmin;

    public ConsumerService(MqAdminService mqAdmin) {
        this.mqAdmin = mqAdmin;
    }

    public List<GroupVO> list() {
        String brokerAddr = requireBroker();
        SubscriptionGroupWrapper wrapper = mqAdmin.getAllSubscriptionGroup(brokerAddr);
        return wrapper.getSubscriptionGroupTable().values().stream().map(this::toGroupVO).toList();
    }

    public GroupVO detail(String group) {
        SubscriptionGroupWrapper wrapper = mqAdmin.getAllSubscriptionGroup(requireBroker());
        SubscriptionGroupConfig config = wrapper.getSubscriptionGroupTable().get(group);
        if (config == null) {
            throw new BizException("consumer group not found: " + group);
        }
        return toGroupVO(config);
    }

    public void create(CreateGroupRequest request) {
        mqAdmin.createOrUpdateSubscriptionGroup(request.getGroupName(), request.isConsumeBroadcastEnable(),
                request.getRetryQueueNums(), request.getRetryMaxTimes());
    }

    public void delete(String group) {
        mqAdmin.deleteSubscriptionGroup(group);
    }

    public List<ConnectionVO> connections(String group) {
        ConsumerConnection consumerConnection = mqAdmin.examineConsumerConnectionInfo(group);
        return consumerConnection.getConnectionSet().stream().map(connection -> {
            ConnectionVO vo = new ConnectionVO();
            vo.setClientId(connection.getClientId());
            vo.setClientAddr(connection.getClientAddr());
            vo.setLanguage(connection.getLanguage() == null ? null : connection.getLanguage().name());
            vo.setVersion(String.valueOf(connection.getVersion()));
            return vo;
        }).toList();
    }

    public ConsumeStatsVO consumeStats(String group) {
        ConsumeStats stats = mqAdmin.examineConsumeStats(group);
        List<QueueOffsetVO> queues = new ArrayList<>();
        long totalLag = 0;
        Map<MessageQueue, OffsetWrapper> offsetTable = stats.getOffsetTable();
        if (offsetTable != null) {
            for (Map.Entry<MessageQueue, OffsetWrapper> entry : offsetTable.entrySet()) {
                MessageQueue queue = entry.getKey();
                OffsetWrapper wrapper = entry.getValue();
                long lag = Math.max(0, wrapper.getBrokerOffset() - wrapper.getConsumerOffset());
                totalLag += lag;

                QueueOffsetVO vo = new QueueOffsetVO();
                vo.setTopic(queue.getTopic());
                vo.setBrokerName(queue.getBrokerName());
                vo.setQueueId(queue.getQueueId());
                vo.setBrokerOffset(wrapper.getBrokerOffset());
                vo.setConsumerOffset(wrapper.getConsumerOffset());
                vo.setLag(lag);
                vo.setLastTimestamp(wrapper.getLastTimestamp());
                queues.add(vo);
            }
        }
        ConsumeStatsVO result = new ConsumeStatsVO();
        result.setGroup(group);
        result.setTotalLag(totalLag);
        result.setQueues(queues);
        return result;
    }

    public List<RetryTopicVO> retryTopics(String group) {
        return List.of(retryTopic(group, "%RETRY%" + group, "RETRY"), retryTopic(group, "%DLQ%" + group, "DLQ"));
    }

    private RetryTopicVO retryTopic(String group, String topic, String type) {
        RetryTopicVO vo = new RetryTopicVO();
        vo.setTopic(topic);
        vo.setType(type);
        try {
            TopicRouteData route = mqAdmin.examineTopicRouteInfo(topic);
            vo.setExists(true);
        } catch (BizException e) {
            vo.setExists(false);
            return vo;
        }
        try {
            TopicStatsTable stats = mqAdmin.examineTopicStats(topic);
            long total = stats.getOffsetTable() == null ? 0
                    : stats.getOffsetTable().values().stream().mapToLong(TopicOffset::getMaxOffset).sum();
            vo.setTotalMessages(total);
        } catch (BizException e) {
            vo.setTotalMessages(0);
        }
        return vo;
    }

    private GroupVO toGroupVO(SubscriptionGroupConfig config) {
        GroupVO vo = new GroupVO();
        vo.setGroupName(config.getGroupName());
        vo.setConsumeEnable(config.isConsumeEnable());
        vo.setConsumeFromMinEnable(config.isConsumeFromMinEnable());
        vo.setConsumeBroadcastEnable(config.isConsumeBroadcastEnable());
        vo.setRetryQueueNums(config.getRetryQueueNums());
        vo.setRetryMaxTimes(config.getRetryMaxTimes());
        vo.setBrokerId(config.getBrokerId());
        vo.setWhichBrokerWhenConsumeSlowly(config.getWhichBrokerWhenConsumeSlowly());
        vo.setNotifyConsumerIdsChangedEnable(config.isNotifyConsumerIdsChangedEnable());
        return vo;
    }

    private String requireBroker() {
        String brokerAddr = mqAdmin.firstMasterBrokerAddr();
        if (brokerAddr == null) {
            throw new BizException("no broker available in cluster");
        }
        return brokerAddr;
    }
}
