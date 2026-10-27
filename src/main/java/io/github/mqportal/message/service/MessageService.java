package io.github.mqportal.message.service;

import io.github.mqportal.cluster.service.MqAdminService;
import io.github.mqportal.message.dto.ConsumeDirectlyResultVO;
import io.github.mqportal.message.dto.MessageVO;
import io.github.mqportal.message.dto.ResendDeadLetterRequest;
import io.github.mqportal.message.dto.ResetOffsetRequest;
import org.apache.rocketmq.client.QueryResult;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.protocol.body.ConsumeMessageDirectlyResult;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Service
public class MessageService {

    private final MqAdminService mqAdmin;

    public MessageService(MqAdminService mqAdmin) {
        this.mqAdmin = mqAdmin;
    }

    public List<MessageVO> query(String topic, String key, Long begin, Long end, Integer maxNum) {
        long beginTs = begin == null ? 0 : begin;
        long endTs = end == null ? System.currentTimeMillis() : end;
        int max = maxNum == null ? 32 : Math.min(maxNum, 100);
        QueryResult result = mqAdmin.queryMessage(topic, key, max, beginTs, endTs);
        return result.getMessageList().stream().map(message -> toVO(message, false)).toList();
    }

    public MessageVO detail(String msgId) {
        return toVO(mqAdmin.viewMessage(msgId), true);
    }

    public ConsumeDirectlyResultVO resendDeadLetter(ResendDeadLetterRequest request) {
        ConsumeMessageDirectlyResult result = mqAdmin.consumeMessageDirectly(
                request.getMsgId(), request.getConsumerGroup(), request.getBrokerName());
        ConsumeDirectlyResultVO vo = new ConsumeDirectlyResultVO();
        vo.setStatus(result.getConsumeResult() == null ? null : result.getConsumeResult().name());
        vo.setInfo(result.getRemark());
        vo.setSpendTimeMills(result.getSpentTimeMills());
        return vo;
    }

    public void resetOffset(ResetOffsetRequest request) {
        mqAdmin.resetOffsetByTimestamp(request.getTopic(), request.getGroup(),
                request.getTimestamp(), request.isForce());
    }

    private MessageVO toVO(MessageExt message, boolean withBody) {
        MessageVO vo = new MessageVO();
        vo.setMsgId(message.getMsgId());
        vo.setTopic(message.getTopic());
        vo.setTags(message.getTags());
        vo.setKeys(message.getKeys());
        vo.setBornTimestamp(message.getBornTimestamp());
        vo.setStoreTimestamp(message.getStoreTimestamp());
        vo.setQueueId(message.getQueueId());
        vo.setQueueOffset(message.getQueueOffset());
        vo.setStoreHost(message.getStoreHost() == null ? null : message.getStoreHost().toString());
        String body = decodeBody(message.getBody());
        vo.setBodyPreview(truncate(body, 500));
        if (withBody) {
            vo.setBody(body);
        }
        return vo;
    }

    private String decodeBody(byte[] body) {
        if (body == null || body.length == 0) {
            return "";
        }
        String decoded = new String(body, StandardCharsets.UTF_8);
        if (decoded.indexOf('\uFFFD') >= 0) {
            return Base64.getEncoder().encodeToString(body);
        }
        return decoded;
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
