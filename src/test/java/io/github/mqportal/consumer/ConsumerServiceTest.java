package io.github.mqportal.consumer;

import io.github.mqportal.cluster.service.MqAdminService;
import io.github.mqportal.consumer.dto.ConsumeStatsVO;
import io.github.mqportal.consumer.service.ConsumerService;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.protocol.admin.ConsumeStats;
import org.apache.rocketmq.remoting.protocol.admin.OffsetWrapper;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConsumerServiceTest {

    @Test
    void consumeStatsComputesLagAndClampsNegative() throws Exception {
        DefaultMQAdminExt admin = mock(DefaultMQAdminExt.class);
        ConsumerService service = new ConsumerService(new MqAdminService(admin));

        ConsumeStats stats = new ConsumeStats();
        HashMap<MessageQueue, OffsetWrapper> offsetTable = new HashMap<>();
        offsetTable.put(new MessageQueue("t", "b", 0), wrapper(100, 60, 1000));
        offsetTable.put(new MessageQueue("t", "b", 1), wrapper(50, 80, 2000));
        stats.setOffsetTable(offsetTable);
        when(admin.examineConsumeStats("g")).thenReturn(stats);

        ConsumeStatsVO result = service.consumeStats("g");

        assertEquals(40, result.getTotalLag());
        assertEquals(2, result.getQueues().size());
        assertEquals(40, result.getQueues().get(0).getLag());
        assertEquals(0, result.getQueues().get(1).getLag());
    }

    private OffsetWrapper wrapper(long brokerOffset, long consumerOffset, long timestamp) {
        OffsetWrapper offsetWrapper = new OffsetWrapper();
        offsetWrapper.setBrokerOffset(brokerOffset);
        offsetWrapper.setConsumerOffset(consumerOffset);
        offsetWrapper.setLastTimestamp(timestamp);
        return offsetWrapper;
    }
}
