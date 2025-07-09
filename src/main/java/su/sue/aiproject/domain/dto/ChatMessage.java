package su.sue.aiproject.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 聊天消息
 */
@Data
@Schema(description = "聊天消息")
public class ChatMessage {
    
    @Schema(description = "角色：system, user, assistant", example = "user")
    private String role;
    
    @Schema(description = "消息内容", example = "你好，请介绍一下你自己")
    private String content;
    
    @Schema(description = "消息名称（可选）")
    private String name;
}
