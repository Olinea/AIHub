# AI模型API连接测试功能

## 功能概述

本功能实现了对多种AI服务商的API连接测试，支持以下提供商：

- **OpenAI 及其兼容服务**: 支持标准的OpenAI API格式
- **Anthropic (Claude)**: 支持Claude API格式
- **Google (Gemini)**: 支持Google AI API格式
- **百度文心一言**: 支持百度API格式
- **阿里巴巴通义千问**: 支持阿里云API格式
- **智谱AI (ChatGLM)**: 支持智谱API格式
- **其他兼容OpenAI格式的服务**: 如Azure OpenAI、各种开源模型服务等

## API接口

### 测试API连接

**请求方式**: `POST`
**请求路径**: `/api/admin/ai-models/{id}/test-connection`
**权限要求**: 需要管理员权限

**路径参数**:
- `id`: 模型ID (整数)

**响应示例**:

```json
{
    "success": true,
    "message": "API连接测试完成",
    "data": "🔍 测试模型信息:\n模型名称: gpt-4\n提供商: OpenAI\nAPI端点: https://api.openai.com/v1/chat/completions\nAPI密钥: sk-proj-***\n组织ID: org-xxx\n\n📊 测试结果:\n✅ OpenAI兼容API连接测试成功\n响应模型: gpt-4\n响应内容: Hello! This is a test message."
}
```

### 显示模型配置

**请求方式**: `GET`
**请求路径**: `/api/admin/ai-models/{id}/show-config`
**权限要求**: 需要管理员权限

## 支持的配置字段

在AI模型配置中，以下字段会被用于API连接测试：

- **modelName**: 模型名称（必需）
- **provider**: 提供商名称（用于选择API格式）
- **apiEndpoint**: API端点URL（必需）
- **apiKey**: API密钥（必需）
- **apiSecret**: API密钥Secret（某些厂商需要）
- **organizationId**: 组织ID（OpenAI等需要）
- **projectId**: 项目ID（某些厂商需要）
- **extraHeaders**: 额外的请求头（JSON格式）

## 提供商特定配置

### OpenAI及兼容服务
```json
{
    "modelName": "gpt-4",
    "provider": "OpenAI",
    "apiEndpoint": "https://api.openai.com/v1/chat/completions",
    "apiKey": "sk-proj-xxxxx",
    "organizationId": "org-xxxxx",
    "projectId": "proj_xxxxx"
}
```

### Anthropic (Claude)
```json
{
    "modelName": "claude-3-opus-20240229",
    "provider": "Anthropic",
    "apiEndpoint": "https://api.anthropic.com/v1/messages",
    "apiKey": "sk-ant-xxxxx"
}
```

### Google (Gemini)
```json
{
    "modelName": "gemini-pro",
    "provider": "Google",
    "apiEndpoint": "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent",
    "apiKey": "AIzaSyxxxxx"
}
```

### Azure OpenAI
```json
{
    "modelName": "gpt-4",
    "provider": "OpenAI",
    "apiEndpoint": "https://your-resource.openai.azure.com/openai/deployments/gpt-4/chat/completions?api-version=2024-02-15-preview",
    "apiKey": "your-azure-key",
    "extraHeaders": "{\"api-key\": \"your-azure-key\"}"
}
```

## 测试机制

1. **连接验证**: 发送简单的测试消息到AI服务
2. **响应解析**: 验证API响应格式的正确性
3. **错误处理**: 详细记录和报告连接错误
4. **安全保护**: 在日志和响应中掩码敏感信息

## 错误处理

系统会捕获并报告以下类型的错误：
- 网络连接错误
- 认证失败
- API配额限制
- 响应格式错误
- 超时错误

## 安全考虑

- API密钥在响应中会被掩码处理
- 敏感信息不会记录到日志中
- 所有API调用都有超时限制（30秒）
- 支持自定义请求头以满足特殊认证需求
