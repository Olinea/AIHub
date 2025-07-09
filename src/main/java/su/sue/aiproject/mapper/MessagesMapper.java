package su.sue.aiproject.mapper;

import su.sue.aiproject.domain.Messages;
import su.sue.aiproject.domain.dto.MessageDetailResponse;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author Akaio
* @description 针对表【messages】的数据库操作Mapper
* @createDate 2025-07-07 14:39:51
* @Entity su.sue.aiproject.domain.Messages
*/
public interface MessagesMapper extends BaseMapper<Messages> {

    /**
     * 获取对话的消息列表
     */
    @Select("""
        SELECT 
            m.id,
            m.role,
            m.content,
            m.name,
            m.model_id as modelId,
            am.model_name as modelName,
            COALESCE(m.tokens_consumed, 0) as tokensConsumed,
            m.prompt_tokens as promptTokens,
            m.completion_tokens as completionTokens,
            m.total_tokens as totalTokens,
            m.finish_reason as finishReason,
            m.tool_calls as toolCalls,
            m.tool_call_id as toolCallId,
            m.system_fingerprint as systemFingerprint,
            m.created_at as createdAt
        FROM messages m
        LEFT JOIN ai_models am ON m.model_id = am.id
        WHERE m.conversation_id = #{conversationId}
        ORDER BY m.created_at ASC
        """)
    List<MessageDetailResponse> findMessagesByConversationId(@Param("conversationId") Long conversationId);

    /**
     * 分页获取对话的消息列表
     */
    @Select("""
        SELECT 
            m.id,
            m.role,
            m.content,
            m.name,
            m.model_id as modelId,
            am.model_name as modelName,
            COALESCE(m.tokens_consumed, 0) as tokensConsumed,
            m.prompt_tokens as promptTokens,
            m.completion_tokens as completionTokens,
            m.total_tokens as totalTokens,
            m.finish_reason as finishReason,
            m.tool_calls as toolCalls,
            m.tool_call_id as toolCallId,
            m.system_fingerprint as systemFingerprint,
            m.created_at as createdAt
        FROM messages m
        LEFT JOIN ai_models am ON m.model_id = am.id
        WHERE m.conversation_id = #{conversationId}
        ORDER BY m.created_at ASC
        """)
    Page<MessageDetailResponse> findMessagesByConversationIdWithPage(Page<MessageDetailResponse> page, @Param("conversationId") Long conversationId);

}




