package su.sue.aiproject.service.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import su.sue.aiproject.domain.dto.ChatCompletionRequest;
import su.sue.aiproject.domain.dto.ChatCompletionResponse;
import su.sue.aiproject.domain.AiModels;
import su.sue.aiproject.service.AiModelsService;

/**
 * AI聊天管理服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatManagerService {
    
    @Qualifier("deepseekChatService")
    private final AiChatService deepseekChatService;
    
    @Qualifier("qwenChatService")
    private final AiChatService qwenChatService;
    
    @Qualifier("genericOpenAiChatService")
    private final AiChatService genericOpenAiChatService;
    
    private final AiModelsService aiModelsService;
    
    /**
     * 统一聊天接口 - 同步模式
     */
    public ChatCompletionResponse chat(ChatCompletionRequest request, Long userId) {
        try {
            log.info("收到同步聊天请求: modelId={}, userId={}, conversationId={}", 
                    request.getId(), userId, request.getConversationId());
            
            AiChatService chatService = getServiceByModelId(request.getId());
            log.debug("使用聊天服务: {}", chatService.getClass().getSimpleName());
            
            return chatService.chat(request, userId);
        } catch (Exception e) {
            log.error("同步聊天请求失败: modelId={}, userId={}, error={}", 
                    request.getId(), userId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 统一聊天接口 - 流式模式
     */
    public SseEmitter chatStream(ChatCompletionRequest request, Long userId) {
        try {
            log.info("收到流式聊天请求: modelId={}, userId={}, conversationId={}", 
                    request.getId(), userId, request.getConversationId());
            
            AiChatService chatService = getServiceByModelId(request.getId());
            log.debug("使用聊天服务: {}", chatService.getClass().getSimpleName());
            
            return chatService.chatStream(request, userId);
        } catch (Exception e) {
            log.error("流式聊天请求失败: modelId={}, userId={}, error={}", 
                    request.getId(), userId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 根据模型提供商获取对应的服务
     */
    private AiChatService getServiceByProvider(String provider) {
        if (provider == null) {
            throw new RuntimeException("模型提供商不能为空");
        }
        
        // 根据提供商标识判断使用哪个服务
        switch (provider.toLowerCase()) {
            case "deepseek":
                return deepseekChatService;
            case "qwen":
                return qwenChatService;
            case "other":
                return genericOpenAiChatService;
            // TODO: 添加其他AI服务商的支持
            // case "openai":
            //     return openaiChatService;
            // case "anthropic":
            //     return anthropicChatService;
            default:
                log.warn("未知提供商 '{}', 使用通用OpenAI兼容服务", provider);
                return genericOpenAiChatService;
        }
    }
    
    /**
     * 根据模型名称获取对应的服务（兼容旧接口）
     */
    private AiChatService getServiceByModel(String modelName) {
        // 兼容性：根据模型名称推断提供商
        if (modelName.startsWith("deepseek") || modelName.contains("deepseek")) {
            return deepseekChatService;
        }
        
        if (modelName.startsWith("qwen") || modelName.contains("qwen")) {
            return qwenChatService;
        }
        
        throw new RuntimeException("无法从模型名称推断提供商: " + modelName);
    }
    
    /**
     * 根据模型ID获取对应的服务
     */
    private AiChatService getServiceByModelId(Integer modelId) {
        if (modelId == null) {
            throw new RuntimeException("模型ID不能为空");
        }
        
        AiModels aiModel = aiModelsService.getById(modelId);
        if (aiModel == null) {
            throw new RuntimeException("模型不存在: " + modelId);
        }
        
        log.debug("获取模型服务: modelId={}, provider={}, modelName={}", 
                modelId, aiModel.getProvider(), aiModel.getModelName());
        
        // 优先使用provider字段，如果为空则降级到modelName推断
        if (aiModel.getProvider() != null && !aiModel.getProvider().trim().isEmpty()) {
            return getServiceByProvider(aiModel.getProvider());
        } else {
            log.warn("模型 {} 的provider字段为空，使用modelName推断: {}", modelId, aiModel.getModelName());
            return getServiceByModel(aiModel.getModelName());
        }
    }
    
    /**
     * 检查模型是否支持（通过模型ID）
     */
    public boolean isModelSupported(Integer modelId) {
        try {
            getServiceByModelId(modelId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 检查模型是否支持（通过模型名称，保持向后兼容）
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
