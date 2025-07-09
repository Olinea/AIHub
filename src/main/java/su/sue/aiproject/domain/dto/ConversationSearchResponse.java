package su.sue.aiproject.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 对话搜索结果响应DTO
 */
@Data
@Schema(description = "对话搜索结果响应")
public class ConversationSearchResponse {
    
    @Schema(description = "会话ID", example = "1")
    private Long conversationId;
    
    @Schema(description = "会话标题", example = "关于AI的讨论")
    private String conversationTitle;
    
    @Schema(description = "匹配的消息ID", example = "5")
    private Long messageId;
    
    @Schema(description = "消息角色", example = "user")
    private String messageRole;
    
    @Schema(description = "消息内容", example = "一句话介绍你自己")
    private String messageContent;
    
    @Schema(description = "消息创建时间")
    private LocalDateTime messageCreatedAt;
    
    @Schema(description = "模型ID", example = "1")
    private Integer modelId;
    
    @Schema(description = "模型名称", example = "deepseek-chat")
    private String modelName;
    
    @Schema(description = "匹配得分", example = "0.95")
    private Double matchScore;
}
