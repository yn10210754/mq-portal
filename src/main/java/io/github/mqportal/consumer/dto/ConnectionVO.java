package io.github.mqportal.consumer.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConnectionVO {

    private String clientId;

    private String clientAddr;

    private String language;

    private String version;
}
