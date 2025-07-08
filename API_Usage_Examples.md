# API连接测试使用示例

## 前提条件

1. 应用程序已启动并运行在 `http://localhost:8080`
2. 拥有管理员权限的用户账号
3. 已配置的AI模型记录

## 示例场景

假设我们有以下AI模型配置需要测试：

### 1. OpenAI GPT-4 模型测试

```bash
# 首先获取JWT令牌（需要管理员账号）
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "your-password"
  }'

# 响应示例
{
  "success": true,
  "message": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer"
  }
}

# 使用JWT令牌测试AI模型连接
curl -X POST http://localhost:8080/api/admin/ai-models/1/test-connection \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..." \
  -H "Content-Type: application/json"

# 成功响应示例
{
  "success": true,
  "message": "API连接测试完成",
  "data": "🔍 测试模型信息:\n模型名称: gpt-4\n提供商: OpenAI\nAPI端点: https://api.openai.com/v1/chat/completions\nAPI密钥: sk-proj-***AbC123\n组织ID: org-example123\n项目ID: proj_example456\n\n📊 测试结果:\n✅ OpenAI兼容API连接测试成功\n响应模型: gpt-4\n响应内容: Hello! This appears to be a connection test. How can I assist you today?"
}

# 失败响应示例
{
  "success": false,
  "message": "API连接测试完成",
  "data": "❌ OpenAI兼容API连接测试失败\n状态码: 401 UNAUTHORIZED\n错误信息: Unauthorized\nAPI错误: Invalid authentication credentials\n错误类型: invalid_request_error"
}
```

### 2. Anthropic Claude 模型测试

```bash
curl -X POST http://localhost:8080/api/admin/ai-models/2/test-connection \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json"

# 响应示例
{
  "success": true,
  "message": "API连接测试完成",
  "data": "🔍 测试模型信息:\n模型名称: claude-3-opus-20240229\n提供商: Anthropic\nAPI端点: https://api.anthropic.com/v1/messages\nAPI密钥: sk-ant-***XyZ789\n\n📊 测试结果:\n✅ Anthropic API连接测试成功\n响应模型: claude-3-opus-20240229\n响应内容: Hello! I can confirm that this connection test was successful."
}
```

### 3. Google Gemini 模型测试

```bash
curl -X POST http://localhost:8080/api/admin/ai-models/3/test-connection \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json"

# 响应示例
{
  "success": true,
  "message": "API连接测试完成",
  "data": "🔍 测试模型信息:\n模型名称: gemini-pro\n提供商: Google\nAPI端点: https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent\nAPI密钥: AIzaSy***Def456\n\n📊 测试结果:\n✅ Google API连接测试成功\n响应内容: Hello! This is a connection test response from Gemini."
}
```

### 4. 查看模型完整配置

```bash
curl -X GET http://localhost:8080/api/admin/ai-models/1/show-config \
  -H "Authorization: Bearer your-jwt-token"

# 响应示例
{
  "success": true,
  "message": "获取模型配置成功",
  "data": {
    "id": 1,
    "modelName": "gpt-4",
    "provider": "OpenAI",
    "apiEndpoint": "https://api.openai.com/v1/chat/completions",
    "apiKey": "sk-proj-abcdefghijk...",
    "apiSecret": null,
    "organizationId": "org-example123",
    "projectId": "proj_example456",
    "extraHeaders": null,
    "costPer1kTokens": 0.03,
    "rateLimitPerMinute": 60,
    "isEnabled": true,
    "createdAt": "2025-07-08T02:15:30",
    "updatedAt": "2025-07-08T02:15:30"
  }
}
```

## 错误处理示例

### 模型不存在
```bash
curl -X POST http://localhost:8080/api/admin/ai-models/999/test-connection \
  -H "Authorization: Bearer your-jwt-token"

# 响应
HTTP 404 Not Found
```

### API密钥未配置
```bash
# 响应示例
{
  "success": false,
  "message": "API密钥未配置"
}
```

### API端点未配置
```bash
# 响应示例
{
  "success": false,
  "message": "API端点未配置"
}
```

### 权限不足
```bash
curl -X POST http://localhost:8080/api/admin/ai-models/1/test-connection
# (不带Authorization头)

# 响应
HTTP 401 Unauthorized
{
  "error": "Full authentication is required to access this resource"
}
```

## 集成测试脚本

```bash
#!/bin/bash

# 设置变量
BASE_URL="http://localhost:8080"
USERNAME="admin"
PASSWORD="your-password"

# 1. 登录获取令牌
echo "正在登录..."
LOGIN_RESPONSE=$(curl -s -X POST $BASE_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\"}")

TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.data.accessToken')

if [ "$TOKEN" = "null" ]; then
  echo "登录失败"
  exit 1
fi

echo "登录成功，获取到令牌"

# 2. 测试模型连接
MODELS=(1 2 3)  # 模型ID列表

for MODEL_ID in "${MODELS[@]}"; do
  echo "正在测试模型 $MODEL_ID 的连接..."
  
  RESPONSE=$(curl -s -X POST $BASE_URL/api/admin/ai-models/$MODEL_ID/test-connection \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json")
  
  SUCCESS=$(echo $RESPONSE | jq -r '.success')
  MESSAGE=$(echo $RESPONSE | jq -r '.data')
  
  if [ "$SUCCESS" = "true" ]; then
    echo "✅ 模型 $MODEL_ID 测试成功"
  else
    echo "❌ 模型 $MODEL_ID 测试失败"
  fi
  
  echo "详细信息:"
  echo "$MESSAGE"
  echo "---"
done

echo "所有测试完成"
```

## 前端集成示例

```javascript
// JavaScript/TypeScript示例
class AiModelTestClient {
  constructor(baseUrl, authToken) {
    this.baseUrl = baseUrl;
    this.authToken = authToken;
  }

  async testModelConnection(modelId) {
    try {
      const response = await fetch(`${this.baseUrl}/api/admin/ai-models/${modelId}/test-connection`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${this.authToken}`,
          'Content-Type': 'application/json'
        }
      });

      const result = await response.json();
      
      if (result.success) {
        console.log('✅ 连接测试成功');
        console.log(result.data);
        return { success: true, data: result.data };
      } else {
        console.log('❌ 连接测试失败');
        console.log(result.message);
        return { success: false, error: result.message };
      }
    } catch (error) {
      console.error('请求失败:', error);
      return { success: false, error: error.message };
    }
  }

  async getModelConfig(modelId) {
    try {
      const response = await fetch(`${this.baseUrl}/api/admin/ai-models/${modelId}/show-config`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${this.authToken}`,
          'Content-Type': 'application/json'
        }
      });

      const result = await response.json();
      return result.success ? result.data : null;
    } catch (error) {
      console.error('获取配置失败:', error);
      return null;
    }
  }
}

// 使用示例
const client = new AiModelTestClient('http://localhost:8080', 'your-jwt-token');

// 测试模型连接
client.testModelConnection(1).then(result => {
  if (result.success) {
    document.getElementById('test-result').innerText = result.data;
  } else {
    document.getElementById('test-result').innerText = 'Error: ' + result.error;
  }
});
```
