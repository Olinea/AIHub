package su.sue.aiproject.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * AI模型安全响应DTO - 仅包含用户可见的安全字段
 */
@Data
@Schema(description = "AI模型信息（安全版本）")
public class AiModelSafeResponse {
    
    /**
     * 模型唯一标识
     */
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
     * 每1000 token的成本
     */
    @Schema(description = "每1000 token的成本", example = "0.03")
    private BigDecimal costPer1kTokens;

    /**
     * 构造函数 - 从AiModels实体转换
     */
    public AiModelSafeResponse(AiModels aiModel) {
        this.id = aiModel.getId();
        this.modelName = aiModel.getModelName();
        this.provider = aiModel.getProvider();
        this.costPer1kTokens = aiModel.getCostPer1kTokens();
    }

    /**
     * 默认构造函数
     */
    public AiModelSafeResponse() {
    }
}
