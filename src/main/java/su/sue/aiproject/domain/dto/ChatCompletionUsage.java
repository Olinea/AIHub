package su.sue.aiproject.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Token使用统计
 */
@Data
@Schema(description = "Token使用统计")
public class ChatCompletionUsage {
    
    @Schema(description = "提示词Token数", example = "20")
    private Integer promptTokens;
    
    @Schema(description = "完成Token数", example = "50")
    private Integer completionTokens;
    
    @Schema(description = "总Token数", example = "70")
    private Integer totalTokens;
    
    @Schema(description = "提示词Token详情")
    private TokenDetails promptTokensDetails;
    
    @Schema(description = "提示词缓存命中Token数", example = "0")
    private Integer promptCacheHitTokens;
    
    @Schema(description = "提示词缓存未命中Token数", example = "20")
    private Integer promptCacheMissTokens;
    
    @Data
    @Schema(description = "Token详情")
    public static class TokenDetails {
        @Schema(description = "缓存Token数", example = "0")
        private Integer cachedTokens;
    }
}
