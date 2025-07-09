package su.sue.aiproject.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * OpenAI兼容的聊天完成请求
 */
@Data
@Schema(description = "聊天完成请求")
public class ChatCompletionRequest {
    
    @Schema(description = "模型ID", example = "1")
    private Integer id;
    
    @Schema(description = "会话ID（必填）", example = "123", required = true)
    private Long conversationId;
    
    @Schema(description = "消息列表")
    private List<ChatMessage> messages;
    
    @Schema(description = "是否流式响应", example = "true")
    private Boolean stream = false;
    
    @Schema(description = "温度参数，控制随机性", example = "0.7")
    private Double temperature;
    
    @Schema(description = "最大token数量", example = "2000")
    private Integer maxTokens;
    
    @Schema(description = "top_p参数", example = "1.0")
    private Double topP;
    
    @Schema(description = "频率惩罚", example = "0.0")
    private Double frequencyPenalty;
    
    @Schema(description = "存在惩罚", example = "0.0")
    private Double presencePenalty;
    
    @Schema(description = "停止词")
    private List<String> stop;
    
    @Schema(description = "用户唯一标识")
    private String user;
    
    @Schema(description = "额外参数")
    private Map<String, Object> additionalProperties;
}
