package io.github.mqportal.message.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageVO {

    private String msgId;

    private String topic;

    private String tags;

    private String keys;

    private long bornTimestamp;

    private long storeTimestamp;

    private int queueId;

    private long queueOffset;

    private String storeHost;

    private String bodyPreview;

    private String body;
}
