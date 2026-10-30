package io.github.mqportal.message.service;

import io.github.mqportal.common.BizException;
import io.github.mqportal.config.RocketMqProperties;
import io.github.mqportal.message.dto.SendMessageRequest;
import io.github.mqportal.message.dto.SendResultVO;
import jakarta.annotation.PreDestroy;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class MessageSendService {

    private final DefaultMQProducer producer;

    private volatile boolean started;

    @org.springframework.beans.factory.annotation.Autowired
    public MessageSendService(RocketMqProperties properties) {
        this(newProducer(properties));
    }

    public MessageSendService(DefaultMQProducer producer) {
        this.producer = producer;
    }

    private static DefaultMQProducer newProducer(RocketMqProperties properties) {
        DefaultMQProducer producer = new DefaultMQProducer("mq-portal-producer");
        producer.setNamesrvAddr(properties.getNamesrvAddr());
        producer.setInstanceName("mq-portal-producer");
        return producer;
    }

    public List<SendResultVO> send(SendMessageRequest request) {
        ensureStarted();
        List<SendResultVO> results = new ArrayList<>(request.getCount());
        for (int i = 0; i < request.getCount(); i++) {
            String keys = request.getKeys() == null ? null
                    : (request.getCount() == 1 ? request.getKeys() : request.getKeys() + "-" + i);
            Message message = new Message(request.getTopic(), request.getTags(), keys,
                    request.getBody().getBytes(StandardCharsets.UTF_8));
            try {
                results.add(toVO(producer.send(message)));
            } catch (Exception e) {
                throw new BizException(500, "RocketMQ operation failed: send - " + e.getMessage());
            }
        }
        return results;
    }

    private synchronized void ensureStarted() {
        if (started) {
            return;
        }
        try {
            producer.start();
            started = true;
        } catch (Exception e) {
            throw new BizException(500, "RocketMQ operation failed: start producer - " + e.getMessage());
        }
    }

    private SendResultVO toVO(SendResult result) {
        SendResultVO vo = new SendResultVO();
        vo.setMsgId(result.getMsgId());
        vo.setSendStatus(result.getSendStatus() == null ? null : result.getSendStatus().name());
        if (result.getMessageQueue() != null) {
            vo.setQueueId(result.getMessageQueue().getQueueId());
            vo.setQueueOffset(result.getQueueOffset());
        }
        return vo;
    }

    @PreDestroy
    public void shutdown() {
        if (started) {
            producer.shutdown();
        }
    }
}
