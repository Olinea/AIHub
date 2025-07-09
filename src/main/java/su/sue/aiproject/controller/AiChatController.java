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
import su.sue.aiproject.domain.dto.GenerateTitleRequest;
import su.sue.aiproject.domain.dto.GenerateTitleResponse;
import su.sue.aiproject.domain.ApiResponse;
import su.sue.aiproject.service.ai.AiChatManagerService;
import su.sue.aiproject.service.ai.QwenTitleGeneratorService;
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
    private final QwenTitleGeneratorService qwenTitleGeneratorService;
    
    @PostMapping(value = "/completions", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_EVENT_STREAM_VALUE})
    @Operation(summary = "聊天完成接口", description = "兼容OpenAI格式的聊天完成接口，支持同步和流式响应")
    public Object chatCompletions(
            @RequestBody ChatCompletionRequest request,
            Authentication authentication) {
        
        try {
            // 获取用户ID
            Long userId = getUserId(authentication);
            
            // 验证请求
            validateRequest(request);
            
            // 检查模型是否支持
            if (!aiChatManagerService.isModelSupported(request.getId())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("不支持的模型ID: " + request.getId()));
            }
            
            // 根据是否流式响应选择不同的处理方式
            if (Boolean.TRUE.equals(request.getStream())) {
                // 流式响应 - 直接返回SseEmitter，不包装在ResponseEntity中
                SseEmitter emitter = aiChatManagerService.chatStream(request, userId);
                
                // 设置错误处理
                emitter.onError(error -> {
                    log.error("SSE流发生错误: {}", error.getMessage(), error);
                });
                
                emitter.onTimeout(() -> {
                    log.warn("SSE流超时");
                    emitter.complete();
                });
                
                emitter.onCompletion(() -> {
                    log.debug("SSE流正常完成");
                });
                
                return emitter;
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
    
    @PostMapping(value = "/generate-title", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "生成对话标题", description = "根据消息生成对话标题")
    public ResponseEntity<ApiResponse<GenerateTitleResponse>> generateTitle(
            @RequestBody GenerateTitleRequest request,
            Authentication authentication) {
        
        try {
            // 获取用户ID
            Long userId = getUserId(authentication);
            
            // 验证请求
            validateTitleRequest(request);
            
            // 生成标题
            GenerateTitleResponse response = qwenTitleGeneratorService.generateTitle(request, userId);
            
            return ResponseEntity.ok(ApiResponse.success("标题生成成功", response));
        } catch (Exception e) {
            log.error("标题生成请求处理失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("处理失败: " + e.getMessage()));
        }
    }
    
    @PostMapping(value = "/generate-summary-title", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "生成对话总汇标题", description = "基于整个对话历史生成总汇标题，不计费")
    public ResponseEntity<ApiResponse<GenerateTitleResponse>> generateSummaryTitle(
            @RequestBody GenerateTitleRequest request,
            Authentication authentication) {
        
        try {
            // 获取用户ID
            Long userId = getUserId(authentication);
            
            // 验证请求
            validateTitleRequest(request);
            
            // 生成总汇标题（使用专门的提示词）
            GenerateTitleResponse response = qwenTitleGeneratorService.generateSummaryTitle(request, userId);
            
            return ResponseEntity.ok(ApiResponse.success("对话总汇标题生成成功", response));
        } catch (Exception e) {
            log.error("对话总汇标题生成请求处理失败", e);
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
        if (request.getId() == null) {
            throw new IllegalArgumentException("模型ID不能为空");
        }
        
        if (request.getConversationId() == null) {
            throw new IllegalArgumentException("会话ID不能为空");
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
    
    /**
     * 验证生成标题请求参数
     */
    private void validateTitleRequest(GenerateTitleRequest request) {
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
