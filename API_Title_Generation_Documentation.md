# AI对话标题生成API文档

## 概述
本系统提供了两个用于生成对话标题的API接口，使用阿里云通义千问（Qwen）模型。

## API接口

### 1. 生成对话标题
**接口地址：** `POST /api/v1/chat/generate-title`

**功能描述：** 根据对话消息生成简洁的对话标题

**请求体：**
```json
{
  "conversationId": 123,
  "messages": [
    {
      "role": "user",
      "content": "你好，请帮我介绍一下Spring Boot的基本概念"
    },
    {
      "role": "assistant", 
      "content": "Spring Boot是一个基于Spring框架的开发工具..."
    }
  ],
  "prompt": "请为这段对话生成一个简洁的标题" // 可选字段
}
```

**响应体：**
```json
{
  "code": 200,
  "message": "标题生成成功",
  "data": {
    "title": "Spring Boot基本概念介绍",
    "conversationId": 123,
    "timestamp": 1625123456789
  }
}
```

### 2. 生成对话总汇标题（新增）
**接口地址：** `POST /api/v1/chat/generate-summary-title`

**功能描述：** 基于整个对话历史生成总汇标题，适用于长对话的主题概括，不计费

**请求体：**
```json
{
  "conversationId": 123,
  "messages": [
    // 完整的对话历史消息列表
    {
      "role": "user",
      "content": "你好，请帮我介绍一下Spring Boot"
    },
    {
      "role": "assistant",
      "content": "Spring Boot是一个基于Spring框架的开发工具..."
    },
    // ... 更多对话消息
  ]
}
```

**响应体：**
```json
{
  "code": 200,
  "message": "对话总汇标题生成成功",
  "data": {
    "title": "Spring Boot开发技术讨论与实践",
    "conversationId": 123,
    "timestamp": 1625123456789
  }
}
```

## 技术实现

### 配置信息
- **API服务商：** 阿里云通义千问
- **模型：** qwen-turbo
- **API端点：** https://dashscope.aliyuncs.com/compatible-mode/v1
- **API密钥：** 硬编码在代码中（生产环境建议使用配置文件）

### 特性说明

1. **普通标题生成**
   - 适用于对话开始阶段的快速标题生成
   - 只处理前10条消息以控制Token消耗
   - 生成简洁标题（不超过20个字符）

2. **对话总汇标题生成**
   - 适用于长对话的主题总结
   - 处理完整的对话历史
   - 生成更加全面的总汇标题（不超过25个字符）
   - 对长消息进行智能截断保留关键信息

### 错误处理
- 当API调用失败时，自动返回默认标题
- 普通标题生成失败返回："新对话"
- 总汇标题生成失败返回："对话总汇"

### 安全性
- 需要用户认证
- 请求参数验证
- 错误日志记录

## 使用示例

### curl 示例

```bash
# 生成普通对话标题
curl -X POST "http://localhost:8080/api/v1/chat/generate-title" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "conversationId": 123,
    "messages": [
      {
        "role": "user",
        "content": "请介绍一下Java的基本语法"
      }
    ]
  }'

# 生成对话总汇标题
curl -X POST "http://localhost:8080/api/v1/chat/generate-summary-title" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "conversationId": 123,
    "messages": [
      {
        "role": "user",
        "content": "请介绍一下Java的基本语法"
      },
      {
        "role": "assistant",
        "content": "Java是一种面向对象的编程语言..."
      }
    ]
  }'
```

## 注意事项

1. **API密钥配置**：当前API密钥是硬编码的示例，生产环境请替换为真实密钥
2. **Token限制**：为避免超出Token限制，系统会自动截断过长的消息
3. **超时设置**：API调用超时时间设置为30秒
4. **日志记录**：所有请求和错误都会记录到日志中便于调试
