package io.github.mqportal.metrics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("metrics_snapshot")
public class MetricsSnapshot {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String topic;

    private String consumerGroup;

    private String metricType;

    private Long metricValue;

    private LocalDateTime createdAt;
}
