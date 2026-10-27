package io.github.mqportal.audit.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.mqportal.audit.entity.AuditLogEntity;
import io.github.mqportal.audit.mapper.AuditLogMapper;
import io.github.mqportal.common.PageResult;
import io.github.mqportal.common.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit")
@Tag(name = "audit")
public class AuditController {

    private final AuditLogMapper auditLogMapper;

    public AuditController(AuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    @GetMapping("/logs")
    public Result<PageResult<AuditLogEntity>> logs(@RequestParam(required = false) String username,
                                                   @RequestParam(required = false) String module,
                                                   @RequestParam(required = false) String action,
                                                   @RequestParam(required = false) Boolean success,
                                                   @RequestParam(defaultValue = "1") long page,
                                                   @RequestParam(defaultValue = "20") long size) {
        Page<AuditLogEntity> result = auditLogMapper.selectPage(new Page<>(page, size),
                Wrappers.<AuditLogEntity>lambdaQuery()
                        .eq(username != null, AuditLogEntity::getUsername, username)
                        .eq(module != null, AuditLogEntity::getModule, module)
                        .eq(action != null, AuditLogEntity::getAction, action)
                        .eq(success != null, AuditLogEntity::getSuccess, success)
                        .orderByDesc(AuditLogEntity::getCreatedAt));
        return Result.ok(PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }
}
