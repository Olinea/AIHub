package su.sue.aiproject.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * AI模型实体类
 * @TableName ai_models
 */
@TableName(value ="ai_models")
@Data
@Schema(description = "AI模型信息")
public class AiModels {
    /**
     * 模型唯一标识
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "模型ID", example = "1")
    private Integer id;

    /**
     * 模型名称
     */
    @Schema(description = "模型名称", example = "gpt-4")
    private String modelName;

    /**
     * 提供商
     */
    @Schema(description = "提供商", example = "OpenAI")
    private String provider;

    /**
     * API端点
     */
    @Schema(description = "API端点", example = "https://api.openai.com/v1/chat/completions")
    private String apiEndpoint;

    /**
     * API密钥
     */
    @Schema(description = "API密钥", example = "sk-xxxxxxxxxxxxxxxxxxxxxxxxx")
    @TableField("api_key")
    private String apiKey;

    /**
     * API密钥Secret（某些厂商需要）
     */
    @Schema(description = "API密钥Secret", example = "secret_xxxxxxxxxxxxxxxxx")
    @TableField("api_secret")
    private String apiSecret;

    /**
     * 组织ID（OpenAI等需要）
     */
    @TableField("organization_id")
    private String organizationId;

    /**
     * 项目ID（某些厂商需要）
     */
    @TableField("project_id")
    private String projectId;

    /**
     * 额外的请求头（JSON格式存储）
     */
    @TableField("extra_headers")
    private String extraHeaders;

    /**
     * 每1000 token的成本
     */
    @Schema(description = "每1000 token的成本", example = "0.03")
    private BigDecimal costPer1kTokens;

    /**
     * 每分钟请求限制
     */
    @Schema(description = "每分钟请求限制", example = "60")
    @TableField("rate_limit_per_minute")
    private Integer rateLimitPerMinute;

    /**
     * 是否启用
     */
    @Schema(description = "是否启用", example = "true")
    private Boolean isEnabled;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}