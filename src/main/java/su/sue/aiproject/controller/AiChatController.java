package su.sue.aiproject.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import su.sue.aiproject.domain.dto.ChatCompletionRequest;
import su.sue.aiproject.domain.dto.ChatCompletionResponse;
import su.sue.aiproject.domain.ApiResponse;
import su.sue.aiproject.service.ai.AiChatManagerService;
import su.sue.aiproject.security.UserPrincipal;

/**
 * AI聊天统一接口控制器
 */
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Tag(name = "AI聊天", description = "AI聊天统一接口（兼容OpenAI格式）")
@Slf4j
public class AiChatController {
    
    private final AiChatManagerService aiChatManagerService;
    
    @PostMapping("/completions")
    @Operation(summary = "聊天完成接口", description = "兼容OpenAI格式的聊天完成接口，支持同步和流式响应")
    public ResponseEntity<?> chatCompletions(
            @RequestBody ChatCompletionRequest request,
            Authentication authentication) {
        
        try {
            // 获取用户ID
            Long userId = getUserId(authentication);
            
            // 验证请求
            validateRequest(request);
            
            // 检查模型是否支持
            if (!aiChatManagerService.isModelSupported(request.getModel())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("不支持的模型: " + request.getModel()));
            }
            
            // 根据是否流式响应选择不同的处理方式
            if (Boolean.TRUE.equals(request.getStream())) {
                // 流式响应
                SseEmitter emitter = aiChatManagerService.chatStream(request, userId);
                return ResponseEntity.ok()
                        .contentType(MediaType.TEXT_EVENT_STREAM)
                        .body(emitter);
            } else {
                // 同步响应
                ChatCompletionResponse response = aiChatManagerService.chat(request, userId);
                return ResponseEntity.ok(response);
            }
            
        } catch (Exception e) {
            log.error("聊天请求处理失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("处理失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/models")
    @Operation(summary = "获取支持的模型列表", description = "获取当前支持的AI模型列表")
    public ResponseEntity<ApiResponse<Object>> getSupportedModels() {
        // TODO: 实现获取支持的模型列表
        return ResponseEntity.ok(ApiResponse.success("暂未实现", null));
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
    
    /**
     * 验证请求参数
     */
    private void validateRequest(ChatCompletionRequest request) {
        if (request.getModel() == null || request.getModel().trim().isEmpty()) {
            throw new IllegalArgumentException("模型名称不能为空");
        }
        
        if (request.getMessages() == null || request.getMessages().isEmpty()) {
            throw new IllegalArgumentException("消息列表不能为空");
        }
        
        // 验证消息格式
        request.getMessages().forEach(message -> {
            if (message.getRole() == null || message.getRole().trim().isEmpty()) {
                throw new IllegalArgumentException("消息角色不能为空");
            }
            if (message.getContent() == null || message.getContent().trim().isEmpty()) {
                throw new IllegalArgumentException("消息内容不能为空");
            }
        });
    }
}
