# AI 聊天统一接口文档

本文档介绍如何使用 AI 聊天统一接口，该接口模仿 OpenAI 的 API 格式，支持多个 AI 提供商。唯一不同的是模型 ID 需要在数据库中配置，且在api请求时使用数据库中的模型 ID，而不是直接使用模型名称。


## GET 获取启用的AI模型列表

GET /api/ai-models/enabled

获取所有启用状态的AI模型供用户选择

> 返回示例

> 200 Response

```
{
    "code": 200,
    "message": "获取启用的AI模型列表成功",
    "data": [
        {
            "id": 4,
            "modelName": "deepseek-chat",
            "provider": "deepseek",
            "costPer1kTokens": 0.014000
        },
        {
            "id": 2,
            "modelName": "gemini-2.0-flash:generateContent",
            "provider": "Google",
            "costPer1kTokens": 0.010000
        }
    ],
    "timestamp": 1752046115108
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[ApiResponseListAiModels](#schemaapiresponselistaimodels)|


## ai对话接口地址


- **生产接口**: `POST /api/v1/chat/completions`

## 认证

所有请求需要在请求头中包含 JWT token：

```
Authorization: Bearer <your-jwt-token>
```

## 请求格式

### 标准聊天接口

```http
POST /api/v1/chat/completions
Content-Type: application/json
Authorization: Bearer <your-jwt-token>

{
  "id": 4, //对应数据库中的模型ID
  "messages": [
    {
      "role": "system",
      "content": "You are a helpful assistant."
    },
    {
      "role": "user",
      "content": "你好，请简单介绍一下你自己"
    }
  ],
  "stream": false,
  "temperature": 0.7,
  "max_tokens": 2000
}
```

### 流式响应

设置 `"stream": true` 开启流式响应：

```http
POST /api/v1/chat/completions
Content-Type: application/json
Authorization: Bearer <your-jwt-token>

{
  "model": "deepseek-chat",
  "messages": [
    {
      "role": "user",
      "content": "讲一个故事"
    }
  ],
  "stream": true
}
```

## 响应格式

### 同步响应

```json
{
  "id": "c3744b75-97a9-40b8-a2e6-2a0597327a6a",
  "object": "chat.completion",
  "created": 1752045449,
  "model": "deepseek-chat",
  "systemFingerprint": null,
  "choices": [
    {
      "index": 0,
      "message": {
        "role": "assistant",
        "content": "你好！我是DeepSeek Chat",
        "name": null
      },
      "delta": null,
      "finishReason": null
    }
  ],
  "usage": {
    "promptTokens": null,
    "completionTokens": null,
    "totalTokens": null,
    "promptTokensDetails": null,
    "promptCacheHitTokens": null,
    "promptCacheMissTokens": null
  }
}
```

### 流式响应

```
data: {"id":"chatcmpl-123","object":"chat.completion.chunk","created":1677652288,"model":"deepseek-chat","choices":[{"index":0,"delta":{"content":"你好"},"finish_reason":null}]}

data: {"id":"chatcmpl-123","object":"chat.completion.chunk","created":1677652288,"model":"deepseek-chat","choices":[{"index":0,"delta":{"content":"！"},"finish_reason":null}]}

data: [DONE]
```

## 支持的模型

### DeepSeek

- `deepseek-chat`: DeepSeek 聊天模型

### 即将支持

- OpenAI GPT 系列
- Anthropic Claude 系列
- 其他主流 AI 模型

## 参数说明

| 参数              | 类型    | 必需 | 默认值 | 说明              |
| ----------------- | ------- | ---- | ------ | ----------------- |
| id                | number  | 是   | -      | 模型 ID           |
| messages          | array   | 是   | -      | 消息列表          |
| stream            | boolean | 否   | false  | 是否流式响应      |
| temperature       | number  | 否   | 1.0    | 温度参数(0-2)     |
| max_tokens        | integer | 否   | -      | 最大 token 数     |
| top_p             | number  | 否   | 1.0    | top_p 参数        |
| frequency_penalty | number  | 否   | 0.0    | 频率惩罚(-2 到 2) |
| presence_penalty  | number  | 否   | 0.0    | 存在惩罚(-2 到 2) |
| stop              | array   | 否   | null   | 停止词列表        |

## 计费说明

- 按实际消耗的 token 数量计费
- 不同模型有不同的单价
- 计费公式：`费用 = (总token数 / 1000) × 模型单价`
- 费用将自动从用户积分余额中扣除

## 错误处理

### 常见错误

```json
{
  "success": false,
  "message": "积分余额不足，请充值",
  "data": null
}
```

```json
{
  "success": false,
  "message": "不支持的模型: invalid-model",
  "data": null
}
```

### 错误代码

- `400`: 请求参数错误
- `401`: 未认证或 token 无效
- `403`: 权限不足
- `500`: 服务器内部错误
