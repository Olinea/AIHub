package su.sue.aiproject.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 流式响应块
 */
@Data
@Schema(description = "流式响应块")
public class ChatCompletionChunk {
    
    @Schema(description = "响应ID", example = "chatcmpl-123")
    private String id;
    
    @Schema(description = "对象类型", example = "chat.completion.chunk")
    private String object;
    
    @Schema(description = "创建时间戳", example = "1677652288")
    private Long created;
    
    @Schema(description = "模型名称", example = "deepseek-chat")
    private String model;
    
    @Schema(description = "系统指纹")
    private String systemFingerprint;
    
    @Schema(description = "选择项列表")
    private List<ChatCompletionChoice> choices;
    
    @Schema(description = "Token使用统计（最后一个chunk）")
    private ChatCompletionUsage usage;
}
