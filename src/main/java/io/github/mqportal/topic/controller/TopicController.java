package io.github.mqportal.topic.controller;

import io.github.mqportal.audit.AuditLog;
import io.github.mqportal.common.Result;
import io.github.mqportal.topic.dto.CreateTopicRequest;
import io.github.mqportal.topic.dto.TopicVO;
import io.github.mqportal.topic.dto.UpdateTopicRequest;
import io.github.mqportal.topic.service.TopicService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/topics")
@Tag(name = "topic")
public class TopicController {

    private final TopicService topicService;

    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    @GetMapping
    public Result<List<TopicVO>> list(@RequestParam(required = false) String keyword) {
        return Result.ok(topicService.list(keyword));
    }

    @GetMapping("/{topic}")
    public Result<TopicVO> detail(@PathVariable String topic) {
        return Result.ok(topicService.detail(topic));
    }

    @PostMapping
    @AuditLog(module = "topic", action = "create")
    public Result<Void> create(@Valid @RequestBody CreateTopicRequest request) {
        topicService.create(request);
        return Result.ok();
    }

    @PutMapping("/{topic}")
    @AuditLog(module = "topic", action = "update")
    public Result<Void> update(@PathVariable String topic, @Valid @RequestBody UpdateTopicRequest request) {
        topicService.update(topic, request);
        return Result.ok();
    }

    @DeleteMapping("/{topic}")
    @AuditLog(module = "topic", action = "delete")
    public Result<Void> delete(@PathVariable String topic) {
        topicService.delete(topic);
        return Result.ok();
    }
}
