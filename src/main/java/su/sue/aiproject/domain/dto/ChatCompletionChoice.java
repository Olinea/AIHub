package su.sue.aiproject.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 流式响应中的选择项
 */
@Data
@Schema(description = "选择项")
public class ChatCompletionChoice {
    
    @Schema(description = "选择项索引", example = "0")
    private Integer index;
    
    @Schema(description = "消息内容（非流式）")
    private ChatMessage message;
    
    @Schema(description = "增量内容（流式）")
    private ChatMessage delta;
    
    @Schema(description = "结束原因", example = "stop")
    private String finishReason;
}
