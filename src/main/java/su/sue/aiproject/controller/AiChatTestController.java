package su.sue.aiproject.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import su.sue.aiproject.domain.dto.ChatCompletionRequest;
import su.sue.aiproject.domain.dto.ChatMessage;
import su.sue.aiproject.domain.ApiResponse;
import su.sue.aiproject.service.ai.AiChatManagerService;

import java.util.Collections;

/**
 * AI聊天测试控制器（开发测试用）
 */
@RestController
@RequestMapping("/api/test/chat")
@RequiredArgsConstructor
@Tag(name = "AI聊天测试", description = "AI聊天测试接口（仅用于开发测试）")
@Slf4j
public class AiChatTestController {
    
    private final AiChatManagerService aiChatManagerService;
    
    @PostMapping("/simple")
    @Operation(summary = "简单聊天测试", description = "简单的聊天测试接口，使用固定用户ID进行测试")
    public ResponseEntity<ApiResponse<Object>> simpleChat(
            @RequestParam(defaultValue = "deepseek-chat") String model,
            @RequestParam(defaultValue = "你好，请简单介绍一下你自己") String message,
            @RequestParam(defaultValue = "1") Long userId) {
        
        try {
            // 构建聊天请求
            ChatCompletionRequest request = new ChatCompletionRequest();
            request.setModel(model);
            request.setStream(false);
            
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setRole("user");
            chatMessage.setContent(message);
            
            request.setMessages(Collections.singletonList(chatMessage));
            
            // 调用聊天服务
            Object response = aiChatManagerService.chat(request, userId);
            
            return ResponseEntity.ok(ApiResponse.success("聊天成功", response));
            
        } catch (Exception e) {
            log.error("聊天测试失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("聊天失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/models")
    @Operation(summary = "检查模型支持", description = "检查指定模型是否支持")
    public ResponseEntity<ApiResponse<Boolean>> checkModel(
            @RequestParam(defaultValue = "deepseek-chat") String model) {
        
        try {
            boolean supported = aiChatManagerService.isModelSupported(model);
            return ResponseEntity.ok(ApiResponse.success("检查完成", supported));
        } catch (Exception e) {
            log.error("检查模型支持失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("检查失败: " + e.getMessage()));
        }
    }
}
