# 数据库修改建议 - OpenAI格式兼容性分析

## 现状分析

当前数据库设计与OpenAI API格式对比：

### 当前Messages表结构
```sql
CREATE TABLE messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id BIGINT,
    role VARCHAR(10), 
    content TEXT,
    model_id INT,
    tokens_consumed INT,
    created_at DATETIME
);
```

### 当前Conversations表结构
```sql
CREATE TABLE conversations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    title VARCHAR(255),
    created_at DATETIME
);
```

## OpenAI API格式要求

### 1. 消息格式扩展需求

OpenAI API支持以下消息字段：
- `role`: "system", "user", "assistant", "tool", "function"
- `content`: 消息内容（可以是字符串或结构化内容）
- `name`: 可选，消息发送者名称
- `tool_calls`: 工具调用信息（新版API）
- `function_call`: 函数调用信息（旧版API，已废弃）
- `tool_call_id`: 工具调用响应时的ID

### 2. 响应元数据需求

OpenAI响应包含：
- `finish_reason`: "stop", "length", "function_call", "tool_calls", "content_filter"
- `usage`: token使用统计
- `system_fingerprint`: 系统指纹

## 建议的数据库修改

### 1. Messages表扩展

```sql
ALTER TABLE messages 
ADD COLUMN name VARCHAR(100) COMMENT '消息发送者名称（可选）',
ADD COLUMN finish_reason VARCHAR(20) COMMENT '完成原因：stop/length/tool_calls等',
ADD COLUMN tool_calls JSON COMMENT '工具调用信息（JSON格式）',
ADD COLUMN tool_call_id VARCHAR(50) COMMENT '工具调用响应ID',
ADD COLUMN system_fingerprint VARCHAR(100) COMMENT '系统指纹',
ADD COLUMN prompt_tokens INT COMMENT '提示词token数',
ADD COLUMN completion_tokens INT COMMENT '完成token数',
ADD COLUMN total_tokens INT COMMENT '总token数（冗余字段，等于tokens_consumed）';

-- 修改role字段长度以支持更多角色类型
ALTER TABLE messages MODIFY COLUMN role VARCHAR(20);

-- 添加索引优化查询
CREATE INDEX idx_messages_conversation_created ON messages(conversation_id, created_at);
CREATE INDEX idx_messages_role ON messages(role);
```

### 2. Conversations表扩展

```sql
ALTER TABLE conversations
ADD COLUMN model_id INT COMMENT '会话默认模型ID',
ADD COLUMN updated_at DATETIME COMMENT '最后更新时间',
ADD COLUMN message_count INT DEFAULT 0 COMMENT '消息数量',
ADD COLUMN total_tokens INT DEFAULT 0 COMMENT '会话总token消耗',
ADD COLUMN status VARCHAR(20) DEFAULT 'active' COMMENT '会话状态：active/archived/deleted';

-- 添加索引
CREATE INDEX idx_conversations_user_updated ON conversations(user_id, updated_at DESC);
CREATE INDEX idx_conversations_status ON conversations(status);
```

### 3. 新增表：conversation_settings

为了更好地支持不同AI模型的个性化设置：

```sql
CREATE TABLE conversation_settings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id BIGINT NOT NULL,
    temperature DECIMAL(3,2) DEFAULT 1.0 COMMENT '温度参数',
    max_tokens INT COMMENT '最大token数',
    top_p DECIMAL(3,2) DEFAULT 1.0 COMMENT 'top_p参数',
    frequency_penalty DECIMAL(3,2) DEFAULT 0.0 COMMENT '频率惩罚',
    presence_penalty DECIMAL(3,2) DEFAULT 0.0 COMMENT '存在惩罚',
    stop_sequences JSON COMMENT '停止序列',
    system_message TEXT COMMENT '系统提示词',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    UNIQUE KEY uk_conversation_settings (conversation_id)
);
```

### 4. 新增表：message_attachments

为了支持未来的多模态功能（图片、文件等）：

```sql
CREATE TABLE message_attachments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    message_id BIGINT NOT NULL,
    attachment_type VARCHAR(20) NOT NULL COMMENT '附件类型：image/file/audio等',
    file_url VARCHAR(500) COMMENT '文件URL',
    file_name VARCHAR(255) COMMENT '文件名',
    file_size BIGINT COMMENT '文件大小（字节）',
    mime_type VARCHAR(100) COMMENT 'MIME类型',
    metadata JSON COMMENT '附件元数据',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE,
    INDEX idx_attachments_message (message_id),
    INDEX idx_attachments_type (attachment_type)
);
```

