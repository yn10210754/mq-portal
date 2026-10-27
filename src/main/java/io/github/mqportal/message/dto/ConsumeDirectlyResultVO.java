package io.github.mqportal.message.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConsumeDirectlyResultVO {

    private String status;

    private String info;

    private long spendTimeMills;
}
