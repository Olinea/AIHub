package su.sue.aiproject.mapper;

import su.sue.aiproject.domain.Conversations;
import su.sue.aiproject.domain.dto.ConversationListResponse;
import su.sue.aiproject.domain.dto.ConversationDetailResponse;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author Akaio
* @description 针对表【conversations】的数据库操作Mapper
* @createDate 2025-07-07 14:39:48
* @Entity su.sue.aiproject.domain.Conversations
*/
public interface ConversationsMapper extends BaseMapper<Conversations> {

    /**
     * 更新会话的模型ID
     */
    @Update("UPDATE conversations SET model_id = #{modelId} WHERE id = #{conversationId}")
    int updateModelId(@Param("conversationId") Long conversationId, @Param("modelId") Integer modelId);

    /**
     * 获取用户的对话列表，包含最后一条消息信息
     */
    @Select("""
        SELECT 
            c.id,
            c.title,
            COALESCE(c.status, 'active') as status,
            c.model_id as modelId,
            am.model_name as modelName,
            COALESCE(msg_stats.messageCount, 0) as messageCount,
            COALESCE(msg_stats.totalTokens, 0) as totalTokens,
            lm.content as lastMessageContent,
            lm.created_at as lastMessageTime,
            c.created_at as createdAt,
            c.updated_at as updatedAt
        FROM conversations c
        LEFT JOIN ai_models am ON c.model_id = am.id
        LEFT JOIN (
            SELECT 
                m1.conversation_id,
                m1.content,
                m1.created_at
            FROM messages m1
            INNER JOIN (
                SELECT conversation_id, MAX(created_at) as max_created_at
                FROM messages
                GROUP BY conversation_id
            ) m2 ON m1.conversation_id = m2.conversation_id AND m1.created_at = m2.max_created_at
        ) lm ON c.id = lm.conversation_id
        LEFT JOIN (
            SELECT 
                conversation_id,
                COUNT(*) as messageCount,
                COALESCE(SUM(COALESCE(total_tokens, tokens_consumed, 0)), 0) as totalTokens
            FROM messages
            GROUP BY conversation_id
        ) msg_stats ON c.id = msg_stats.conversation_id
        WHERE c.user_id = #{userId}
            AND COALESCE(c.status, 'active') != 'deleted'
        ORDER BY COALESCE(lm.created_at, c.created_at) DESC
        """)
    List<ConversationListResponse> findConversationsByUserId(@Param("userId") Long userId);

    /**
     * 分页获取用户的对话列表
     */
    @Select("""
        SELECT 
            c.id,
            c.title,
            COALESCE(c.status, 'active') as status,
            c.model_id as modelId,
            am.model_name as modelName,
            COALESCE(msg_stats.messageCount, 0) as messageCount,
            COALESCE(msg_stats.totalTokens, 0) as totalTokens,
            lm.content as lastMessageContent,
            lm.created_at as lastMessageTime,
            c.created_at as createdAt,
            c.updated_at as updatedAt
        FROM conversations c
        LEFT JOIN ai_models am ON c.model_id = am.id
        LEFT JOIN (
            SELECT 
                m1.conversation_id,
                m1.content,
                m1.created_at
            FROM messages m1
            INNER JOIN (
                SELECT conversation_id, MAX(created_at) as max_created_at
                FROM messages
                GROUP BY conversation_id
            ) m2 ON m1.conversation_id = m2.conversation_id AND m1.created_at = m2.max_created_at
        ) lm ON c.id = lm.conversation_id
        LEFT JOIN (
            SELECT 
                conversation_id,
                COUNT(*) as messageCount,
                COALESCE(SUM(COALESCE(total_tokens, tokens_consumed, 0)), 0) as totalTokens
            FROM messages
            GROUP BY conversation_id
        ) msg_stats ON c.id = msg_stats.conversation_id
        WHERE c.user_id = #{userId}
            AND COALESCE(c.status, 'active') != 'deleted'
        ORDER BY COALESCE(lm.created_at, c.created_at) DESC
        """)
    Page<ConversationListResponse> findConversationsByUserIdWithPage(Page<ConversationListResponse> page, @Param("userId") Long userId);

    /**
     * 获取对话详情（不包含消息）
     */
    @Select("""
        SELECT 
            c.id,
            c.title,
            COALESCE(c.status, 'active') as status,
            c.model_id as modelId,
            am.model_name as modelName,
            COALESCE(msg_stats.messageCount, 0) as messageCount,
            COALESCE(msg_stats.totalTokens, 0) as totalTokens,
            c.created_at as createdAt,
            c.updated_at as updatedAt
        FROM conversations c
        LEFT JOIN ai_models am ON c.model_id = am.id
        LEFT JOIN (
            SELECT 
                conversation_id,
                COUNT(*) as messageCount,
                COALESCE(SUM(COALESCE(total_tokens, tokens_consumed, 0)), 0) as totalTokens
            FROM messages
            GROUP BY conversation_id
        ) msg_stats ON c.id = msg_stats.conversation_id
        WHERE c.id = #{conversationId} 
            AND c.user_id = #{userId}
            AND COALESCE(c.status, 'active') != 'deleted'
        """)
    ConversationDetailResponse findConversationDetailById(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

}




