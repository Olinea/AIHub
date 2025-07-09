package su.sue.aiproject.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话详情响应DTO
 */
@Data
@Schema(description = "对话详情响应")
public class ConversationDetailResponse {
    
    @Schema(description = "会话ID", example = "1")
    private Long id;
    
    @Schema(description = "会话标题", example = "关于AI的讨论")
    private String title;
    
    @Schema(description = "会话状态", example = "active", allowableValues = {"active", "archived", "deleted"})
    private String status;
    
    @Schema(description = "默认模型ID", example = "1")
    private Integer modelId;
    
    @Schema(description = "模型名称", example = "deepseek-chat")
    private String modelName;
    
    @Schema(description = "消息数量", example = "10")
    private Integer messageCount;
    
    @Schema(description = "总token消耗", example = "500")
    private Integer totalTokens;
    
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
    
    @Schema(description = "消息列表")
    private List<MessageDetailResponse> messages;
}
