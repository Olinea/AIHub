-- 为ai_models表添加API认证相关字段的SQL脚本

-- 添加API认证相关字段
ALTER TABLE ai_models 
ADD COLUMN api_key VARCHAR(500) COMMENT 'API密钥',
ADD COLUMN api_secret VARCHAR(500) COMMENT 'API密钥Secret（某些厂商需要）',
ADD COLUMN organization_id VARCHAR(100) COMMENT '组织ID（OpenAI等需要）',
ADD COLUMN project_id VARCHAR(100) COMMENT '项目ID（某些厂商需要）',
ADD COLUMN extra_headers JSON COMMENT '额外的请求头（JSON格式存储）',
ADD COLUMN rate_limit_per_minute INT DEFAULT 60 COMMENT '每分钟请求限制',
ADD COLUMN created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
ADD COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- 为字段添加索引
CREATE INDEX idx_ai_models_provider ON ai_models(provider);
CREATE INDEX idx_ai_models_enabled ON ai_models(is_enabled);
