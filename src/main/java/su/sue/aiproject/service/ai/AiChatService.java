package su.sue.aiproject.service.ai;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import su.sue.aiproject.domain.dto.ChatCompletionRequest;
import su.sue.aiproject.domain.dto.ChatCompletionResponse;

/**
 * AI聊天服务接口
 */
public interface AiChatService {
    
    /**
     * 同步聊天接口
     * 
     * @param request 聊天请求
     * @param userId 用户ID
     * @return 聊天响应
     */
    ChatCompletionResponse chat(ChatCompletionRequest request, Long userId);
    
    /**
     * 流式聊天接口
     * 
     * @param request 聊天请求
     * @param userId 用户ID
     * @return SSE发射器
     */
    SseEmitter chatStream(ChatCompletionRequest request, Long userId);
    
    /**
     * 检查是否支持指定的模型
     * 
     * @param modelName 模型名称
     * @return 是否支持
     */
    boolean supportsModel(String modelName);
}
