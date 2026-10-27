package io.github.mqportal.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mqportal.audit.entity.AuditLogEntity;
import io.github.mqportal.audit.mapper.AuditLogMapper;
import io.github.mqportal.auth.CurrentUser;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.time.LocalDateTime;

@Aspect
@Component
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);
    private static final int MAX_PARAM_LENGTH = 2000;
    private static final int MAX_MESSAGE_LENGTH = 2000;

    private final AuditLogMapper auditLogMapper;
    private final ObjectMapper objectMapper;

    public AuditAspect(AuditLogMapper auditLogMapper, ObjectMapper objectMapper) {
        this.auditLogMapper = auditLogMapper;
        this.objectMapper = objectMapper;
    }

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        AuditLogEntity entity = new AuditLogEntity();
        entity.setUsername(CurrentUser.username());
        entity.setModule(auditLog.module());
        entity.setAction(auditLog.action());
        entity.setMethod(joinPoint.getSignature().toShortString());
        entity.setParams(summarizeParams(joinPoint.getArgs()));
        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            entity.setSuccess(true);
            return result;
        } catch (Throwable throwable) {
            entity.setSuccess(false);
            entity.setErrorMessage(truncate(throwable.getMessage(), MAX_MESSAGE_LENGTH));
            throw throwable;
        } finally {
            entity.setDurationMs(System.currentTimeMillis() - start);
            entity.setCreatedAt(LocalDateTime.now());
            try {
                auditLogMapper.insert(entity);
            } catch (Exception e) {
                log.warn("failed to persist audit log: {}", e.getMessage());
            }
        }
    }

    private String summarizeParams(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (Object arg : args) {
            if (isIgnorable(arg)) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(", ");
            }
            try {
                builder.append(objectMapper.writeValueAsString(arg));
            } catch (Exception e) {
                builder.append(arg);
            }
        }
        return truncate(builder.toString(), MAX_PARAM_LENGTH);
    }

    private boolean isIgnorable(Object arg) {
        return arg == null
                || arg instanceof HttpServletRequest
                || arg instanceof HttpServletResponse
                || arg instanceof ServletRequest
                || arg instanceof ServletResponse
                || arg instanceof BindingResult;
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
