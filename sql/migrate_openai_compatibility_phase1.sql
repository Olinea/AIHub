-- OpenAI兼容性数据库迁移脚本 - 第一阶段
-- 执行前请备份数据库！

-- 1. 扩展messages表以支持OpenAI完整格式
ALTER TABLE messages 
ADD COLUMN `name` VARCHAR(100) NULL COMMENT '消息发送者名称（OpenAI可选字段）',
ADD COLUMN `finish_reason` VARCHAR(20) NULL COMMENT '完成原因：stop/length/tool_calls/content_filter等',
ADD COLUMN `tool_calls` JSON NULL COMMENT '工具调用信息（JSON格式存储）',
ADD COLUMN `tool_call_id` VARCHAR(50) NULL COMMENT '工具调用响应ID',
ADD COLUMN `system_fingerprint` VARCHAR(100) NULL COMMENT '系统指纹',
ADD COLUMN `prompt_tokens` INT NULL COMMENT '提示词token数量',
ADD COLUMN `completion_tokens` INT NULL COMMENT '完成响应token数量',
ADD COLUMN `total_tokens` INT NULL COMMENT '总token数量（自动计算）';

-- 2. 修改role字段长度以支持更多角色类型
ALTER TABLE messages MODIFY COLUMN `role` VARCHAR(20) NOT NULL COMMENT '角色：system/user/assistant/tool/function';

-- 3. 添加计算列用于自动计算总token数
-- 注意：如果你的MySQL版本不支持计算列，可以通过触发器或应用层计算
ALTER TABLE messages ADD COLUMN `total_tokens_calculated` INT GENERATED ALWAYS AS (
    COALESCE(prompt_tokens, 0) + COALESCE(completion_tokens, 0)
) STORED COMMENT '自动计算的总token数';

-- 4. 扩展conversations表
ALTER TABLE conversations
ADD COLUMN `model_id` INT NULL COMMENT '会话默认使用的模型ID',
ADD COLUMN `updated_at` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
ADD COLUMN `message_count` INT DEFAULT 0 COMMENT '会话中消息数量',
ADD COLUMN `total_tokens` INT DEFAULT 0 COMMENT '会话总token消耗',
ADD COLUMN `status` VARCHAR(20) DEFAULT 'active' COMMENT '会话状态：active/archived/deleted';

-- 5. 添加外键约束（如果不存在）
-- 检查是否已有外键约束
SET @constraint_exists = (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
    AND TABLE_NAME = 'conversations'
    AND CONSTRAINT_NAME = 'fk_conversations_user'
);

-- 如果外键不存在，则添加
SET @sql = IF(@constraint_exists = 0,
    'ALTER TABLE conversations ADD CONSTRAINT fk_conversations_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE',
    'SELECT "外键 fk_conversations_user 已存在" as status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查messages表外键
SET @constraint_exists = (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
    AND TABLE_NAME = 'messages'
    AND CONSTRAINT_NAME = 'fk_messages_conversation'
);

SET @sql = IF(@constraint_exists = 0,
    'ALTER TABLE messages ADD CONSTRAINT fk_messages_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE',
    'SELECT "外键 fk_messages_conversation 已存在" as status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 6. 添加性能优化索引
CREATE INDEX IF NOT EXISTS `idx_messages_conversation_created` ON messages(`conversation_id`, `created_at`);
CREATE INDEX IF NOT EXISTS `idx_messages_role` ON messages(`role`);
CREATE INDEX IF NOT EXISTS `idx_conversations_user_updated` ON conversations(`user_id`, `updated_at` DESC);
CREATE INDEX IF NOT EXISTS `idx_conversations_status` ON conversations(`status`);
CREATE INDEX IF NOT EXISTS `idx_messages_finish_reason` ON messages(`finish_reason`);

-- 7. 更新现有数据的默认值
-- 为现有消息设置默认的finish_reason
UPDATE messages 
SET finish_reason = 'stop' 
WHERE finish_reason IS NULL AND role = 'assistant';

-- 为现有会话设置updated_at
UPDATE conversations 
SET updated_at = created_at 
WHERE updated_at IS NULL;

-- 更新会话的消息计数
UPDATE conversations c 
SET message_count = (
    SELECT COUNT(*) 
    FROM messages m 
    WHERE m.conversation_id = c.id
)
WHERE message_count = 0;

-- 更新会话的token消耗统计
UPDATE conversations c 
SET total_tokens = (
    SELECT COALESCE(SUM(tokens_consumed), 0)
    FROM messages m 
    WHERE m.conversation_id = c.id
)
WHERE total_tokens = 0;

-- 8. 验证数据迁移
SELECT 
    '数据库结构验证' as check_type,
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM information_schema.COLUMNS 
            WHERE TABLE_SCHEMA = DATABASE() 
            AND TABLE_NAME = 'messages' 
            AND COLUMN_NAME = 'tool_calls'
        ) THEN '✓ messages表已成功扩展'
        ELSE '✗ messages表扩展失败'
    END as messages_status,
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM information_schema.COLUMNS 
            WHERE TABLE_SCHEMA = DATABASE() 
            AND TABLE_NAME = 'conversations' 
            AND COLUMN_NAME = 'status'
        ) THEN '✓ conversations表已成功扩展'
        ELSE '✗ conversations表扩展失败'
    END as conversations_status;

-- 显示表结构摘要
SELECT 
    'messages表字段统计' as info_type,
    COUNT(*) as total_columns
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'messages';

SELECT 
    'conversations表字段统计' as info_type,
    COUNT(*) as total_columns
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'conversations';

-- 完成提示
SELECT 
    '迁移完成' as status,
    NOW() as completed_at,
    '数据库已更新为OpenAI兼容格式' as message;
