package su.sue.aiproject.service.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import su.sue.aiproject.domain.dto.ChatCompletionRequest;
import su.sue.aiproject.domain.dto.ChatCompletionResponse;

/**
 * AI聊天管理服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatManagerService {
    
    @Qualifier("deepseekChatService")
    private final AiChatService deepseekChatService;
    
    /**
     * 统一聊天接口 - 同步模式
     */
    public ChatCompletionResponse chat(ChatCompletionRequest request, Long userId) {
        AiChatService chatService = getServiceByModel(request.getModel());
        return chatService.chat(request, userId);
    }
    
    /**
     * 统一聊天接口 - 流式模式
     */
    public SseEmitter chatStream(ChatCompletionRequest request, Long userId) {
        AiChatService chatService = getServiceByModel(request.getModel());
        return chatService.chatStream(request, userId);
    }
    
    /**
     * 根据模型名称获取对应的服务
     */
    private AiChatService getServiceByModel(String modelName) {
        // 根据模型名称判断使用哪个服务
        if (modelName.startsWith("deepseek") || modelName.contains("deepseek")) {
            return deepseekChatService;
        }
        
        // TODO: 添加其他AI服务商的支持
        // if (modelName.startsWith("gpt") || modelName.contains("openai")) {
        //     return openaiChatService;
        // }
        // if (modelName.startsWith("claude") || modelName.contains("anthropic")) {
        //     return anthropicChatService;
        // }
        
        throw new RuntimeException("不支持的模型: " + modelName);
    }
    
    /**
     * 检查模型是否支持
     */
    public boolean isModelSupported(String modelName) {
        try {
            getServiceByModel(modelName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
