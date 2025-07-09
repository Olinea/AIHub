package su.sue.aiproject.service;

import su.sue.aiproject.domain.Conversations;
import su.sue.aiproject.domain.dto.ConversationListResponse;
import su.sue.aiproject.domain.dto.ConversationDetailResponse;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 对话管理服务接口
 */
public interface ConversationManagementService {

    /**
     * 获取用户的对话列表
     *
     * @param userId 用户ID
     * @return 对话列表
     */
    List<ConversationListResponse> getUserConversations(Long userId);

    /**
     * 分页获取用户的对话列表
     *
     * @param userId   用户ID
     * @param current  当前页数
     * @param size     每页大小
     * @return 分页对话列表
     */
    Page<ConversationListResponse> getUserConversationsWithPage(Long userId, long current, long size);

    /**
     * 获取对话详情
     *
     * @param conversationId 对话ID
     * @param userId         用户ID
     * @return 对话详情
     */
    ConversationDetailResponse getConversationDetail(Long conversationId, Long userId);

    /**
     * 获取对话详情（分页消息）
     *
     * @param conversationId 对话ID
     * @param userId         用户ID
     * @param messageCurrent 消息当前页
     * @param messageSize    消息每页大小
     * @return 对话详情（包含分页消息）
     */
    ConversationDetailResponse getConversationDetailWithPagedMessages(Long conversationId, Long userId, long messageCurrent, long messageSize);

    /**
     * 删除对话（软删除）
     *
     * @param conversationId 对话ID
     * @param userId         用户ID
     * @return 是否删除成功
     */
    boolean deleteConversation(Long conversationId, Long userId);

    /**
     * 归档对话
     *
     * @param conversationId 对话ID
     * @param userId         用户ID
     * @return 是否归档成功
     */
    boolean archiveConversation(Long conversationId, Long userId);

    /**
     * 更新对话标题
     *
     * @param conversationId 对话ID
     * @param userId         用户ID
     * @param newTitle       新标题
     * @return 是否更新成功
     */
    boolean updateConversationTitle(Long conversationId, Long userId, String newTitle);

    /**
     * 创建新会话
     *
     * @param conversation 会话对象
     * @return 创建的会话对象（包含ID）
     */
    Conversations createNewConversation(Conversations conversation);
}
