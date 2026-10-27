package io.github.mqportal.topic.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.mqportal.cluster.service.MqAdminService;
import io.github.mqportal.common.BizException;
import io.github.mqportal.topic.dto.BrokerTopicVO;
import io.github.mqportal.topic.dto.CreateTopicRequest;
import io.github.mqportal.topic.dto.TopicVO;
import io.github.mqportal.topic.dto.UpdateTopicRequest;
import io.github.mqportal.topic.entity.TopicMeta;
import io.github.mqportal.topic.mapper.TopicMetaMapper;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.protocol.admin.TopicOffset;
import org.apache.rocketmq.remoting.protocol.admin.TopicStatsTable;
import org.apache.rocketmq.remoting.protocol.body.TopicList;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.remoting.protocol.route.QueueData;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TopicService {

    private final MqAdminService mqAdmin;
    private final TopicMetaMapper topicMetaMapper;

    public TopicService(MqAdminService mqAdmin, TopicMetaMapper topicMetaMapper) {
        this.mqAdmin = mqAdmin;
        this.topicMetaMapper = topicMetaMapper;
    }

    public List<TopicVO> list(String keyword) {
        TopicList topicList = mqAdmin.fetchAllTopicList();
        Map<String, TopicMeta> metaMap = topicMetaMapper.selectList(null).stream()
                .collect(Collectors.toMap(TopicMeta::getTopic, Function.identity(), (a, b) -> a));
        return topicList.getTopicList().stream()
                .filter(topic -> keyword == null || keyword.isBlank() || topic.contains(keyword.trim()))
                .filter(TopicService::isUserTopic)
                .map(topic -> buildVO(topic, metaMap.get(topic)))
                .collect(Collectors.toList());
    }

    public TopicVO detail(String topic) {
        return buildVO(topic, meta(topic));
    }

    public void create(CreateTopicRequest request) {
        validateQueues(request.getReadQueueNums(), request.getWriteQueueNums(), request.getPerm());
        mqAdmin.createOrUpdateTopic(request.getTopic(), request.getReadQueueNums(),
                request.getWriteQueueNums(), request.getPerm());
        upsertMeta(request.getTopic(), request.getOwner(), request.getRemark());
    }

    public void update(String topic, UpdateTopicRequest request) {
        validateQueues(request.getReadQueueNums(), request.getWriteQueueNums(), request.getPerm());
        mqAdmin.createOrUpdateTopic(topic, request.getReadQueueNums(), request.getWriteQueueNums(), request.getPerm());
        upsertMeta(topic, request.getOwner(), request.getRemark());
    }

    public void delete(String topic) {
        mqAdmin.deleteTopic(topic);
        topicMetaMapper.delete(Wrappers.<TopicMeta>lambdaQuery().eq(TopicMeta::getTopic, topic));
    }

    private TopicVO buildVO(String topic, TopicMeta meta) {
        TopicVO vo = new TopicVO();
        vo.setTopic(topic);
        if (meta != null) {
            vo.setOwner(meta.getOwner());
            vo.setRemark(meta.getRemark());
        }
        TopicRouteData route = mqAdmin.examineTopicRouteInfo(topic);
        TopicStatsTable stats;
        try {
            stats = mqAdmin.examineTopicStats(topic);
        } catch (BizException e) {
            stats = new TopicStatsTable();
        }

        Map<String, long[]> brokerStats = new HashMap<>();
        Map<MessageQueue, TopicOffset> offsetTable = stats.getOffsetTable();
        if (offsetTable != null) {
            for (Map.Entry<MessageQueue, TopicOffset> entry : offsetTable.entrySet()) {
                String brokerName = entry.getKey().getBrokerName();
                TopicOffset offset = entry.getValue();
                long[] agg = brokerStats.computeIfAbsent(brokerName, k -> new long[2]);
                agg[0] += offset.getMaxOffset();
                agg[1] = Math.max(agg[1], offset.getLastUpdateTimestamp());
            }
        }

        Map<String, QueueData> queueByBroker = new HashMap<>();
        if (route.getQueueDatas() != null) {
            route.getQueueDatas().forEach(q -> queueByBroker.put(q.getBrokerName(), q));
        }

        List<BrokerTopicVO> brokers = new ArrayList<>();
        if (route.getBrokerDatas() != null) {
            for (BrokerData brokerData : route.getBrokerDatas()) {
                BrokerTopicVO broker = new BrokerTopicVO();
                broker.setBrokerName(brokerData.getBrokerName());
                broker.setCluster(brokerData.getCluster());
                broker.setBrokerAddrs(new LinkedHashMap<>(brokerData.getBrokerAddrs()));
                QueueData queueData = queueByBroker.get(brokerData.getBrokerName());
                if (queueData != null) {
                    broker.setReadQueueNums(queueData.getReadQueueNums());
                    broker.setWriteQueueNums(queueData.getWriteQueueNums());
                    broker.setPerm(queueData.getPerm());
                }
                long[] agg = brokerStats.get(brokerData.getBrokerName());
                broker.setMaxOffset(agg == null ? 0 : agg[0]);
                broker.setLastUpdateTimestamp(agg == null ? 0 : agg[1]);
                brokers.add(broker);
            }
        }
        vo.setBrokers(brokers);
        return vo;
    }

    private TopicMeta meta(String topic) {
        return topicMetaMapper.selectOne(Wrappers.<TopicMeta>lambdaQuery().eq(TopicMeta::getTopic, topic));
    }

    private void upsertMeta(String topic, String owner, String remark) {
        TopicMeta existing = meta(topic);
        LocalDateTime now = LocalDateTime.now();
        if (existing == null) {
            TopicMeta created = new TopicMeta();
            created.setTopic(topic);
            created.setOwner(owner);
            created.setRemark(remark);
            created.setCreatedAt(now);
            created.setUpdatedAt(now);
            topicMetaMapper.insert(created);
        } else {
            if (owner != null) {
                existing.setOwner(owner);
            }
            if (remark != null) {
                existing.setRemark(remark);
            }
            existing.setUpdatedAt(now);
            topicMetaMapper.updateById(existing);
        }
    }

    private void validateQueues(int readQueueNums, int writeQueueNums, int perm) {
        if (readQueueNums <= 0 || writeQueueNums <= 0) {
            throw BizException.badRequest("queue nums must be positive");
        }
        if (perm != 2 && perm != 4 && perm != 6) {
            throw BizException.badRequest("perm must be 2 (write), 4 (read) or 6 (read/write)");
        }
    }

    private static boolean isUserTopic(String topic) {
        return !(topic.startsWith("%RETRY%")
                || topic.startsWith("%DLQ%")
                || topic.startsWith("RMQ_SYS_")
                || topic.startsWith("SCHEDULE_")
                || topic.equals("TBW102")
                || topic.equals("OFFSET_MOVED_EVENT")
                || topic.equals("DefaultCluster"));
    }
}
