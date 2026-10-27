package io.github.mqportal.metrics;

import io.github.mqportal.cluster.service.MqAdminService;
import io.github.mqportal.common.BizException;
import io.github.mqportal.metrics.entity.MetricsSnapshot;
import io.github.mqportal.metrics.mapper.MetricsSnapshotMapper;
import org.apache.rocketmq.remoting.protocol.admin.ConsumeStats;
import org.apache.rocketmq.remoting.protocol.admin.TopicOffset;
import org.apache.rocketmq.remoting.protocol.admin.TopicStatsTable;
import org.apache.rocketmq.remoting.protocol.body.SubscriptionGroupWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;

@Component
public class MetricsCollector {

    private static final Logger log = LoggerFactory.getLogger(MetricsCollector.class);

    private final MqAdminService mqAdmin;
    private final MetricsSnapshotMapper snapshotMapper;

    public MetricsCollector(MqAdminService mqAdmin, MetricsSnapshotMapper snapshotMapper) {
        this.mqAdmin = mqAdmin;
        this.snapshotMapper = snapshotMapper;
    }

    @Scheduled(fixedDelayString = "${metrics.collect-interval-ms:60000}",
            initialDelayString = "${metrics.collect-initial-delay-ms:30000}")
    public void collect() {
        collectTopicOffsets();
        collectGroupLags();
    }

    private void collectTopicOffsets() {
        try {
            Set<String> topics = mqAdmin.fetchAllTopicList().getTopicList();
            LocalDateTime now = LocalDateTime.now();
            for (String topic : topics) {
                if (!isUserTopic(topic)) {
                    continue;
                }
                try {
                    TopicStatsTable stats = mqAdmin.examineTopicStats(topic);
                    long total = stats.getOffsetTable() == null ? 0
                            : stats.getOffsetTable().values().stream().mapToLong(TopicOffset::getMaxOffset).sum();
                    insert(topic, null, MetricType.TOPIC_OFFSET, total, now);
                } catch (BizException e) {
                    log.debug("skip topic {} during metrics collection: {}", topic, e.getMessage());
                }
            }
        } catch (BizException e) {
            log.warn("failed to collect topic offsets: {}", e.getMessage());
        }
    }

    private void collectGroupLags() {
        try {
            String brokerAddr = mqAdmin.firstMasterBrokerAddr();
            if (brokerAddr == null) {
                return;
            }
            SubscriptionGroupWrapper wrapper = mqAdmin.getAllSubscriptionGroup(brokerAddr);
            LocalDateTime now = LocalDateTime.now();
            for (String group : wrapper.getSubscriptionGroupTable().keySet()) {
                try {
                    ConsumeStats stats = mqAdmin.examineConsumeStats(group);
                    long lag = stats.getOffsetTable() == null ? 0
                            : stats.getOffsetTable().values().stream()
                            .mapToLong(w -> Math.max(0, w.getBrokerOffset() - w.getConsumerOffset())).sum();
                    insert(null, group, MetricType.GROUP_LAG, lag, now);
                } catch (BizException e) {
                    log.debug("skip group {} during metrics collection: {}", group, e.getMessage());
                }
            }
        } catch (BizException e) {
            log.warn("failed to collect group lags: {}", e.getMessage());
        }
    }

    private void insert(String topic, String group, MetricType metricType, long value, LocalDateTime createdAt) {
        MetricsSnapshot snapshot = new MetricsSnapshot();
        snapshot.setTopic(topic);
        snapshot.setConsumerGroup(group);
        snapshot.setMetricType(metricType.name());
        snapshot.setMetricValue(value);
        snapshot.setCreatedAt(createdAt);
        snapshotMapper.insert(snapshot);
    }

    private boolean isUserTopic(String topic) {
        return !(topic.startsWith("%RETRY%")
                || topic.startsWith("%DLQ%")
                || topic.startsWith("RMQ_SYS_")
                || topic.startsWith("SCHEDULE_")
                || topic.equals("TBW102")
                || topic.equals("OFFSET_MOVED_EVENT")
                || topic.equals("DefaultCluster"));
    }
}
