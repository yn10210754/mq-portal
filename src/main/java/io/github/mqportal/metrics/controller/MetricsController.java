package io.github.mqportal.metrics.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.mqportal.common.Result;
import io.github.mqportal.metrics.dto.MetricPointVO;
import io.github.mqportal.metrics.entity.MetricsSnapshot;
import io.github.mqportal.metrics.mapper.MetricsSnapshotMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/metrics")
@Tag(name = "metrics")
public class MetricsController {

    private final MetricsSnapshotMapper snapshotMapper;

    public MetricsController(MetricsSnapshotMapper snapshotMapper) {
        this.snapshotMapper = snapshotMapper;
    }

    @GetMapping("/series")
    public Result<List<MetricPointVO>> series(@RequestParam(required = false) String topic,
                                              @RequestParam(required = false) String consumerGroup,
                                              @RequestParam(required = false) String metricType,
                                              @RequestParam(required = false) Long start,
                                              @RequestParam(required = false) Long end) {
        List<MetricsSnapshot> snapshots = snapshotMapper.selectList(
                Wrappers.<MetricsSnapshot>lambdaQuery()
                        .eq(topic != null, MetricsSnapshot::getTopic, topic)
                        .eq(consumerGroup != null, MetricsSnapshot::getConsumerGroup, consumerGroup)
                        .eq(metricType != null, MetricsSnapshot::getMetricType, metricType)
                        .ge(start != null, MetricsSnapshot::getCreatedAt, toLocalDateTime(start))
                        .le(end != null, MetricsSnapshot::getCreatedAt, toLocalDateTime(end))
                        .orderByAsc(MetricsSnapshot::getCreatedAt));
        return Result.ok(snapshots.stream().map(this::toVO).toList());
    }

    private MetricPointVO toVO(MetricsSnapshot snapshot) {
        MetricPointVO vo = new MetricPointVO();
        vo.setTimestamp(snapshot.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        vo.setTopic(snapshot.getTopic());
        vo.setConsumerGroup(snapshot.getConsumerGroup());
        vo.setMetricType(snapshot.getMetricType());
        vo.setValue(snapshot.getMetricValue() == null ? 0 : snapshot.getMetricValue());
        return vo;
    }

    private LocalDateTime toLocalDateTime(Long epochMillis) {
        if (epochMillis == null) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault());
    }
}