## 数据库迁移脚本

### 第一阶段：基础兼容性修改

```sql
-- 1. 扩展messages表
ALTER TABLE messages 
ADD COLUMN name VARCHAR(100) COMMENT '消息发送者名称',
ADD COLUMN finish_reason VARCHAR(20) COMMENT '完成原因',
ADD COLUMN tool_calls JSON COMMENT '工具调用信息',
ADD COLUMN tool_call_id VARCHAR(50) COMMENT '工具调用响应ID',
ADD COLUMN system_fingerprint VARCHAR(100) COMMENT '系统指纹',
ADD COLUMN prompt_tokens INT COMMENT '提示词token数',
ADD COLUMN completion_tokens INT COMMENT '完成token数';

-- 2. 修改role字段长度
ALTER TABLE messages MODIFY COLUMN role VARCHAR(20);

-- 3. 为total_tokens添加计算字段
ALTER TABLE messages ADD COLUMN total_tokens INT AS (COALESCE(prompt_tokens, 0) + COALESCE(completion_tokens, 0)) STORED;

-- 4. 扩展conversations表
ALTER TABLE conversations
ADD COLUMN model_id INT COMMENT '默认模型ID',
ADD COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
ADD COLUMN message_count INT DEFAULT 0,
ADD COLUMN total_tokens INT DEFAULT 0,
ADD COLUMN status VARCHAR(20) DEFAULT 'active';

-- 5. 添加索引
CREATE INDEX idx_messages_conversation_created ON messages(conversation_id, created_at);
CREATE INDEX idx_conversations_user_updated ON conversations(user_id, updated_at DESC);
```

### 第二阶段：高级功能支持

```sql
-- 创建会话设置表
CREATE TABLE conversation_settings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id BIGINT NOT NULL,
    temperature DECIMAL(3,2) DEFAULT 1.0,
    max_tokens INT,
    top_p DECIMAL(3,2) DEFAULT 1.0,
    frequency_penalty DECIMAL(3,2) DEFAULT 0.0,
    presence_penalty DECIMAL(3,2) DEFAULT 0.0,
    stop_sequences JSON,
    system_message TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    UNIQUE KEY uk_conversation_settings (conversation_id)
);

-- 创建附件表（为未来多模态做准备）
CREATE TABLE message_attachments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    message_id BIGINT NOT NULL,
    attachment_type VARCHAR(20) NOT NULL,
    file_url VARCHAR(500),
    file_name VARCHAR(255),
    file_size BIGINT,
    mime_type VARCHAR(100),
    metadata JSON,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE
);
```

## Java实体类修改

### Messages实体类需要添加的字段

```java
@TableField("name")
private String name;

@TableField("finish_reason")
private String finishReason;

@TableField("tool_calls")
private String toolCalls; // JSON字符串

@TableField("tool_call_id")
private String toolCallId;

@TableField("system_fingerprint")
private String systemFingerprint;

@TableField("prompt_tokens")
private Integer promptTokens;

@TableField("completion_tokens")
private Integer completionTokens;

@TableField("total_tokens")
private Integer totalTokens;
```

### Conversations实体类需要添加的字段

```java
@TableField("model_id")
private Integer modelId;

@TableField("updated_at")
private LocalDateTime updatedAt;

@TableField("message_count")
private Integer messageCount;

@TableField("total_tokens")
private Integer totalTokens;

@TableField("status")
private String status;
```

## 兼容性收益

修改后的数据库将支持：

1. ✅ 完整的OpenAI消息格式
2. ✅ 工具调用功能（ChatGPT函数调用）
3. ✅ 详细的token统计
4. ✅ 多种角色支持（system, user, assistant, tool）
5. ✅ 消息命名功能
6. ✅ 完成原因追踪
7. ✅ 会话参数个性化
8. ✅ 多模态扩展能力

## 迁移建议

1. **阶段性迁移**：分两个阶段执行，先基础兼容性，后高级功能
2. **数据备份**：执行前务必备份数据库
3. **向后兼容**：新字段设为可空，确保现有代码正常运行
4. **逐步启用**：新功能可以逐步启用，不影响现有功能
5. **性能测试**：添加索引后进行性能测试

## 总结

建议进行数据库修改以实现完整的OpenAI兼容性。这些修改不仅支持当前需求，还为未来的功能扩展（如工具调用、多模态、个性化设置）奠定了基础。修改是向后兼容的，不会影响现有功能。
