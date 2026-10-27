package io.github.mqportal.audit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("audit_log")
public class AuditLogEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String module;

    private String action;

    private String method;

    private String params;

    private Boolean success;

    private String errorMessage;

    private Long durationMs;

    private LocalDateTime createdAt;
}
