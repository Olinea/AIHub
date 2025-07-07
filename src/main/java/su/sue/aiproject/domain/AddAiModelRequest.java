package su.sue.aiproject.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;

@Data
@Schema(description = "添加AI模型请求")
public class AddAiModelRequest {

    @NotBlank(message = "模型名称不能为空")
    @Schema(description = "模型名称", example = "gpt-4")
    private String modelName;

    @NotBlank(message = "提供商不能为空")
    @Schema(description = "提供商", example = "OpenAI")
    private String provider;

    @NotBlank(message = "API端点不能为空")
    @Schema(description = "API端点", example = "https://api.openai.com/v1/chat/completions")
    private String apiEndpoint;

    @NotBlank(message = "API密钥不能为空")
    @Schema(description = "API密钥", example = "sk-xxxxxxxxxxxxxxxxxxxxxxxxx")
    private String apiKey;

    @Schema(description = "API密钥Secret（某些厂商需要）", example = "")
    private String apiSecret;

    @Schema(description = "组织ID（OpenAI等需要）", example = "org-xxxxxxxxxxxxxxxxx")
    private String organizationId;

    @Schema(description = "项目ID（某些厂商需要）", example = "proj_xxxxxxxxxxxxxxxxx")
    private String projectId;

    @Schema(description = "额外的请求头（JSON格式）", example = "{\"Custom-Header\": \"value\"}")
    private String extraHeaders;

    @NotNull(message = "每1K token成本不能为空")
    @PositiveOrZero(message = "每1K token成本不能为负数")
    @Schema(description = "每1000 token的成本", example = "0.03")
    private BigDecimal costPer1kTokens;

    @Min(value = 1, message = "每分钟请求限制至少为1")
    @Schema(description = "每分钟请求限制", example = "60")
    private Integer rateLimitPerMinute = 60;

    @Schema(description = "是否启用", example = "true")
    private Boolean isEnabled = true;
}
