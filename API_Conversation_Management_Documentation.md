# 对话管理API接口文档

## 接口概述

本文档描述了用户对话管理相关的API接口，包括获取对话列表、查看对话详情、以及对话的增删改查操作。

## 基础信息

- **基础路径**: `/api/v1/conversations`
- **认证方式**: JWT Bearer Token
- **响应格式**: 统一使用 `ApiResponse<T>` 格式

## 接口列表

### 1. 获取对话列表

#### 1.1 获取全部对话列表

**请求信息**
```
GET /api/v1/conversations
Authorization: Bearer <JWT_TOKEN>
```

**响应示例**
```json
{
  "code": 200,
  "message": "获取对话列表成功",
  "data": [
    {
      "id": 1,
      "title": "关于AI的讨论",
      "status": "active",
      "modelId": 1,
      "modelName": "deepseek-chat",
      "messageCount": 10,
      "totalTokens": 500,
      "lastMessageContent": "我理解了，谢谢你的解释...",
      "lastMessageTime": "2025-07-09T10:30:00",
      "createdAt": "2025-07-09T09:00:00",
      "updatedAt": "2025-07-09T10:30:00"
    }
  ],
  "timestamp": 1625644800000
}
```

#### 1.2 分页获取对话列表

**请求信息**
```
GET /api/v1/conversations/page?current=1&size=20
Authorization: Bearer <JWT_TOKEN>
```

**请求参数**
- `current`: 当前页数（默认：1）
- `size`: 每页大小（默认：20）

**响应示例**
```json
{
  "code": 200,
  "message": "获取对话列表成功",
  "data": {
    "current": 1,
    "size": 20,
    "total": 100,
    "pages": 5,
    "records": [
      {
        "id": 1,
        "title": "关于AI的讨论",
        "status": "active",
        "modelId": 1,
        "modelName": "deepseek-chat",
        "messageCount": 10,
        "totalTokens": 500,
        "lastMessageContent": "我理解了，谢谢你的解释...",
        "lastMessageTime": "2025-07-09T10:30:00",
        "createdAt": "2025-07-09T09:00:00",
        "updatedAt": "2025-07-09T10:30:00"
      }
    ]
  },
  "timestamp": 1625644800000
}
```

### 2. 获取对话详情

#### 2.1 获取对话详情（包含全部消息）

**请求信息**
```
GET /api/v1/conversations/{conversationId}
Authorization: Bearer <JWT_TOKEN>
```

**路径参数**
- `conversationId`: 对话ID

**响应示例**
```json
{
  "code": 200,
  "message": "获取对话详情成功",
  "data": {
    "id": 1,
    "title": "关于AI的讨论",
    "status": "active",
    "modelId": 1,
    "modelName": "deepseek-chat",
    "messageCount": 10,
    "totalTokens": 500,
    "createdAt": "2025-07-09T09:00:00",
    "updatedAt": "2025-07-09T10:30:00",
    "messages": [
      {
        "id": 1,
        "role": "user",
        "content": "你好，请介绍一下你自己",
        "name": null,
        "modelId": 1,
        "modelName": "deepseek-chat",
        "tokensConsumed": 25,
        "promptTokens": 10,
        "completionTokens": 15,
        "totalTokens": 25,
        "finishReason": "stop",
        "toolCalls": null,
        "toolCallId": null,
        "systemFingerprint": "fp_44709d6fcb",
        "createdAt": "2025-07-09T09:05:00"
      },
      {
        "id": 2,
        "role": "assistant",
        "content": "你好！我是你的智能助手...",
        "name": null,
        "modelId": 1,
        "modelName": "deepseek-chat",
        "tokensConsumed": 45,
        "promptTokens": 20,
        "completionTokens": 25,
        "totalTokens": 45,
        "finishReason": "stop",
        "toolCalls": null,
        "toolCallId": null,
        "systemFingerprint": "fp_44709d6fcb",
        "createdAt": "2025-07-09T09:05:30"
      }
    ]
  },
  "timestamp": 1625644800000
}
```

#### 2.2 获取对话详情（分页消息）

**请求信息**
```
GET /api/v1/conversations/{conversationId}/messages?messageCurrent=1&messageSize=50
Authorization: Bearer <JWT_TOKEN>
```

**路径参数**
- `conversationId`: 对话ID

