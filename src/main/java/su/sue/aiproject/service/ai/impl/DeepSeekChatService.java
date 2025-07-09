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
import su.sue.aiproject.domain.AiModels;
import su.sue.aiproject.domain.dto.*;
import su.sue.aiproject.service.AiModelsService;
import su.sue.aiproject.service.ai.AiChatService;
import su.sue.aiproject.service.CreditService;
import su.sue.aiproject.service.ConversationsService;
import su.sue.aiproject.service.MessagesService;
import su.sue.aiproject.domain.Conversations;
import su.sue.aiproject.domain.Messages;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Date;
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
    private final ConversationsService conversationsService;
    private final MessagesService messagesService;
    
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
            
            // 调试日志：检查 usage 信息
            log.debug("DeepSeek API 响应: {}", response);
            if (chatResponse.getUsage() == null) {
                log.warn("DeepSeek API 响应中 usage 为 null，响应内容: {}", response);
                // 创建估算的 usage 信息
                ChatCompletionUsage estimatedUsage = createEstimatedUsage(request, chatResponse);
                chatResponse.setUsage(estimatedUsage);
                log.info("使用估算的 usage 信息: {}", estimatedUsage);
            } else {
                log.info("DeepSeek API usage 信息: {}", chatResponse.getUsage());
            }
            
            // 计费和保存记录（现在总是会执行）
            chargeCreditAndSaveRecord(userId, model, chatResponse.getUsage(), request, chatResponse);
            
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
        StringBuilder fullContent = new StringBuilder();
        final AtomicReference<ChatCompletionUsage> finalUsageRef = new AtomicReference<>();
        
        try {
            // 构建请求
            String requestBody = buildRequestBody(request, true);
            
            // 异步处理流式响应
            CompletableFuture.runAsync(() -> {
                try {
                    // 修正 SSE 流式响应处理
                    webClient.post()
                            .uri(model.getApiEndpoint())
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + model.getApiKey())
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .header(HttpHeaders.ACCEPT, "text/event-stream")
                            .bodyValue(requestBody)
                            .retrieve()
                            .bodyToFlux(String.class)
                            .timeout(Duration.ofSeconds(60))
                            .doOnNext(data -> {
                                try {
                                    log.debug("收到 SSE 数据: {}", data);
                                    // 直接转发原始SSE数据，同时提取内容用于本地记录
                                    handleAndForwardSseData(data, emitter, finalUsageRef, fullContent);
                                } catch (Exception e) {
                                    log.error("处理 SSE 数据失败", e);
                                    emitter.completeWithError(e);
                                }
                            })
                            .doOnError(error -> {
                                log.error("流式请求失败", error);
                                emitter.completeWithError(error);
                            })
                            .doOnComplete(() -> {
                                try {
                                    // 不再手动发送 [DONE]，因为已经在 handleAndForwardSseData 中转发了
                                    log.debug("流式响应完成");
                                    
                                    // 计费和保存记录
                                    ChatCompletionUsage finalUsage = finalUsageRef.get();
                                    if (finalUsage == null) {
                                        // 如果没有获取到 usage，创建估算的 usage
                                        ChatCompletionResponse mockResponse = new ChatCompletionResponse();
                                        // 创建一个包含流式内容的 choice
                                        ChatCompletionChoice choice = new ChatCompletionChoice();
                                        ChatMessage message = new ChatMessage();
                                        message.setRole("assistant");
                                        message.setContent(fullContent.toString());
                                        choice.setMessage(message);
                                        mockResponse.setChoices(java.util.Arrays.asList(choice));
                                        
                                        finalUsage = createEstimatedUsage(request, mockResponse);
                                        log.info("流式响应创建估算 usage: {}", finalUsage);
                                    }
                                    
                                    ChatCompletionResponse mockResponse = new ChatCompletionResponse();
                                    mockResponse.setUsage(finalUsage);
                                    // 添加完整的助手回复用于保存到数据库
                                    ChatCompletionChoice choice = new ChatCompletionChoice();
                                    ChatMessage message = new ChatMessage();
                                    message.setRole("assistant");
                                    message.setContent(fullContent.toString());
                                    choice.setMessage(message);
                                    mockResponse.setChoices(java.util.Arrays.asList(choice));
                                    
                                    chargeCreditAndSaveRecord(userId, model, finalUsage, request, mockResponse);
                                    
                                    emitter.complete();
                                } catch (Exception e) {
                                    log.error("完成流式响应失败", e);
                                    emitter.completeWithError(e);
                                }
                            })
                            .subscribe();
                    
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
    
    /**
     * 创建估算的 usage 信息（当 API 不返回 usage 时使用）
     */
    private ChatCompletionUsage createEstimatedUsage(ChatCompletionRequest request, ChatCompletionResponse response) {
        ChatCompletionUsage usage = new ChatCompletionUsage();
        
        // 估算输入 token 数量（粗略估算：1个字符约等于0.75个token）
        int promptTokens = 0;
        if (request.getMessages() != null) {
            for (ChatMessage message : request.getMessages()) {
                if (message.getContent() != null) {
                    promptTokens += (int) (message.getContent().length() * 0.75);
                }
            }
        }
        
        // 估算输出 token 数量
        int completionTokens = 0;
        if (response.getChoices() != null && !response.getChoices().isEmpty()) {
            ChatCompletionChoice choice = response.getChoices().get(0);
            if (choice.getMessage() != null && choice.getMessage().getContent() != null) {
                completionTokens = (int) (choice.getMessage().getContent().length() * 0.75);
            }
        }
        
        usage.setPromptTokens(promptTokens);
        usage.setCompletionTokens(completionTokens);
        usage.setTotalTokens(promptTokens + completionTokens);
        
        log.info("估算 token 使用情况: prompt={}, completion={}, total={}", 
                promptTokens, completionTokens, usage.getTotalTokens());
        
        return usage;
    }
    
    /**
     * 安全的计费和保存记录，处理 usage 为 null 的情况
     */
    private void chargeCreditAndSaveRecord(Long userId, AiModels model, ChatCompletionUsage usage, 
                                         ChatCompletionRequest request, ChatCompletionResponse response) {
        try {
            // 确保 usage 不为 null
            if (usage == null) {
                log.warn("usage 信息为 null，创建估算信息");
                usage = createEstimatedUsage(request, response);
            }
            
            // 计算费用，添加安全检查
            int totalTokens = usage.getTotalTokens() != null ? usage.getTotalTokens() : 0;
            if (totalTokens <= 0) {
                // 如果 token 数量无效，使用最小费用
                totalTokens = 1;
                log.warn("token 数量无效，使用最小计费: {}", totalTokens);
            }
            
            BigDecimal cost = model.getCostPer1kTokens()
                    .multiply(new BigDecimal(totalTokens))
                    .divide(new BigDecimal(1000), 6, java.math.RoundingMode.UP);
            
            // 扣除积分
            creditService.deductCredit(userId, cost, "AI对话消费 - " + model.getModelName());
            
            // 保存对话记录（无论计费是否成功都要保存）
            saveConversationRecord(userId, model, request, response, usage, cost);
            
            log.info("用户 {} 使用模型 {} 消费积分 {}, 消耗token {}", 
                    userId, model.getModelName(), cost, totalTokens);
            
        } catch (Exception e) {
            log.error("计费失败，但仍尝试保存对话记录", e);
            // 即使计费失败，也要尝试保存对话记录（不计费）
            try {
                saveConversationRecord(userId, model, request, response, usage, BigDecimal.ZERO);
            } catch (Exception saveError) {
                log.error("保存对话记录也失败", saveError);
            }
        }
    }
    
    private void saveConversationRecord(Long userId, AiModels model, ChatCompletionRequest request, 
                                      ChatCompletionResponse response, ChatCompletionUsage usage, BigDecimal cost) {
        try {
            // 实现完整的对话记录保存逻辑
            
            // 1. 创建或获取当前会话 (简化版本：为每次对话创建新会话)
            Conversations conversation = new Conversations();
            conversation.setUserId(userId);
            // 从用户的第一条消息获取标题
            String title = getConversationTitle(request.getMessages());
            conversation.setTitle(title);
            conversation.setCreatedAt(new Date());
            
            // 保存会话
            conversationsService.save(conversation);
            Long conversationId = conversation.getId();
            
            log.info("创建新会话: conversationId={}, userId={}, title={}", conversationId, userId, title);
            
            // 2. 保存用户消息
            for (ChatMessage message : request.getMessages()) {
                if ("user".equals(message.getRole())) {
                    Messages userMessage = new Messages();
                    userMessage.setConversationId(conversationId);
                    userMessage.setRole(message.getRole());
                    userMessage.setContent(message.getContent());
                    userMessage.setModelId(model.getId().intValue());
                    userMessage.setTokensConsumed(0); // 用户消息不消耗 token
                    userMessage.setCreatedAt(new Date());
                    
                    messagesService.save(userMessage);
                    log.debug("保存用户消息: {}", userMessage.getId());
                }
            }
            
            // 3. 保存AI响应
            if (response.getChoices() != null && !response.getChoices().isEmpty()) {
                ChatCompletionChoice choice = response.getChoices().get(0);
                if (choice.getMessage() != null) {
                    Messages assistantMessage = new Messages();
                    assistantMessage.setConversationId(conversationId);
                    assistantMessage.setRole("assistant");
                    assistantMessage.setContent(choice.getMessage().getContent());
                    assistantMessage.setModelId(model.getId().intValue());
                    assistantMessage.setTokensConsumed(usage != null && usage.getTotalTokens() != null ? usage.getTotalTokens() : 0);
                    assistantMessage.setCreatedAt(new Date());
                    
                    messagesService.save(assistantMessage);
                    log.debug("保存AI回复: {}", assistantMessage.getId());
                }
            }
            
            log.info("成功保存对话记录: userId={}, conversationId={}, model={}, cost={}, tokens={}", 
                    userId, conversationId, model.getModelName(), cost, 
                    usage != null && usage.getTotalTokens() != null ? usage.getTotalTokens() : 0);
            
        } catch (Exception e) {
            log.error("保存对话记录失败", e);
            // 不抛出异常，避免影响主流程
        }
    }
    
    /**
     * 从消息列表中提取会话标题
     */
    private String getConversationTitle(java.util.List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return "新对话";
        }
        
        // 找到第一个用户消息作为标题
        for (ChatMessage message : messages) {
            if ("user".equals(message.getRole()) && message.getContent() != null) {
                String content = message.getContent().trim();
                // 限制标题长度
                if (content.length() > 50) {
                    return content.substring(0, 50) + "...";
                }
                return content;
            }
        }
        
        return "新对话";
    }
    
    /**
     * 处理并转发 SSE 流式数据
     * 既要原样转发给前端，又要本地拼接内容用于数据库保存
     */
    private void handleAndForwardSseData(String data, SseEmitter emitter, 
                                       AtomicReference<ChatCompletionUsage> finalUsageRef, 
                                       StringBuilder fullContent) throws Exception {
        // SSE 数据可能包含多行，逐行处理
        String[] lines = data.split("\\r?\\n");
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                // 空行也要转发，保持原始格式
                emitter.send(SseEmitter.event().data(""));
                continue;
            }
            
            log.debug("处理 SSE 行: {}", line);
            
            if (line.startsWith("data: ")) {
                String jsonData = line.substring(6).trim();
                
                // 原样转发整行（包含 "data: " 前缀）
                emitter.send(SseEmitter.event().data(line));
                
                if ("[DONE]".equals(jsonData)) {
                    log.debug("收到流结束标记，已转发");
                    return; // 流结束
                }
                
                // 解析 JSON chunk 用于本地处理
                ChatCompletionChunk chunk = parseChunk(jsonData);
                if (chunk != null) {
                    // 积累内容用于数据库保存
                    if (chunk.getChoices() != null && !chunk.getChoices().isEmpty()) {
                        ChatCompletionChoice choice = chunk.getChoices().get(0);
                        if (choice.getDelta() != null && choice.getDelta().getContent() != null) {
                            fullContent.append(choice.getDelta().getContent());
                            log.debug("拼接内容: {}", choice.getDelta().getContent());
                        }
                    }
                    
                    // 保存最终的usage信息
                    if (chunk.getUsage() != null) {
                        finalUsageRef.set(chunk.getUsage());
                        log.debug("收到 usage 信息: {}", chunk.getUsage());
                    }
                } else {
                    log.warn("无法解析的 chunk 数据: {}", jsonData);
                }
            } else if (line.startsWith("event:") || line.startsWith("id:") || line.startsWith("retry:")) {
                // 其他 SSE 字段也要原样转发
                emitter.send(SseEmitter.event().data(line));
                log.debug("转发 SSE 元数据: {}", line);
            } else {
                // 其他格式的数据也要转发
                emitter.send(SseEmitter.event().data(line));
                log.debug("转发其他数据: {}", line);
            }
        }
    }
}
