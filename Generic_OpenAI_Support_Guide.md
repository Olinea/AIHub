# 通用OpenAI兼容API支持文档

## 概述

系统现在支持通用的OpenAI兼容API，这意味着您可以使用任何兼容OpenAI ChatCompletion API格式的服务提供商，包括但不限于：

- ✅ **阿里云通义千问** (Qwen) - 使用 `provider = "qwen"`
- ✅ **DeepSeek** - 使用 `provider = "deepseek"`
- ✅ **任何OpenAI兼容服务** - 使用 `provider = "other"`
- ✅ **OpenAI官方API** - 使用 `provider = "other"`
- ✅ **本地模型服务** (如Ollama, LocalAI等) - 使用 `provider = "other"`
- ✅ **其他AI服务商** - 使用 `provider = "other"`

## 路由规则

系统根据数据库中模型的 `provider` 字段自动选择服务：

| Provider值 | 使用的服务 | 说明 |
|-----------|-----------|------|
| `deepseek` | DeepSeekChatService | 专门优化的DeepSeek服务 |
| `qwen` | QwenChatService | 专门优化的Qwen服务 |
| `other` | GenericOpenAiChatService | 通用OpenAI兼容服务 |
| 其他任何值 | GenericOpenAiChatService | 默认使用通用服务 |

## 配置示例

### 1. 阿里云通义千问 (推荐使用专用服务)
```sql
INSERT INTO ai_models (
    model_name, display_name, provider, api_endpoint, api_key, 
    cost_per_1k_tokens, is_enabled, created_at, updated_at
) VALUES (
    'qwen-plus', '通义千问-Plus', 'qwen',
    'https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions',
    'your-qwen-api-key',
    0.004, 1, NOW(), NOW()
);
```

### 2. OpenAI官方API
```sql
INSERT INTO ai_models (
    model_name, display_name, provider, api_endpoint, api_key, 
    cost_per_1k_tokens, is_enabled, created_at, updated_at
) VALUES (
    'gpt-4', 'GPT-4', 'other',
    'https://api.openai.com/v1/chat/completions',
    'your-openai-api-key',
    0.03, 1, NOW(), NOW()
);
```

### 3. 本地Ollama服务
```sql
INSERT INTO ai_models (
    model_name, display_name, provider, api_endpoint, api_key, 
    cost_per_1k_tokens, is_enabled, created_at, updated_at
) VALUES (
    'llama2', 'Llama2本地模型', 'other',
    'http://localhost:11434/v1/chat/completions',
    'ollama', -- Ollama通常不需要真实API密钥
    0.0, 1, NOW(), NOW()
);
```

### 4. 其他云服务商 (如智谱AI、百度文心等)
```sql
INSERT INTO ai_models (
    model_name, display_name, provider, api_endpoint, api_key, 
    cost_per_1k_tokens, is_enabled, created_at, updated_at
) VALUES (
    'glm-4', '智谱GLM-4', 'other',
    'https://open.bigmodel.cn/api/paas/v4/chat/completions',
    'your-zhipu-api-key',
    0.005, 1, NOW(), NOW()
);
```

## 修复您当前的配置

根据您的查询结果，模型ID 5目前的配置是：
```
provider: Other  (应该改为小写 "other")
model_name: qwen-plus
api_endpoint: https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions
```

### 选项A：使用通用服务 (推荐)
```sql
UPDATE ai_models SET 
    provider = 'other',  -- 改为小写
    updated_at = NOW()
WHERE id = 5;
```

### 选项B：使用专门的Qwen服务 (更优化)
```sql
UPDATE ai_models SET 
    provider = 'qwen',  -- 使用专门的Qwen服务
    updated_at = NOW()
WHERE id = 5;
```

## 通用服务的特性

### ✅ 支持的功能
- 标准OpenAI ChatCompletion API格式
- 同步和流式响应
- 完整的参数支持 (temperature, max_tokens, top_p等)
- Token使用统计和计费
- 会话管理和消息存储
- 错误处理和重试机制
- 详细的日志记录

### 🔧 自动适配
- 自动处理不同服务商的细微差异
- 智能Token估算 (当服务商不返回usage时)
- 统一的错误处理
- 灵活的认证头处理

## 测试您的配置

执行SQL修复后，使用以下命令测试：

```bash
curl "http://localhost:5173/api/v1/chat/completions" \
  -H "Authorization: Bearer your-token" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 5,
    "conversationId": 81,
    "messages": [{"role": "user", "content": "你好"}],
    "stream": false,
    "temperature": 0.7
  }'
```

## 优势

### 1. **灵活性**
- 支持任何OpenAI兼容的API
- 无需为每个服务商单独开发

### 2. **可扩展性**
- 添加新的AI服务商只需配置数据库
- 无需修改代码

### 3. **统一性**
- 所有服务使用相同的接口
- 统一的计费和会话管理

### 4. **兜底机制**
- 未知的provider自动使用通用服务
- 确保系统的鲁棒性

## 常见使用场景

### 1. 多模型对比
可以同时配置多个不同的AI服务进行对比测试

### 2. 成本控制
将不同任务路由到不同成本的模型

### 3. 本地部署
支持完全离线的本地AI模型

### 4. 备用方案
主服务不可用时自动切换到备用服务

现在您的系统已经具备了强大的AI服务兼容性！🚀
