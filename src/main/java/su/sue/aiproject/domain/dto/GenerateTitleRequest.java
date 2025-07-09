package su.sue.aiproject.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 生成对话标题请求
 */
@Data
@Schema(description = "生成对话标题请求")
public class GenerateTitleRequest {
    
    @Schema(description = "会话ID", required = true, example = "1")
    private Long conversationId;
    
    @Schema(description = "对话消息列表（用于生成标题）", required = true)
    private List<ChatMessage> messages;
    
    @Schema(description = "生成标题的提示词（可选）", example = "请为这段对话生成一个简洁的标题")
    private String prompt;
}
