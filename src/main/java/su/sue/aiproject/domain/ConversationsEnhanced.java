package su.sue.aiproject.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 增强的会话实体类 - 支持OpenAI扩展功能
 * @TableName conversations
 */
@TableName(value = "conversations")
@Data
@Schema(description = "会话信息（增强版）")
public class ConversationsEnhanced {
    
    /**
     * 会话唯一标识
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "会话ID", example = "1")
    private Long id;

    /**
     * 所属用户ID
     */
    @TableField("user_id")
    @Schema(description = "用户ID", example = "1")
    private Long userId;

    /**
     * 会话标题
     */
    @Schema(description = "会话标题", example = "关于AI的讨论")
    private String title;

    /**
     * 会话默认使用的模型ID
     */
    @TableField("model_id")
    @Schema(description = "默认模型ID", example = "1")
    private Integer modelId;

    /**
     * 会话状态
     */
    @Schema(description = "会话状态", example = "active", allowableValues = {"active", "archived", "deleted"})
    private String status;

    /**
     * 消息数量
     */
    @TableField("message_count")
    @Schema(description = "消息数量", example = "10")
    private Integer messageCount;

    /**
     * 总token消耗
     */
    @TableField("total_tokens")
    @Schema(description = "总token消耗", example = "500")
    private Integer totalTokens;

    /**
     * 创建时间
     */
    @TableField("created_at")
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 最后更新时间
     */
    @TableField("updated_at")
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
