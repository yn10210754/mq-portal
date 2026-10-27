package io.github.mqportal.topic.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("topic_meta")
public class TopicMeta {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String topic;

    private String owner;

    private String remark;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
