package io.github.mqportal.consumer.controller;

import io.github.mqportal.audit.AuditLog;
import io.github.mqportal.common.Result;
import io.github.mqportal.consumer.dto.ConnectionVO;
import io.github.mqportal.consumer.dto.ConsumeStatsVO;
import io.github.mqportal.consumer.dto.CreateGroupRequest;
import io.github.mqportal.consumer.dto.GroupVO;
import io.github.mqportal.consumer.dto.RetryTopicVO;
import io.github.mqportal.consumer.service.ConsumerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/consumer/groups")
@Tag(name = "consumer")
public class ConsumerController {

    private final ConsumerService consumerService;

    public ConsumerController(ConsumerService consumerService) {
        this.consumerService = consumerService;
    }

    @GetMapping
    public Result<List<GroupVO>> list() {
        return Result.ok(consumerService.list());
    }

    @GetMapping("/{group}")
    public Result<GroupVO> detail(@PathVariable String group) {
        return Result.ok(consumerService.detail(group));
    }

    @PostMapping
    @AuditLog(module = "consumer", action = "create")
    public Result<Void> create(@Valid @RequestBody CreateGroupRequest request) {
        consumerService.create(request);
        return Result.ok();
    }

    @DeleteMapping("/{group}")
    @AuditLog(module = "consumer", action = "delete")
    public Result<Void> delete(@PathVariable String group) {
        consumerService.delete(group);
        return Result.ok();
    }

    @GetMapping("/{group}/connections")
    public Result<List<ConnectionVO>> connections(@PathVariable String group) {
        return Result.ok(consumerService.connections(group));
    }

    @GetMapping("/{group}/consume-stats")
    public Result<ConsumeStatsVO> consumeStats(@PathVariable String group) {
        return Result.ok(consumerService.consumeStats(group));
    }

    @GetMapping("/{group}/retry-topics")
    public Result<List<RetryTopicVO>> retryTopics(@PathVariable String group) {
        return Result.ok(consumerService.retryTopics(group));
    }
}
