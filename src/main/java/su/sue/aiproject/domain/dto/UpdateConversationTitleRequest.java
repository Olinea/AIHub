package su.sue.aiproject.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 更新对话标题请求DTO
 */
@Data
@Schema(description = "更新对话标题请求")
public class UpdateConversationTitleRequest {
    
    @NotBlank(message = "标题不能为空")
    @Size(min = 1, max = 255, message = "标题长度必须在1-255个字符之间")
    @Schema(description = "新的对话标题", example = "关于人工智能的深度讨论", required = true)
    private String title;
}
