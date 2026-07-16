package com.workai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("message")
public class Message {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long conversationId;

    private String role;

    private String content;

    private String fileId;

    private String fileName;

    private String functionName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
