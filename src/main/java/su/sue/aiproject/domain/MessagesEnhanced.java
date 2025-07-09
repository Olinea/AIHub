package su.sue.aiproject.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 增强的消息实体类 - 支持OpenAI完整格式
 * @TableName messages
 */
@TableName(value = "messages")
@Data
@Schema(description = "消息信息（OpenAI兼容）")
public class MessagesEnhanced {
    
    /**
     * 消息唯一标识
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "消息ID", example = "1")
    private Long id;

    /**
     * 所属会话ID
     */
    @TableField("conversation_id")
    @Schema(description = "会话ID", example = "1")
    private Long conversationId;

    /**
     * 角色：system, user, assistant, tool, function
     */
    @Schema(description = "角色", example = "user", allowableValues = {"system", "user", "assistant", "tool", "function"})
    private String role;

    /**
     * 消息内容
     */
    @Schema(description = "消息内容", example = "你好，请介绍一下你自己")
    private String content;

    /**
     * 消息发送者名称（OpenAI可选字段）
     */
    @Schema(description = "消息发送者名称", example = "张三")
    private String name;

    /**
     * 使用的AI模型ID
     */
    @TableField("model_id")
    @Schema(description = "AI模型ID", example = "1")
    private Integer modelId;

    /**
     * 本次交互消耗的token数（兼容旧字段）
     */
    @TableField("tokens_consumed")
    @Schema(description = "消耗的token数", example = "50")
    private Integer tokensConsumed;

    /**
     * 提示词token数
     */
    @TableField("prompt_tokens")
    @Schema(description = "提示词token数", example = "20")
    private Integer promptTokens;

    /**
     * 完成响应token数
     */
    @TableField("completion_tokens")
    @Schema(description = "完成token数", example = "30")
    private Integer completionTokens;

    /**
     * 总token数
     */
    @TableField("total_tokens")
    @Schema(description = "总token数", example = "50")
    private Integer totalTokens;

    /**
     * 完成原因：stop, length, tool_calls, content_filter等
     */
    @TableField("finish_reason")
    @Schema(description = "完成原因", example = "stop", allowableValues = {"stop", "length", "tool_calls", "content_filter"})
    private String finishReason;

    /**
     * 工具调用信息（JSON格式）
     */
    @TableField("tool_calls")
    @Schema(description = "工具调用信息（JSON）")
    private String toolCalls;

    /**
     * 工具调用响应ID
     */
    @TableField("tool_call_id")
    @Schema(description = "工具调用ID", example = "call_abc123")
    private String toolCallId;

    /**
     * 系统指纹
     */
    @TableField("system_fingerprint")
    @Schema(description = "系统指纹", example = "fp_44709d6fcb")
    private String systemFingerprint;

    /**
     * 创建时间
     */
    @TableField("created_at")
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
