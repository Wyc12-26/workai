CREATE TABLE IF NOT EXISTS `conversation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title` VARCHAR(255) DEFAULT NULL COMMENT '对话标题',
    `user_id` VARCHAR(64) DEFAULT NULL COMMENT '用户ID',
    `created_at` DATETIME DEFAULT NULL COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT NULL COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标识',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对话表';

CREATE TABLE IF NOT EXISTS `message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `conversation_id` BIGINT DEFAULT NULL COMMENT '对话ID',
    `role` VARCHAR(32) DEFAULT NULL COMMENT '角色：user/assistant',
    `content` TEXT DEFAULT NULL COMMENT '消息内容',
    `file_id` VARCHAR(64) DEFAULT NULL COMMENT '关联文件ID',
    `file_name` VARCHAR(255) DEFAULT NULL COMMENT '文件名',
    `function_name` VARCHAR(64) DEFAULT NULL COMMENT '功能名称',
    `created_at` DATETIME DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_conversation_id` (`conversation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';
