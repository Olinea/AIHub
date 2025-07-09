package su.sue.aiproject.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import su.sue.aiproject.domain.Conversations;
import su.sue.aiproject.domain.dto.ConversationDetailResponse;
import su.sue.aiproject.domain.dto.ConversationListResponse;
import su.sue.aiproject.domain.dto.MessageDetailResponse;
import su.sue.aiproject.mapper.ConversationsMapper;
import su.sue.aiproject.mapper.MessagesMapper;
import su.sue.aiproject.service.ConversationManagementService;

import java.util.List;

/**
 * 对话管理服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationManagementServiceImpl implements ConversationManagementService {

    private final ConversationsMapper conversationsMapper;
    private final MessagesMapper messagesMapper;

    @Override
    public List<ConversationListResponse> getUserConversations(Long userId) {
        log.info("获取用户对话列表, userId: {}", userId);
        try {
            List<ConversationListResponse> conversations = conversationsMapper.findConversationsByUserId(userId);
            log.info("获取用户对话列表成功, userId: {}, 总数: {}", userId, conversations.size());
            return conversations;
        } catch (Exception e) {
            log.error("获取用户对话列表失败, userId: {}", userId, e);
            throw new RuntimeException("获取对话列表失败", e);
        }
    }

    @Override
    public Page<ConversationListResponse> getUserConversationsWithPage(Long userId, long current, long size) {
        log.info("分页获取用户对话列表, userId: {}, current: {}, size: {}", userId, current, size);
        try {
            Page<ConversationListResponse> page = new Page<>(current, size);
            Page<ConversationListResponse> result = conversationsMapper.findConversationsByUserIdWithPage(page, userId);
            log.info("分页获取用户对话列表成功, userId: {}, current: {}, size: {}, total: {}", 
                    userId, current, size, result.getTotal());
            return result;
        } catch (Exception e) {
            log.error("分页获取用户对话列表失败, userId: {}, current: {}, size: {}", userId, current, size, e);
            throw new RuntimeException("获取对话列表失败", e);
        }
    }

    @Override
    public ConversationDetailResponse getConversationDetail(Long conversationId, Long userId) {
        log.info("获取对话详情, conversationId: {}, userId: {}", conversationId, userId);
        try {
            // 获取对话基本信息
            ConversationDetailResponse detail = conversationsMapper.findConversationDetailById(conversationId, userId);
            if (detail == null) {
                log.warn("对话不存在或无权限访问, conversationId: {}, userId: {}", conversationId, userId);
                throw new RuntimeException("对话不存在或无权限访问");
            }

            // 获取消息列表
            List<MessageDetailResponse> messages = messagesMapper.findMessagesByConversationId(conversationId);
            detail.setMessages(messages);

            log.info("获取对话详情成功, conversationId: {}, userId: {}, 消息数: {}", 
                    conversationId, userId, messages.size());
            return detail;
        } catch (Exception e) {
            log.error("获取对话详情失败, conversationId: {}, userId: {}", conversationId, userId, e);
            throw new RuntimeException("获取对话详情失败", e);
        }
    }

    @Override
    public ConversationDetailResponse getConversationDetailWithPagedMessages(Long conversationId, Long userId, 
                                                                             long messageCurrent, long messageSize) {
        log.info("获取对话详情（分页消息）, conversationId: {}, userId: {}, messageCurrent: {}, messageSize: {}", 
                conversationId, userId, messageCurrent, messageSize);
        try {
            // 获取对话基本信息
            ConversationDetailResponse detail = conversationsMapper.findConversationDetailById(conversationId, userId);
            if (detail == null) {
                log.warn("对话不存在或无权限访问, conversationId: {}, userId: {}", conversationId, userId);
                throw new RuntimeException("对话不存在或无权限访问");
            }

            // 分页获取消息列表
            Page<MessageDetailResponse> messagePage = new Page<>(messageCurrent, messageSize);
            Page<MessageDetailResponse> messageResult = messagesMapper.findMessagesByConversationIdWithPage(messagePage, conversationId);
            detail.setMessages(messageResult.getRecords());

            log.info("获取对话详情（分页消息）成功, conversationId: {}, userId: {}, 消息数: {}, 总消息数: {}", 
                    conversationId, userId, messageResult.getRecords().size(), messageResult.getTotal());
            return detail;
        } catch (Exception e) {
            log.error("获取对话详情（分页消息）失败, conversationId: {}, userId: {}, messageCurrent: {}, messageSize: {}", 
                    conversationId, userId, messageCurrent, messageSize, e);
            throw new RuntimeException("获取对话详情失败", e);
        }
    }

    @Override
    public boolean deleteConversation(Long conversationId, Long userId) {
        log.info("删除对话, conversationId: {}, userId: {}", conversationId, userId);
        try {
            UpdateWrapper<Conversations> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", conversationId)
                        .eq("user_id", userId)
                        .set("status", "deleted");
            
            int updated = conversationsMapper.update(null, updateWrapper);
            boolean success = updated > 0;
            
            if (success) {
                log.info("删除对话成功, conversationId: {}, userId: {}", conversationId, userId);
            } else {
                log.warn("删除对话失败，对话不存在或无权限, conversationId: {}, userId: {}", conversationId, userId);
            }
            
            return success;
        } catch (Exception e) {
            log.error("删除对话失败, conversationId: {}, userId: {}", conversationId, userId, e);
            throw new RuntimeException("删除对话失败", e);
        }
    }

    @Override
    public boolean archiveConversation(Long conversationId, Long userId) {
        log.info("归档对话, conversationId: {}, userId: {}", conversationId, userId);
        try {
            UpdateWrapper<Conversations> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", conversationId)
                        .eq("user_id", userId)
                        .set("status", "archived");
            
            int updated = conversationsMapper.update(null, updateWrapper);
            boolean success = updated > 0;
            
            if (success) {
                log.info("归档对话成功, conversationId: {}, userId: {}", conversationId, userId);
            } else {
                log.warn("归档对话失败，对话不存在或无权限, conversationId: {}, userId: {}", conversationId, userId);
            }
            
            return success;
        } catch (Exception e) {
            log.error("归档对话失败, conversationId: {}, userId: {}", conversationId, userId, e);
            throw new RuntimeException("归档对话失败", e);
        }
    }

    @Override
    public boolean updateConversationTitle(Long conversationId, Long userId, String newTitle) {
        log.info("更新对话标题, conversationId: {}, userId: {}, newTitle: {}", conversationId, userId, newTitle);
        try {
            if (newTitle == null || newTitle.trim().isEmpty()) {
                throw new IllegalArgumentException("标题不能为空");
            }
            
            UpdateWrapper<Conversations> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", conversationId)
                        .eq("user_id", userId)
                        .set("title", newTitle.trim());
            
            int updated = conversationsMapper.update(null, updateWrapper);
            boolean success = updated > 0;
            
            if (success) {
                log.info("更新对话标题成功, conversationId: {}, userId: {}, newTitle: {}", conversationId, userId, newTitle);
            } else {
                log.warn("更新对话标题失败，对话不存在或无权限, conversationId: {}, userId: {}", conversationId, userId);
            }
            
            return success;
        } catch (Exception e) {
            log.error("更新对话标题失败, conversationId: {}, userId: {}, newTitle: {}", conversationId, userId, newTitle, e);
            throw new RuntimeException("更新对话标题失败", e);
        }
    }

    @Override
    public Conversations createNewConversation(Conversations conversation) {
        log.info("创建新会话, userId: {}, title: {}", conversation.getUserId(), conversation.getTitle());
        try {
            // 保存会话到数据库
            conversationsMapper.insert(conversation);
            log.info("创建新会话成功, conversationId: {}, userId: {}, title: {}", 
                    conversation.getId(), conversation.getUserId(), conversation.getTitle());
            return conversation;
        } catch (Exception e) {
            log.error("创建新会话失败, userId: {}, title: {}", conversation.getUserId(), conversation.getTitle(), e);
            throw new RuntimeException("创建新会话失败", e);
        }
    }
}
