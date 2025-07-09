package su.sue.aiproject.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会话设置实体类 - 支持个性化AI参数
 * @TableName conversation_settings
 */
@TableName(value = "conversation_settings")
@Data
@Schema(description = "会话设置")
public class ConversationSettings {
    
    /**
     * 设置ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "设置ID", example = "1")
    private Long id;

    /**
     * 关联的会话ID
     */
    @TableField("conversation_id")
    @Schema(description = "会话ID", example = "1")
    private Long conversationId;

    /**
     * 温度参数(0.0-2.0)
     */
    @Schema(description = "温度参数", example = "0.7", minimum = "0.0", maximum = "2.0")
    private BigDecimal temperature;

    /**
     * 最大token数量
     */
    @TableField("max_tokens")
    @Schema(description = "最大token数", example = "2000")
    private Integer maxTokens;

    /**
     * top_p参数(0.0-1.0)
     */
    @TableField("top_p")
    @Schema(description = "top_p参数", example = "1.0", minimum = "0.0", maximum = "1.0")
    private BigDecimal topP;

    /**
     * 频率惩罚(-2.0到2.0)
     */
    @TableField("frequency_penalty")
    @Schema(description = "频率惩罚", example = "0.0", minimum = "-2.0", maximum = "2.0")
    private BigDecimal frequencyPenalty;

    /**
     * 存在惩罚(-2.0到2.0)
     */
    @TableField("presence_penalty")
    @Schema(description = "存在惩罚", example = "0.0", minimum = "-2.0", maximum = "2.0")
    private BigDecimal presencePenalty;

    /**
     * 停止序列（JSON格式）
     */
    @TableField("stop_sequences")
    @Schema(description = "停止序列", example = "[\"\\n\", \"END\"]")
    private String stopSequences;

    /**
     * 系统提示词
     */
    @TableField("system_message")
    @Schema(description = "系统提示词", example = "You are a helpful assistant.")
    private String systemMessage;

    /**
     * 是否启用工具调用
     */
    @TableField("tools_enabled")
    @Schema(description = "启用工具调用", example = "false")
    private Boolean toolsEnabled;

    /**
     * 可用工具列表（JSON格式）
     */
    @TableField("available_tools")
    @Schema(description = "可用工具列表")
    private String availableTools;

    /**
     * 创建时间
     */
    @TableField("created_at")
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
