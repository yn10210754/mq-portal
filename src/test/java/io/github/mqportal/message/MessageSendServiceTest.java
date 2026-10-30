package io.github.mqportal.message;

import io.github.mqportal.message.dto.SendMessageRequest;
import io.github.mqportal.message.dto.SendResultVO;
import io.github.mqportal.message.service.MessageSendService;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MessageSendServiceTest {

    @Test
    void sendShouldSendRequestedCountAndMapResult() throws Exception {
        DefaultMQProducer producer = mock(DefaultMQProducer.class);
        SendResult sendResult = new SendResult(SendStatus.SEND_OK, "MSG_ID_1", null,
                new MessageQueue("ORDER_TOPIC", "broker-a", 3), 42L);
        when(producer.send(any(Message.class))).thenReturn(sendResult);

        MessageSendService service = new MessageSendService(producer);
        SendMessageRequest request = new SendMessageRequest();
        request.setTopic("ORDER_TOPIC");
        request.setBody("hello");
        request.setKeys("order-key");
        request.setCount(3);

        List<SendResultVO> results = service.send(request);

        assertEquals(3, results.size());
        assertEquals("MSG_ID_1", results.get(0).getMsgId());
        assertEquals("SEND_OK", results.get(0).getSendStatus());
        assertEquals(3, results.get(0).getQueueId());
        assertEquals(42L, results.get(0).getQueueOffset());
        verify(producer, times(1)).start();
        verify(producer, times(3)).send(any(Message.class));

        Message sent = captureSent(producer);
        assertEquals("ORDER_TOPIC", sent.getTopic());
        assertEquals("hello", new String(sent.getBody(), StandardCharsets.UTF_8));
    }

    private Message captureSent(DefaultMQProducer producer) throws Exception {
        org.mockito.ArgumentCaptor<Message> captor = org.mockito.ArgumentCaptor.forClass(Message.class);
        verify(producer, times(3)).send(captor.capture());
        return captor.getValue();
    }
}
