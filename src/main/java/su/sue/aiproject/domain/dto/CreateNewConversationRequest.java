package su.sue.aiproject.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Size;

/**
 * 创建新会话请求DTO
 */
@Data
@Schema(description = "创建新会话请求")
public class CreateNewConversationRequest {
    
    @Schema(description = "会话标题（可选，默认为'新对话'）", example = "关于AI的讨论")
    @Size(max = 255, message = "标题长度不能超过255个字符")
    private String title;
}
