# AI聊天统一接口文档

本文档介绍如何使用AI聊天统一接口，该接口兼容OpenAI的API格式，支持多个AI提供商。

## 接口地址

- **生产接口**: `POST /api/v1/chat/completions`
- **测试接口**: `POST /api/test/chat/simple`

## 认证

所有请求需要在请求头中包含JWT token：
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
  "model": "deepseek-chat",
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
  "id": "chatcmpl-123",
  "object": "chat.completion",
  "created": 1677652288,
  "model": "deepseek-chat",
  "system_fingerprint": "fp_44709d6fcb",
  "choices": [
    {
      "index": 0,
      "message": {
        "role": "assistant",
        "content": "你好！我是你的智能助手..."
      },
      "finish_reason": "stop"
    }
  ],
  "usage": {
    "prompt_tokens": 20,
    "completion_tokens": 30,
    "total_tokens": 50
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
- `deepseek-chat`: DeepSeek聊天模型

### 即将支持
- OpenAI GPT系列
- Anthropic Claude系列
- 其他主流AI模型

## 参数说明

| 参数 | 类型 | 必需 | 默认值 | 说明 |
|------|------|------|--------|------|
| model | string | 是 | - | 模型名称 |
| messages | array | 是 | - | 消息列表 |
| stream | boolean | 否 | false | 是否流式响应 |
| temperature | number | 否 | 1.0 | 温度参数(0-2) |
| max_tokens | integer | 否 | - | 最大token数 |
| top_p | number | 否 | 1.0 | top_p参数 |
| frequency_penalty | number | 否 | 0.0 | 频率惩罚(-2到2) |
| presence_penalty | number | 否 | 0.0 | 存在惩罚(-2到2) |
| stop | array | 否 | null | 停止词列表 |

## 计费说明

- 按实际消耗的token数量计费
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
- `401`: 未认证或token无效
- `403`: 权限不足
- `500`: 服务器内部错误

## 测试接口

### 简单测试
```http
POST /api/test/chat/simple?model=deepseek-chat&message=你好&userId=1
```

### 检查模型支持
```http
GET /api/test/chat/models?model=deepseek-chat
```

## 使用示例

### JavaScript
```javascript
const response = await fetch('/api/v1/chat/completions', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer ' + token
  },
  body: JSON.stringify({
    model: 'deepseek-chat',
    messages: [
      { role: 'user', content: '你好' }
    ],
    stream: false
  })
});

const result = await response.json();
console.log(result.choices[0].message.content);
```

### Python
```python
import requests

response = requests.post('/api/v1/chat/completions', 
  headers={
    'Content-Type': 'application/json',
    'Authorization': f'Bearer {token}'
  },
  json={
    'model': 'deepseek-chat',
    'messages': [
      {'role': 'user', 'content': '你好'}
    ],
    'stream': False
  }
)

result = response.json()
print(result['choices'][0]['message']['content'])
```

## 注意事项

1. 请求需要有效的JWT token
2. 确保账户有足够的积分余额
3. 流式响应需要正确处理SSE格式
4. 建议设置合理的超时时间
5. 大模型响应可能较慢，请耐心等待

## 数据库配置

在使用前，请确保已正确配置AI模型：

```sql
-- 执行sql/insert_deepseek_model.sql插入DeepSeek配置
```

## 故障排除

1. **"模型不可用"**: 检查数据库中模型配置是否启用
2. **"积分不足"**: 检查用户积分余额
3. **"连接超时"**: 检查AI服务商API是否可访问
4. **"认证失败"**: 检查JWT token是否有效
