# 对话搜索API文档

## 搜索历史对话

### 接口信息
- **接口路径**: `GET /api/v1/conversations/search`
- **接口描述**: 根据关键词搜索用户的历史对话消息
- **认证要求**: 需要JWT认证

### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 | 示例 |
|--------|------|------|--------|------|------|
| keyword | String | 是 | - | 搜索关键词 | "介绍你自己" |
| current | Long | 否 | 1 | 当前页数 | 1 |
| size | Long | 否 | 20 | 每页大小 | 20 |

### 请求示例
```http
GET /api/v1/conversations/search?keyword=介绍你自己&current=1&size=20
Authorization: Bearer <JWT_TOKEN>
```

### 响应格式

```json
{
  "code": 200,
  "message": "搜索历史对话成功",
  "data": {
    "total": 2,
    "size": 20,
    "current": 1,
    "pages": 1,
    "records": [
      {
        "conversationId": 68,
        "conversationTitle": "新对话",
        "messageId": 155,
        "messageRole": "user",
        "messageContent": "一句话介绍你自己",
        "messageCreatedAt": "2025-07-09T19:50:25.24",
        "modelId": 4,
        "modelName": "deepseek-chat",
        "matchScore": 1.0
      },
      {
        "conversationId": 68,
        "conversationTitle": "新对话", 
        "messageId": 156,
        "messageRole": "assistant",
        "messageContent": "\"我是你的智能AI助手，随时为你解答问题、提供创意和高效支持！\"",
        "messageCreatedAt": "2025-07-09T19:50:25.722",
        "modelId": 4,
        "modelName": "deepseek-chat",
        "matchScore": 1.0
      }
    ]
  },
  "timestamp": 1752061892255
}
```

### 搜索结果字段说明

| 字段名 | 类型 | 描述 |
|--------|------|------|
| conversationId | Long | 所属会话ID |
| conversationTitle | String | 会话标题 |
| messageId | Long | 匹配的消息ID |
| messageRole | String | 消息角色（user/assistant） |
| messageContent | String | 消息内容 |
| messageCreatedAt | LocalDateTime | 消息创建时间 |
| modelId | Integer | AI模型ID |
| modelName | String | 模型名称 |
| matchScore | Double | 匹配得分 |

### 搜索规则

1. **搜索范围**: 搜索用户的所有历史对话消息和对话标题
2. **匹配方式**: 模糊匹配，支持部分关键词匹配
3. **权限控制**: 只能搜索当前用户的对话，不会返回其他用户的对话
4. **排序规则**: 按消息创建时间倒序排列（最新的在前）
5. **状态过滤**: 自动过滤已删除的对话

### 错误响应

#### 参数错误
```json
{
  "code": 400,
  "message": "搜索历史对话失败: 搜索关键词不能为空",
  "data": null,
  "timestamp": 1752061892255
}
```

#### 认证失败
```json
{
  "code": 401,
  "message": "用户未认证",
  "data": null,
  "timestamp": 1752061892255
}
```

### 使用示例

1. **搜索包含"介绍"的对话**:
   ```
   GET /api/v1/conversations/search?keyword=介绍
   ```

2. **搜索特定话题，分页获取**:
   ```
   GET /api/v1/conversations/search?keyword=AI&current=1&size=10
   ```

3. **搜索多个关键词（空格分隔）**:
   ```
   GET /api/v1/conversations/search?keyword=你好 智能助手
   ```

### 注意事项

1. 关键词不能为空或纯空格
2. 搜索区分大小写
3. 支持中英文关键词搜索
4. 分页参数最小值为1
5. 建议每页大小不超过100，以保证响应性能
