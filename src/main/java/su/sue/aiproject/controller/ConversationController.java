package su.sue.aiproject.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import su.sue.aiproject.domain.ApiResponse;
import su.sue.aiproject.domain.Conversations;
import su.sue.aiproject.domain.dto.ConversationDetailResponse;
import su.sue.aiproject.domain.dto.ConversationListResponse;
import su.sue.aiproject.domain.dto.UpdateConversationTitleRequest;
import su.sue.aiproject.domain.dto.CreateNewConversationRequest;
import su.sue.aiproject.security.UserPrincipal;
import su.sue.aiproject.service.ConversationManagementService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.Date;
import java.util.List;

/**
 * 对话管理控制器
 */
@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
@Tag(name = "对话管理", description = "用户对话管理相关接口")
@Slf4j
@Validated
public class ConversationController {
    
    private final ConversationManagementService conversationManagementService;
    
    @GetMapping
    @Operation(summary = "获取对话列表", description = "获取当前用户的对话列表")
    public ResponseEntity<ApiResponse<List<ConversationListResponse>>> getConversations(
            Authentication authentication) {
        
        try {
            Long userId = getUserId(authentication);
            List<ConversationListResponse> conversations = conversationManagementService.getUserConversations(userId);
            return ResponseEntity.ok(ApiResponse.success("获取对话列表成功", conversations));
        } catch (Exception e) {
            log.error("获取对话列表失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取对话列表失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/page")
    @Operation(summary = "分页获取对话列表", description = "分页获取当前用户的对话列表")
    public ResponseEntity<ApiResponse<Page<ConversationListResponse>>> getConversationsWithPage(
            @Parameter(description = "当前页数", example = "1")
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页数最小为1") Long current,
            @Parameter(description = "每页大小", example = "20")
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "每页大小最小为1") Long size,
            Authentication authentication) {
        
        try {
            Long userId = getUserId(authentication);
            Page<ConversationListResponse> conversations = conversationManagementService.getUserConversationsWithPage(userId, current, size);
            return ResponseEntity.ok(ApiResponse.success("获取对话列表成功", conversations));
        } catch (Exception e) {
            log.error("分页获取对话列表失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取对话列表失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{conversationId}")
    @Operation(summary = "获取对话详情", description = "获取指定对话的详细信息和消息列表")
    public ResponseEntity<ApiResponse<ConversationDetailResponse>> getConversationDetail(
            @Parameter(description = "对话ID", example = "1")
            @PathVariable Long conversationId,
            Authentication authentication) {
        
        try {
            Long userId = getUserId(authentication);
            ConversationDetailResponse detail = conversationManagementService.getConversationDetail(conversationId, userId);
            return ResponseEntity.ok(ApiResponse.success("获取对话详情成功", detail));
        } catch (Exception e) {
            log.error("获取对话详情失败, conversationId: {}", conversationId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取对话详情失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{conversationId}/messages")
    @Operation(summary = "获取对话详情（分页消息）", description = "获取指定对话的详细信息和分页消息列表")
    public ResponseEntity<ApiResponse<ConversationDetailResponse>> getConversationDetailWithPagedMessages(
            @Parameter(description = "对话ID", example = "1")
            @PathVariable Long conversationId,
            @Parameter(description = "消息当前页数", example = "1")
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页数最小为1") Long messageCurrent,
            @Parameter(description = "消息每页大小", example = "50")
            @RequestParam(defaultValue = "50") @Min(value = 1, message = "每页大小最小为1") Long messageSize,
            Authentication authentication) {
        
        try {
            Long userId = getUserId(authentication);
            ConversationDetailResponse detail = conversationManagementService.getConversationDetailWithPagedMessages(
                    conversationId, userId, messageCurrent, messageSize);
            return ResponseEntity.ok(ApiResponse.success("获取对话详情成功", detail));
        } catch (Exception e) {
            log.error("获取对话详情（分页消息）失败, conversationId: {}", conversationId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取对话详情失败: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{conversationId}/title")
    @Operation(summary = "更新对话标题", description = "更新指定对话的标题")
    public ResponseEntity<ApiResponse<String>> updateConversationTitle(
            @Parameter(description = "对话ID", example = "1")
            @PathVariable Long conversationId,
            @Valid @RequestBody UpdateConversationTitleRequest request,
            Authentication authentication) {
        
        try {
            Long userId = getUserId(authentication);
            boolean success = conversationManagementService.updateConversationTitle(conversationId, userId, request.getTitle());
            
            if (success) {
                return ResponseEntity.ok(ApiResponse.success("更新对话标题成功", null));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("对话不存在或无权限访问"));
            }
        } catch (Exception e) {
            log.error("更新对话标题失败, conversationId: {}", conversationId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("更新对话标题失败: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{conversationId}/archive")
    @Operation(summary = "归档对话", description = "将指定对话设置为归档状态")
    public ResponseEntity<ApiResponse<String>> archiveConversation(
            @Parameter(description = "对话ID", example = "1")
            @PathVariable Long conversationId,
            Authentication authentication) {
        
        try {
            Long userId = getUserId(authentication);
            boolean success = conversationManagementService.archiveConversation(conversationId, userId);
            
            if (success) {
                return ResponseEntity.ok(ApiResponse.success("归档对话成功", null));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("对话不存在或无权限访问"));
            }
        } catch (Exception e) {
            log.error("归档对话失败, conversationId: {}", conversationId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("归档对话失败: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{conversationId}")
    @Operation(summary = "删除对话", description = "删除指定对话（软删除）")
    public ResponseEntity<ApiResponse<String>> deleteConversation(
            @Parameter(description = "对话ID", example = "1")
            @PathVariable Long conversationId,
            Authentication authentication) {
        
        try {
            Long userId = getUserId(authentication);
            boolean success = conversationManagementService.deleteConversation(conversationId, userId);
            
            if (success) {
                return ResponseEntity.ok(ApiResponse.success("删除对话成功", null));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("对话不存在或无权限访问"));
            }
        } catch (Exception e) {
            log.error("删除对话失败, conversationId: {}", conversationId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("删除对话失败: " + e.getMessage()));
        }
    }
    
    @PostMapping("/new")
    @Operation(summary = "创建新会话", description = "创建一个新的空会话并返回会话ID")
    public ResponseEntity<ApiResponse<Long>> createNewConversation(
            @RequestBody @Valid CreateNewConversationRequest request,
            Authentication authentication) {
        
        try {
            Long userId = getUserId(authentication);
            
            // 创建新会话
            Conversations conversation = new Conversations();
            conversation.setUserId(userId);
            conversation.setTitle(request.getTitle() != null ? request.getTitle() : "新对话");
            conversation.setCreatedAt(new Date());
            
            // 保存会话
            conversationManagementService.createNewConversation(conversation);
            
            log.info("创建新会话成功: conversationId={}, userId={}, title={}", 
                    conversation.getId(), userId, conversation.getTitle());
            
            return ResponseEntity.ok(ApiResponse.success("创建新会话成功", conversation.getId()));
            
        } catch (Exception e) {
            log.error("创建新会话失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("创建新会话失败: " + e.getMessage()));
        }
    }

    /**
     * 从认证信息中获取用户ID
     */
    private Long getUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            throw new RuntimeException("用户未认证");
        }
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userPrincipal.getId();
    }
}
