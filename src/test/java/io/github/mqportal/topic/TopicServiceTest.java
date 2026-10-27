package io.github.mqportal.topic;

import io.github.mqportal.cluster.service.MqAdminService;
import io.github.mqportal.topic.dto.TopicVO;
import io.github.mqportal.topic.entity.TopicMeta;
import io.github.mqportal.topic.mapper.TopicMetaMapper;
import io.github.mqportal.topic.service.TopicService;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.protocol.admin.TopicOffset;
import org.apache.rocketmq.remoting.protocol.admin.TopicStatsTable;
import org.apache.rocketmq.remoting.protocol.body.TopicList;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.remoting.protocol.route.QueueData;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TopicServiceTest {

    @Test
    void listAggregatesBrokerOffsetsAndJoinsLocalMeta() throws Exception {
        DefaultMQAdminExt admin = mock(DefaultMQAdminExt.class);
        MqAdminService mqAdmin = new MqAdminService(admin);
        TopicMetaMapper metaMapper = mock(TopicMetaMapper.class);
        TopicService service = new TopicService(mqAdmin, metaMapper);

        TopicList topicList = new TopicList();
        topicList.setTopicList(new HashSet<>(List.of("order-topic", "%RETRY%consumer-x")));
        when(admin.fetchAllTopicList()).thenReturn(topicList);

        TopicRouteData route = new TopicRouteData();
        BrokerData brokerData = new BrokerData();
        brokerData.setCluster("c1");
        brokerData.setBrokerName("broker-a");
        HashMap<Long, String> addrs = new HashMap<>();
        addrs.put(0L, "127.0.0.1:10911");
        brokerData.setBrokerAddrs(addrs);
        route.setBrokerDatas(List.of(brokerData));

        QueueData queueData = new QueueData();
        queueData.setBrokerName("broker-a");
        queueData.setReadQueueNums(4);
        queueData.setWriteQueueNums(4);
        queueData.setPerm(6);
        route.setQueueDatas(List.of(queueData));
        when(admin.examineTopicRouteInfo("order-topic")).thenReturn(route);

        TopicStatsTable stats = new TopicStatsTable();
        HashMap<MessageQueue, TopicOffset> offsetTable = new HashMap<>();
        offsetTable.put(new MessageQueue("order-topic", "broker-a", 0), offset(100, 300, 1000));
        offsetTable.put(new MessageQueue("order-topic", "broker-a", 1), offset(90, 240, 2000));
        stats.setOffsetTable(offsetTable);
        when(admin.examineTopicStats("order-topic")).thenReturn(stats);

        TopicMeta meta = new TopicMeta();
        meta.setTopic("order-topic");
        meta.setOwner("alice");
        meta.setRemark("orders");
        when(metaMapper.selectList(any())).thenReturn(List.of(meta));

        List<TopicVO> result = service.list(null);

        assertEquals(1, result.size());
        TopicVO vo = result.get(0);
        assertEquals("order-topic", vo.getTopic());
        assertEquals("alice", vo.getOwner());
        assertEquals("orders", vo.getRemark());
        assertEquals(1, vo.getBrokers().size());
        assertEquals("broker-a", vo.getBrokers().get(0).getBrokerName());
        assertEquals(540, vo.getBrokers().get(0).getMaxOffset());
        assertEquals(2000, vo.getBrokers().get(0).getLastUpdateTimestamp());
        assertEquals(4, vo.getBrokers().get(0).getReadQueueNums());
        assertEquals(6, vo.getBrokers().get(0).getPerm());
    }

    private TopicOffset offset(long min, long max, long timestamp) {
        TopicOffset topicOffset = new TopicOffset();
        topicOffset.setMinOffset(min);
        topicOffset.setMaxOffset(max);
        topicOffset.setLastUpdateTimestamp(timestamp);
        return topicOffset;
    }
}
