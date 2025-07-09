# AI模型管理接口文档

## 概述

本项目新增了AI模型管理功能，包括管理员专用的模型增删改查接口和普通用户的模型查看接口。API密钥以明文形式存储，请确保数据库安全。

## 接口说明

### 管理员接口（需要管理员权限）

基础路径：`/api/admin/ai-models`

#### 1. 添加AI模型
- **接口**: `POST /api/admin/ai-models`
- **权限**: 仅管理员
- **请求体**:
```json
{
  "modelName": "gpt-4",
  "provider": "OpenAI",
  "apiEndpoint": "https://api.openai.com/v1/chat/completions",
  "apiKey": "sk-your-openai-api-key-here",
  "apiSecret": "",
  "organizationId": "org-xxxxxxxxxxxxxxxxx",
  "projectId": "proj_xxxxxxxxxxxxxxxxx",
  "extraHeaders": "{\"Custom-Header\": \"value\"}",
  "costPer1kTokens": 0.03,
  "rateLimitPerMinute": 60,
  "isEnabled": false
}
```

#### 2. 更新AI模型
- **接口**: `PUT /api/admin/ai-models/{id}`
- **权限**: 仅管理员
- **说明**: API密钥字段为可选，不填表示不更新该字段
- **请求体**: 同添加接口

#### 3. 删除AI模型
- **接口**: `DELETE /api/admin/ai-models/{id}`
- **权限**: 仅管理员

#### 4. 获取AI模型列表（分页）
- **接口**: `GET /api/admin/ai-models`
- **权限**: 仅管理员
- **参数**:
  - `current`: 页码（默认1）
  - `size`: 每页大小（默认10）
  - `modelName`: 模型名称（可选，模糊查询）
  - `provider`: 提供商（可选，模糊查询）
  - `isEnabled`: 是否启用（可选）

#### 5. 获取AI模型详情
- **接口**: `GET /api/admin/ai-models/{id}`
- **权限**: 仅管理员

#### 6. 切换模型状态
- **接口**: `PATCH /api/admin/ai-models/{id}/status`
- **权限**: 仅管理员
- **说明**: 切换模型的启用/禁用状态

#### 7. 获取所有AI模型（不分页）
- **接口**: `GET /api/admin/ai-models/all`
- **权限**: 仅管理员

#### 8. 测试API连接
- **接口**: `POST /api/admin/ai-models/{id}/test-connection`
- **权限**: 仅管理员
- **说明**: 测试指定模型的API连接是否正常

#### 9. 显示模型配置
- **接口**: `GET /api/admin/ai-models/{id}/show-config`
- **权限**: 仅管理员
- **说明**: 显示模型的完整配置信息（包含API密钥）

### 普通用户接口

基础路径：`/api/ai-models`

#### 1. 获取启用的AI模型列表
- **接口**: `GET /api/ai-models/enabled`
- **权限**: 登录用户
- **说明**: 获取所有启用状态的AI模型，供用户在聊天时选择
- **安全说明**: 只返回安全字段（id, modelName, provider, costPer1kTokens），不包含API密钥等敏感信息

**响应示例**:
```json
{
  "code": 200,
  "message": "获取启用的AI模型列表成功",
  "data": [
    {
      "id": 1,
      "modelName": "deepseek-chat",
      "provider": "DeepSeek",
      "costPer1kTokens": 0.001
    },
    {
      "id": 2,
      "modelName": "gpt-4",
      "provider": "OpenAI", 
      "costPer1kTokens": 0.03
    }
  ]
}
```

## 权限控制

### 管理员角色设置

系统通过用户表中的 `is_admin` 字段来判断用户是否为管理员：
- `is_admin = 1`: 管理员用户
- `is_admin = 0`: 普通用户

### 权限验证

1. **Spring Security配置**: `/api/admin/**` 路径需要 `ROLE_ADMIN` 角色
2. **方法级安全**: 使用 `@PreAuthorize("hasRole('ADMIN')")` 注解确保双重保护
3. **JWT令牌**: 用户登录后，JWT中包含用户角色信息

## 数据库表结构

### ai_models表

| 字段 | 类型 | 约束 | 描述 |
|------|------|------|------|
| id | INT | 主键, 自增 | 模型唯一标识 |
| model_name | VARCHAR(100) | 非空 | 模型名称 |
| provider | VARCHAR(50) | 非空 | 提供商 |
| api_endpoint | VARCHAR(255) | 非空 | API端点 |
| api_key | VARCHAR(500) | 可空 | API密钥 |
| api_secret | VARCHAR(500) | 可空 | API密钥Secret |
| organization_id | VARCHAR(100) | 可空 | 组织ID |
| project_id | VARCHAR(100) | 可空 | 项目ID |
| extra_headers | JSON | 可空 | 额外请求头 |
| cost_per_1k_tokens | DECIMAL(10, 6) | 非空 | 每1000 token成本 |
| rate_limit_per_minute | INT | 默认60 | 每分钟请求限制 |
| is_enabled | BOOLEAN | 默认false | 是否启用 |
| created_at | DATETIME | 非空 | 创建时间 |
| updated_at | DATETIME | 非空 | 更新时间 |

## 初始化数据

项目提供了两个SQL脚本：
1. `alter_ai_models_add_api_auth.sql` - 为现有表添加新字段
2. `init_ai_models.sql` - 插入示例数据

包含：
- 常见AI模型示例数据（OpenAI、Anthropic、Google、DeepSeek）
- 测试管理员账号（用户名：admin，密码：admin123）

## 使用示例

### 1. 管理员登录
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "admin123"
  }'
```

### 2. 添加新模型（需要管理员token）
```bash
curl -X POST http://localhost:8080/api/admin/ai-models \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -d '{
    "modelName": "gpt-4-turbo-2024",
    "provider": "OpenAI",
    "apiEndpoint": "https://api.openai.com/v1/chat/completions",
    "apiKey": "sk-your-actual-openai-api-key",
    "organizationId": "org-your-organization-id",
    "costPer1kTokens": 0.01,
    "rateLimitPerMinute": 60,
    "isEnabled": false
  }'
```

### 3. 测试API连接
```bash
curl -X POST http://localhost:8080/api/admin/ai-models/1/test-connection \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

### 4. 普通用户获取可用模型
```bash
curl -X GET http://localhost:8080/api/ai-models/enabled \
  -H "Authorization: Bearer YOUR_USER_TOKEN"
```

## 安全注意事项

1. **API密钥安全**: 
   - API密钥以明文存储在数据库中
   - 请确保数据库访问安全
   - 建议定期轮换API密钥

2. **访问控制**:
   - 管理员接口有严格的权限控制
   - 普通用户无法查看API密钥信息
   - **普通用户接口安全性**: `/api/ai-models/enabled` 接口已优化，只返回安全字段（id, modelName, provider, costPer1kTokens），不暴露API密钥、组织ID等敏感信息

3. **数据暴露控制**:
   - 普通用户接口使用专门的`AiModelSafeResponse` DTO 类
   - 敏感字段（apiKey、apiSecret、organizationId、projectId、extraHeaders等）对普通用户隐藏
   - 管理员接口仍可获取完整模型信息用于管理

4. **生产环境建议**:
   - 使用更强的数据库访问控制
   - 考虑API密钥的环境变量存储
   - 启用数据库加密

## 注意事项

1. **验证**:
   - 所有请求都有完整的参数验证
   - 防止重复的模型名称和提供商组合

2. **错误处理**:
   - 统一的错误响应格式
   - 详细的错误信息提示

3. **文档**:
   - 集成Swagger UI，可访问 `/doc.html` 查看完整API文档
