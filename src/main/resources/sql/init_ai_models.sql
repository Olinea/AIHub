-- AI模型管理初始化数据
-- 插入一些常见的AI模型示例

-- 注意：实际使用时请替换为真实的API密钥

-- OpenAI模型
INSERT INTO ai_models (model_name, provider, api_endpoint, api_key, organization_id, cost_per_1k_tokens, rate_limit_per_minute, is_enabled, created_at, updated_at) VALUES
('gpt-4', 'OpenAI', 'https://api.openai.com/v1/chat/completions', 'sk-your-openai-api-key-here', NULL, 0.03, 60, true, NOW(), NOW()),
('gpt-4-turbo', 'OpenAI', 'https://api.openai.com/v1/chat/completions', 'sk-your-openai-api-key-here', NULL, 0.01, 60, true, NOW(), NOW()),
('gpt-3.5-turbo', 'OpenAI', 'https://api.openai.com/v1/chat/completions', 'sk-your-openai-api-key-here', NULL, 0.0015, 120, true, NOW(), NOW());

-- Anthropic模型
INSERT INTO ai_models (model_name, provider, api_endpoint, api_key, cost_per_1k_tokens, rate_limit_per_minute, is_enabled, created_at, updated_at) VALUES
('claude-3-sonnet', 'Anthropic', 'https://api.anthropic.com/v1/messages', 'sk-ant-your-anthropic-api-key-here', 0.015, 60, true, NOW(), NOW()),
('claude-3-haiku', 'Anthropic', 'https://api.anthropic.com/v1/messages', 'sk-ant-your-anthropic-api-key-here', 0.00025, 100, true, NOW(), NOW()),
('claude-3-opus', 'Anthropic', 'https://api.anthropic.com/v1/messages', 'sk-ant-your-anthropic-api-key-here', 0.075, 30, true, NOW(), NOW());

-- Google模型
INSERT INTO ai_models (model_name, provider, api_endpoint, api_key, cost_per_1k_tokens, rate_limit_per_minute, is_enabled, created_at, updated_at) VALUES
('gemini-pro', 'Google', 'https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent', 'AIzaSyD-your-google-api-key-here', 0.0005, 60, true, NOW(), NOW()),
('gemini-pro-vision', 'Google', 'https://generativelanguage.googleapis.com/v1/models/gemini-pro-vision:generateContent', 'AIzaSyD-your-google-api-key-here', 0.0025, 60, true, NOW(), NOW());

-- DeepSeek模型
INSERT INTO ai_models (model_name, provider, api_endpoint, api_key, cost_per_1k_tokens, rate_limit_per_minute, is_enabled, created_at, updated_at) VALUES
('deepseek-chat', 'DeepSeek', 'https://api.deepseek.com/v1/chat/completions', 'sk-your-deepseek-api-key-here', 0.0014, 60, true, NOW(), NOW()),
('deepseek-coder', 'DeepSeek', 'https://api.deepseek.com/v1/chat/completions', 'sk-your-deepseek-api-key-here', 0.0014, 60, true, NOW(), NOW());

-- 创建一个管理员用户用于测试（密码：admin123）
-- 注意：实际使用时应该通过应用程序注册并手动设置为管理员
INSERT INTO users (username, password_hash, email, credit_balance, is_admin, created_at, updated_at) VALUES
('admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'admin@example.com', 100.00, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE email = VALUES(email);

-- 使用说明：
-- 1. 执行上述SQL后，需要将示例API密钥替换为真实密钥
-- 2. 管理员可以通过后台管理界面更新API密钥
-- 3. API密钥以明文存储，请确保数据库安全
