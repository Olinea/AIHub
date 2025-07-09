package su.sue.aiproject.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 生成对话标题响应
 */
@Data
@Schema(description = "生成对话标题响应")
public class GenerateTitleResponse {
    
    @Schema(description = "生成的标题", example = "关于Spring Boot开发的讨论")
    private String title;
    
    @Schema(description = "会话ID", example = "1")
    private Long conversationId;
    
    @Schema(description = "生成时间戳", example = "1625123456789")
    private Long timestamp;
    
    public GenerateTitleResponse() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public GenerateTitleResponse(String title, Long conversationId) {
        this.title = title;
        this.conversationId = conversationId;
        this.timestamp = System.currentTimeMillis();
    }
}