**请求参数**
- `messageCurrent`: 消息当前页数（默认：1）
- `messageSize`: 消息每页大小（默认：50）

**响应格式**: 与2.1相同，但`messages`字段为分页结果

### 3. 对话管理操作

#### 3.1 更新对话标题

**请求信息**
```
PUT /api/v1/conversations/{conversationId}/title
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "title": "关于人工智能的深度讨论"
}
```

**路径参数**
- `conversationId`: 对话ID

**请求体**
```json
{
  "title": "新的对话标题"
}
```

**响应示例**
```json
{
  "code": 200,
  "message": "更新对话标题成功",
  "data": null,
  "timestamp": 1625644800000
}
```

#### 3.2 归档对话

**请求信息**
```
PUT /api/v1/conversations/{conversationId}/archive
Authorization: Bearer <JWT_TOKEN>
```

**路径参数**
- `conversationId`: 对话ID

**响应示例**
```json
{
  "code": 200,
  "message": "归档对话成功",
  "data": null,
  "timestamp": 1625644800000
}
```

#### 3.3 删除对话

**请求信息**
```
DELETE /api/v1/conversations/{conversationId}
Authorization: Bearer <JWT_TOKEN>
```

**路径参数**
- `conversationId`: 对话ID

**响应示例**
```json
{
  "code": 200,
  "message": "删除对话成功",
  "data": null,
  "timestamp": 1625644800000
}
```

## 数据模型

### ConversationListResponse（对话列表项）

| 字段名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 会话ID |
| title | String | 会话标题 |
| status | String | 会话状态（active/archived/deleted） |
| modelId | Integer | 默认模型ID |
| modelName | String | 模型名称 |
| messageCount | Integer | 消息数量 |
| totalTokens | Integer | 总token消耗 |
| lastMessageContent | String | 最后一条消息内容（预览） |
| lastMessageTime | LocalDateTime | 最后一条消息时间 |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |

### ConversationDetailResponse（对话详情）

| 字段名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 会话ID |
| title | String | 会话标题 |
| status | String | 会话状态 |
| modelId | Integer | 默认模型ID |
| modelName | String | 模型名称 |
| messageCount | Integer | 消息数量 |
| totalTokens | Integer | 总token消耗 |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |
| messages | List&lt;MessageDetailResponse&gt; | 消息列表 |

### MessageDetailResponse（消息详情）

| 字段名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 消息ID |
| role | String | 角色（system/user/assistant/tool/function） |
| content | String | 消息内容 |
| name | String | 消息发送者名称 |
| modelId | Integer | AI模型ID |
| modelName | String | 模型名称 |
| tokensConsumed | Integer | 消耗的token数 |
| promptTokens | Integer | 提示词token数 |
| completionTokens | Integer | 完成token数 |
| totalTokens | Integer | 总token数 |
| finishReason | String | 完成原因 |
| toolCalls | String | 工具调用信息（JSON） |
| toolCallId | String | 工具调用ID |
| systemFingerprint | String | 系统指纹 |
| createdAt | LocalDateTime | 创建时间 |

## 错误处理

### 常见错误码

- **400**: 请求参数错误
- **401**: 未认证或token无效
- **403**: 无权限访问
- **404**: 资源不存在
- **500**: 服务器内部错误

### 错误响应格式

```json
{
  "code": 400,
  "message": "对话不存在或无权限访问",
  "data": null,
  "timestamp": 1625644800000
}
```

## 使用说明

### 权限控制

- 所有接口都需要JWT认证
- 用户只能访问自己的对话
- 软删除的对话不会在列表中显示

### 分页说明

- 对话列表按最后消息时间倒序排列（最新的在前）
- 消息列表按创建时间正序排列（最早的在前）
- 分页参数验证：页数和每页大小最小为1

### 性能考虑

- 对话列表查询已优化，包含最后一条消息预览
- 对话详情支持分页消息，避免大量消息时的性能问题
- 数据库查询使用了适当的索引优化

## 接口测试

### 使用Postman测试

1. 首先通过 `/api/auth/login` 获取JWT token
2. 在后续请求的Header中添加: `Authorization: Bearer <token>`
3. 按照上述接口文档进行测试

### Swagger文档

项目启动后可以访问 Swagger UI 进行在线测试：
- 地址：`http://localhost:8080/doc.html`
- 在"对话管理"标签下可以看到所有相关接口
