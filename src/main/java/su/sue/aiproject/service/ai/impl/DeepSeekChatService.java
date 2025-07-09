package su.sue.aiproject.service.ai.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;
import su.sue.aiproject.domain.AiModels;
import su.sue.aiproject.domain.dto.*;
import su.sue.aiproject.service.AiModelsService;
import su.sue.aiproject.service.ai.AiChatService;
import su.sue.aiproject.service.CreditService;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * DeepSeek AI聊天服务实现
 */
@Service("deepseekChatService")
@RequiredArgsConstructor
@Slf4j
public class DeepSeekChatService implements AiChatService {
    
    private final WebClient webClient;
    private final AiModelsService aiModelsService;
    private final CreditService creditService;
    private final ObjectMapper objectMapper;
    
    private static final String PROVIDER_NAME = "deepseek";
    
    @Override
    public ChatCompletionResponse chat(ChatCompletionRequest request, Long userId) {
        // 获取模型信息
        AiModels model = getModelByName(request.getModel());
        
        // 检查用户积分
        checkUserCredit(userId, model);
        
        try {
            // 构建请求
            String requestBody = buildRequestBody(request, false);
            
            // 调用DeepSeek API
            String response = webClient.post()
                    .uri(model.getApiEndpoint())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + model.getApiKey())
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();
            
            // 解析响应
            ChatCompletionResponse chatResponse = parseResponse(response);
            
            // 计费
            if (chatResponse.getUsage() != null) {
                chargeCreditAndSaveRecord(userId, model, chatResponse.getUsage(), request, chatResponse);
            }
            
            return chatResponse;
            
        } catch (Exception e) {
            log.error("DeepSeek API调用失败", e);
            throw new RuntimeException("AI服务调用失败: " + e.getMessage());
        }
    }
    
    @Override
    public SseEmitter chatStream(ChatCompletionRequest request, Long userId) {
        // 获取模型信息
        AiModels model = getModelByName(request.getModel());
        
        // 检查用户积分
        checkUserCredit(userId, model);
        
        SseEmitter emitter = new SseEmitter(300000L); // 5分钟超时
        
        try {
            // 构建请求
            String requestBody = buildRequestBody(request, true);
            
            // 异步处理流式响应
            CompletableFuture.runAsync(() -> {
                try {
                    Flux<String> responseStream = webClient.post()
                            .uri(model.getApiEndpoint())
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + model.getApiKey())
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .bodyValue(requestBody)
                            .retrieve()
                            .bodyToFlux(String.class)
                            .timeout(Duration.ofSeconds(60));
                    
                    StringBuilder fullContent = new StringBuilder();
                    final AtomicReference<ChatCompletionUsage> finalUsageRef = new AtomicReference<>();
                    
                    responseStream.subscribe(
                            data -> {
                                try {
                                    // 处理SSE数据
                                    String[] lines = data.split("\n");
                                    for (String line : lines) {
                                        if (line.startsWith("data: ")) {
                                            String jsonData = line.substring(6).trim();
                                            if ("[DONE]".equals(jsonData)) {
                                                emitter.complete();
                                                return;
                                            }
                                            
                                            ChatCompletionChunk chunk = parseChunk(jsonData);
                                            if (chunk != null) {
                                                // 积累内容用于计费
                                                if (chunk.getChoices() != null && !chunk.getChoices().isEmpty()) {
                                                    ChatCompletionChoice choice = chunk.getChoices().get(0);
                                                    if (choice.getDelta() != null && choice.getDelta().getContent() != null) {
                                                        fullContent.append(choice.getDelta().getContent());
                                                    }
                                                }
                                                
                                                // 保存最终的usage信息
                                                if (chunk.getUsage() != null) {
                                                    finalUsageRef.set(chunk.getUsage());
                                                }
                                                
                                                // 发送数据到前端
                                                emitter.send(SseEmitter.event()
                                                        .data("data: " + objectMapper.writeValueAsString(chunk))
                                                        .name("message"));
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    log.error("处理流式数据失败", e);
                                    emitter.completeWithError(e);
                                }
                            },
                            error -> {
                                log.error("流式请求失败", error);
                                emitter.completeWithError(error);
                            },
                            () -> {
                                try {
                                    // 发送结束标记
                                    emitter.send(SseEmitter.event()
                                            .data("data: [DONE]")
                                            .name("message"));
                                    
                                    // 计费
                                    ChatCompletionUsage finalUsage = finalUsageRef.get();
                                    if (finalUsage != null) {
                                        ChatCompletionResponse mockResponse = new ChatCompletionResponse();
                                        mockResponse.setUsage(finalUsage);
                                        chargeCreditAndSaveRecord(userId, model, finalUsage, request, mockResponse);
                                    }
                                    
                                    emitter.complete();
                                } catch (Exception e) {
                                    log.error("完成流式响应失败", e);
                                    emitter.completeWithError(e);
                                }
                            }
                    );
                    
                } catch (Exception e) {
                    log.error("启动流式请求失败", e);
                    emitter.completeWithError(e);
                }
            });
            
        } catch (Exception e) {
            log.error("创建流式响应失败", e);
            emitter.completeWithError(e);
        }
        
        return emitter;
    }
    
    @Override
    public boolean supportsModel(String modelName) {
        try {
            AiModels model = aiModelsService.getModelByProviderAndName(PROVIDER_NAME, modelName);
            return model != null && model.getIsEnabled();
        } catch (Exception e) {
            return false;
        }
    }
    
    private AiModels getModelByName(String modelName) {
        AiModels model = aiModelsService.getModelByProviderAndName(PROVIDER_NAME, modelName);
        if (model == null || !model.getIsEnabled()) {
            throw new RuntimeException("模型 " + modelName + " 不可用");
        }
        return model;
    }
    
    private void checkUserCredit(Long userId, AiModels model) {
        BigDecimal userCredit = creditService.getUserCredit(userId);
        BigDecimal minRequired = model.getCostPer1kTokens().multiply(new BigDecimal("0.1")); // 至少需要100个token的费用
        
        if (userCredit.compareTo(minRequired) < 0) {
            throw new RuntimeException("积分余额不足，请充值");
        }
    }
    
    private String buildRequestBody(ChatCompletionRequest request, boolean stream) throws JsonProcessingException {
        // 构建DeepSeek API请求体
        ChatCompletionRequest deepSeekRequest = new ChatCompletionRequest();
        deepSeekRequest.setModel(request.getModel());
        deepSeekRequest.setMessages(request.getMessages());
        deepSeekRequest.setStream(stream);
        deepSeekRequest.setTemperature(request.getTemperature());
        deepSeekRequest.setMaxTokens(request.getMaxTokens());
        deepSeekRequest.setTopP(request.getTopP());
        deepSeekRequest.setFrequencyPenalty(request.getFrequencyPenalty());
        deepSeekRequest.setPresencePenalty(request.getPresencePenalty());
        deepSeekRequest.setStop(request.getStop());
        
        return objectMapper.writeValueAsString(deepSeekRequest);
    }
    
    private ChatCompletionResponse parseResponse(String response) throws JsonProcessingException {
        return objectMapper.readValue(response, ChatCompletionResponse.class);
    }
    
    private ChatCompletionChunk parseChunk(String jsonData) {
        try {
            return objectMapper.readValue(jsonData, ChatCompletionChunk.class);
        } catch (Exception e) {
            log.warn("解析chunk失败: {}", jsonData, e);
            return null;
        }
    }
    
    private void chargeCreditAndSaveRecord(Long userId, AiModels model, ChatCompletionUsage usage, 
                                         ChatCompletionRequest request, ChatCompletionResponse response) {
        try {
            // 计算费用
            int totalTokens = usage.getTotalTokens();
            BigDecimal cost = model.getCostPer1kTokens()
                    .multiply(new BigDecimal(totalTokens))
                    .divide(new BigDecimal(1000), 6, java.math.RoundingMode.UP);
            
            // 扣除积分
            creditService.deductCredit(userId, cost, "AI对话消费 - " + model.getModelName());
            
            // 保存对话记录
            saveConversationRecord(userId, model, request, response, usage, cost);
            
            log.info("用户 {} 使用模型 {} 消费积分 {}, 消耗token {}", 
                    userId, model.getModelName(), cost, totalTokens);
            
        } catch (Exception e) {
            log.error("计费失败", e);
            // 这里可以考虑是否要回滚或者记录失败信息
        }
    }
    
    private void saveConversationRecord(Long userId, AiModels model, ChatCompletionRequest request, 
                                      ChatCompletionResponse response, ChatCompletionUsage usage, BigDecimal cost) {
        try {
            // TODO: 实现完整的对话记录保存逻辑
            // 1. 创建或获取当前会话
            // 2. 保存用户消息
            // 3. 保存AI响应
            // 4. 记录token消耗和费用
            
            log.info("保存对话记录: userId={}, model={}, cost={}, tokens={}", 
                    userId, model.getModelName(), cost, usage.getTotalTokens());
            
            // 暂时只记录日志，后续可以扩展为保存到数据库
            // ConversationsService conversationsService;
            // MessagesService messagesService;
            // 实现具体的保存逻辑
            
        } catch (Exception e) {
            log.error("保存对话记录失败", e);
            // 不抛出异常，避免影响主流程
        }
    }
}
