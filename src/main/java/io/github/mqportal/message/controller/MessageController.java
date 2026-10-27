package io.github.mqportal.message.controller;

import io.github.mqportal.audit.AuditLog;
import io.github.mqportal.common.Result;
import io.github.mqportal.message.dto.ConsumeDirectlyResultVO;
import io.github.mqportal.message.dto.MessageVO;
import io.github.mqportal.message.dto.ResendDeadLetterRequest;
import io.github.mqportal.message.dto.ResetOffsetRequest;
import io.github.mqportal.message.service.MessageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/message")
@Tag(name = "message")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/query")
    public Result<List<MessageVO>> query(@RequestParam String topic,
                                         @RequestParam String key,
                                         @RequestParam(required = false) Long begin,
                                         @RequestParam(required = false) Long end,
                                         @RequestParam(required = false) Integer maxNum) {
        return Result.ok(messageService.query(topic, key, begin, end, maxNum));
    }

    @GetMapping("/{msgId}")
    public Result<MessageVO> detail(@PathVariable String msgId) {
        return Result.ok(messageService.detail(msgId));
    }

    @PostMapping("/dead-letter/resend")
    @AuditLog(module = "message", action = "dead-letter-resend")
    public Result<ConsumeDirectlyResultVO> resendDeadLetter(@Valid @RequestBody ResendDeadLetterRequest request) {
        return Result.ok(messageService.resendDeadLetter(request));
    }

    @PostMapping("/reset-offset")
    @AuditLog(module = "message", action = "reset-offset")
    public Result<Void> resetOffset(@Valid @RequestBody ResetOffsetRequest request) {
        messageService.resetOffset(request);
        return Result.ok();
    }
}
