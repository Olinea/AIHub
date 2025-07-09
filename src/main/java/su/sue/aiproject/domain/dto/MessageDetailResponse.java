package su.sue.aiproject.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息详情响应DTO
 */
@Data
@Schema(description = "消息详情响应")
public class MessageDetailResponse {
    
    @Schema(description = "消息ID", example = "1")
    private Long id;
    
    @Schema(description = "角色", example = "user", allowableValues = {"system", "user", "assistant", "tool", "function"})
    private String role;
    
    @Schema(description = "消息内容", example = "你好，请介绍一下你自己")
    private String content;
    
    @Schema(description = "消息发送者名称", example = "张三")
    private String name;
    
    @Schema(description = "AI模型ID", example = "1")
    private Integer modelId;
    
    @Schema(description = "模型名称", example = "deepseek-chat")
    private String modelName;
    
    @Schema(description = "消耗的token数", example = "50")
    private Integer tokensConsumed;
    
    @Schema(description = "提示词token数", example = "20")
    private Integer promptTokens;
    
    @Schema(description = "完成token数", example = "30")
    private Integer completionTokens;
    
    @Schema(description = "总token数", example = "50")
    private Integer totalTokens;
    
    @Schema(description = "完成原因", example = "stop", allowableValues = {"stop", "length", "tool_calls", "content_filter"})
    private String finishReason;
    
    @Schema(description = "工具调用信息（JSON）")
    private String toolCalls;
    
    @Schema(description = "工具调用ID", example = "call_abc123")
    private String toolCallId;
    
    @Schema(description = "系统指纹", example = "fp_44709d6fcb")
    private String systemFingerprint;
    
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
