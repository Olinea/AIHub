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
import su.sue.aiproject.mapper.ConversationsMapper;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 通用OpenAI兼容API聊天服务实现
 * 支持任何兼容OpenAI ChatCompletion API格式的服务提供商
 */
@Service("genericOpenAiChatService")
@RequiredArgsConstructor
@Slf4j
public class GenericOpenAiChatService implements AiChatService {
    
    private final WebClient webClient;
    private final AiModelsService aiModelsService;
    private final CreditService creditService;
    private final ObjectMapper objectMapper;
    private final ConversationsService conversationsService;
    private final MessagesService messagesService;
    private final ConversationsMapper conversationsMapper;
    
    private static final String PROVIDER_NAME = "other";
    
    @Override
    public ChatCompletionResponse chat(ChatCompletionRequest request, Long userId) {
        // 获取模型信息
        AiModels model = getModelById(request.getId());
        
        // 检查用户积分
        checkUserCredit(userId, model);
        
        try {
            // 构建请求
            String requestBody = buildRequestBody(request, model.getModelName(), false);
            
            log.info("调用通用OpenAI兼容API: endpoint={}, model={}", 
                    model.getApiEndpoint(), model.getModelName());
            
            // 调用通用OpenAI兼容API
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
            log.debug("通用OpenAI API 响应: {}", response);
            if (chatResponse.getUsage() == null) {
                log.warn("通用OpenAI API 响应中 usage 为 null，响应内容: {}", response);
                // 创建估算的 usage 信息
                ChatCompletionUsage estimatedUsage = createEstimatedUsage(request, chatResponse);
                chatResponse.setUsage(estimatedUsage);
                log.info("使用估算的 usage 信息: {}", estimatedUsage);
            } else {
                log.info("通用OpenAI API usage 信息: {}", chatResponse.getUsage());
            }
            
            // 计费和保存记录（现在总是会执行）
            chargeCreditAndSaveRecord(userId, model, chatResponse.getUsage(), request, chatResponse);
            
            return chatResponse;
            
        } catch (Exception e) {
            log.error("通用OpenAI API调用失败", e);
            throw new RuntimeException("AI服务调用失败: " + e.getMessage());
        }
    }
    
    @Override
    public SseEmitter chatStream(ChatCompletionRequest request, Long userId) {
        // 获取模型信息
        AiModels model = getModelById(request.getId());
        
        // 检查用户积分
        checkUserCredit(userId, model);
        
        SseEmitter emitter = new SseEmitter(300000L); // 5分钟超时
        
        // 设置SSE响应的内容类型和编码
        emitter.onCompletion(() -> log.debug("SSE emitter 完成"));
        emitter.onTimeout(() -> log.warn("SSE emitter 超时"));
        emitter.onError(e -> log.error("SSE emitter 错误: {}", e.getMessage()));
        
        StringBuilder fullContent = new StringBuilder();
        final AtomicReference<ChatCompletionUsage> finalUsageRef = new AtomicReference<>();
        final AtomicBoolean isComplete = new AtomicBoolean(false);
        
        try {
            // 构建请求
            String requestBody = buildRequestBody(request, model.getModelName(), true);
            
            log.info("调用通用OpenAI兼容流式API: endpoint={}, model={}", 
                    model.getApiEndpoint(), model.getModelName());
            
            // 异步处理流式响应
            CompletableFuture.runAsync(() -> {
                try {
                    // 修正 SSE 流式响应处理
                    webClient.post()
                            .uri(model.getApiEndpoint())
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + model.getApiKey())
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
                            .header(HttpHeaders.ACCEPT, "text/event-stream;charset=UTF-8")
                            .header("Accept-Charset", "UTF-8")
                            .header("Cache-Control", "no-cache")
                            .bodyValue(requestBody)
                            .retrieve()
                            .bodyToFlux(String.class)
                            .timeout(Duration.ofSeconds(120)) // 增加超时时间
                            .doOnNext(data -> {
                                try {
                                    log.debug("收到 SSE 数据: {}", data);
                                    // 直接转发原始SSE数据，同时提取内容用于本地记录
                                    handleAndForwardSseData(data, emitter, finalUsageRef, fullContent);
                                } catch (Exception e) {
                                    log.error("处理 SSE 数据失败", e);
                                    // 不立即调用 completeWithError，而是记录错误并继续
                                    log.warn("SSE数据处理出错，但继续处理后续数据: {}", e.getMessage());
                                }
                            })
                            .doOnError(error -> {
                                log.error("流式请求失败", error);
                                if (!isComplete.get()) {
                                    // 尝试保存已有的内容
                                    saveStreamDataOnError(userId, model, request, fullContent.toString(), finalUsageRef.get());
                                    emitter.completeWithError(error);
                                }
                            })
                            .doOnComplete(() -> {
                                try {
                                    if (!isComplete.getAndSet(true)) {
                                        log.debug("流式响应完成，开始保存数据");
                                        
                                        // 构建完整的响应对象，包含收集到的内容
                                        ChatCompletionResponse finalResponse = new ChatCompletionResponse();
                                        ChatCompletionChoice choice = new ChatCompletionChoice();
                                        ChatMessage message = new ChatMessage();
                                        message.setRole("assistant");
                                        message.setContent(fullContent.toString());
                                        choice.setMessage(message);
                                        finalResponse.setChoices(java.util.Arrays.asList(choice));
                                        
                                        // 计费和保存记录 - 同步处理
                                        ChatCompletionUsage finalUsage = finalUsageRef.get();
                                        if (finalUsage == null) {
                                            // 如果没有获取到 usage，创建估算的 usage
                                            finalUsage = createEstimatedUsage(request, finalResponse);
                                            log.info("流式响应创建估算 usage: {}", finalUsage);
                                        }
                                        
                                        // 设置 usage 到最终响应对象
                                        finalResponse.setUsage(finalUsage);
                                        
                                        log.info("准备保存流式响应: content={}, tokens={}", 
                                                fullContent.toString(), finalUsage.getTotalTokens());
                                        
                                        chargeCreditAndSaveRecord(userId, model, finalUsage, request, finalResponse);
                                        log.info("流式响应数据保存完成");
                                        
                                        // 使用异步方式延迟关闭连接，确保客户端接收完所有数据
                                        CompletableFuture.runAsync(() -> {
                                            try {
                                                Thread.sleep(500);  // 500ms延迟，确保客户端接收完成
                                                emitter.complete();
                                                log.debug("SSE连接已正常关闭");
                                            } catch (InterruptedException e) {
                                                Thread.currentThread().interrupt();
                                                emitter.complete();
                                            } catch (Exception e) {
                                                log.error("延迟关闭SSE连接失败", e);
                                                emitter.complete();
                                            }
                                        });
                                    }
                                } catch (Exception e) {
                                    log.error("完成流式响应失败", e);
                                    if (!isComplete.get()) {
                                        emitter.completeWithError(e);
                                    }
                                }
                            })
                            .subscribe();
                    
                } catch (Exception e) {
                    log.error("启动流式请求失败", e);
                    if (!isComplete.get()) {
                        emitter.completeWithError(e);
                    }
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
    
    private AiModels getModelById(Integer modelId) {
        if (modelId == null) {
            throw new RuntimeException("模型ID不能为空");
        }
        
        AiModels model = aiModelsService.getById(modelId);
        if (model == null || !model.getIsEnabled()) {
            throw new RuntimeException("模型 ID:" + modelId + " 不可用");
        }
        
        log.info("使用通用OpenAI兼容模型: id={}, name={}, provider={}, endpoint={}", 
                model.getId(), model.getModelName(), model.getProvider(), model.getApiEndpoint());
        
        return model;
    }
    
    private void checkUserCredit(Long userId, AiModels model) {
        BigDecimal userCredit = creditService.getUserCredit(userId);
        BigDecimal minRequired = model.getCostPer1kTokens().multiply(new BigDecimal("0.1")); // 至少需要100个token的费用
        
        if (userCredit.compareTo(minRequired) < 0) {
            throw new RuntimeException("积分余额不足，请充值");
        }
    }
    
    private String buildRequestBody(ChatCompletionRequest request, String modelName, boolean stream) throws JsonProcessingException {
        // 构建标准OpenAI API请求体 - 使用Map来构建JSON
        java.util.Map<String, Object> apiRequest = new java.util.HashMap<>();
        apiRequest.put("model", modelName);
        apiRequest.put("messages", request.getMessages());
        apiRequest.put("stream", stream);
        
        if (request.getTemperature() != null) {
            apiRequest.put("temperature", request.getTemperature());
        }
        if (request.getMaxTokens() != null) {
            apiRequest.put("max_tokens", request.getMaxTokens());
        }
        if (request.getTopP() != null) {
            apiRequest.put("top_p", request.getTopP());
        }
        if (request.getFrequencyPenalty() != null) {
            apiRequest.put("frequency_penalty", request.getFrequencyPenalty());
        }
        if (request.getPresencePenalty() != null) {
            apiRequest.put("presence_penalty", request.getPresencePenalty());
        }
        if (request.getStop() != null) {
            apiRequest.put("stop", request.getStop());
        }
        
        return objectMapper.writeValueAsString(apiRequest);
    }
    
    private ChatCompletionResponse parseResponse(String response) throws JsonProcessingException {
        return objectMapper.readValue(response, ChatCompletionResponse.class);
    }
    
    private ChatCompletionChunk parseChunk(String jsonData) {
        try {
            // 首先尝试解析为标准 OpenAI 格式
            return objectMapper.readValue(jsonData, ChatCompletionChunk.class);
        } catch (Exception e) {
            // 如果解析失败，尝试解析为 Ollama 格式
            try {
                return parseOllamaChunk(jsonData);
            } catch (Exception ollamaError) {
                log.warn("解析chunk失败（尝试了 OpenAI 和 Ollama 格式）: {}", jsonData, e);
                return null;
            }
        }
    }
    
    /**
     * 解析 Ollama 格式的流式响应
     * Ollama 格式示例：{"model":"qwen3:latest","created_at":"2025-07-10T01:47:50.3865203Z","message":{"role":"assistant","content":"询问"},"done":false}
     * 注意：Ollama 流式响应中，每个 chunk 包含的是当前增量的内容，而不是累积内容
     */
    private ChatCompletionChunk parseOllamaChunk(String jsonData) throws JsonProcessingException {
        // 解析 Ollama 原始格式
        com.fasterxml.jackson.databind.JsonNode ollamaNode = objectMapper.readTree(jsonData);
        
        // 创建标准 OpenAI 格式的 chunk
        ChatCompletionChunk chunk = new ChatCompletionChunk();
        
        // 设置 ID（如果有的话，否则生成一个）
        chunk.setId("ollama-" + System.currentTimeMillis());
        
        // 设置模型名称
        if (ollamaNode.has("model")) {
            chunk.setModel(ollamaNode.get("model").asText());
        }
        
        // 设置时间戳
        chunk.setCreated(System.currentTimeMillis() / 1000);
        
        // 解析消息内容
        if (ollamaNode.has("message")) {
            com.fasterxml.jackson.databind.JsonNode messageNode = ollamaNode.get("message");
            
            ChatCompletionChoice choice = new ChatCompletionChoice();
            choice.setIndex(0);
            
            // 创建 delta 消息（转换为标准格式）
            ChatMessage delta = new ChatMessage();
            
            if (messageNode.has("role")) {
                delta.setRole(messageNode.get("role").asText());
            }
            
            if (messageNode.has("content")) {
                String content = messageNode.get("content").asText();
                // Ollama 格式中，每个 chunk 的 content 就是增量内容
                delta.setContent(content);
                log.debug("Ollama chunk 增量内容: '{}'", content);
            }
            
            choice.setDelta(delta);
            
            // 检查是否完成
            boolean done = ollamaNode.has("done") && ollamaNode.get("done").asBoolean();
            if (done) {
                choice.setFinishReason("stop");
                log.debug("Ollama 流结束标记: done=true");
            }
            
            chunk.setChoices(java.util.Arrays.asList(choice));
        }
        
        // 处理 usage 信息（如果存在，通常在最后一个 chunk 中）
        if (ollamaNode.has("usage")) {
            com.fasterxml.jackson.databind.JsonNode usageNode = ollamaNode.get("usage");
            ChatCompletionUsage usage = new ChatCompletionUsage();
            
            if (usageNode.has("prompt_tokens")) {
                usage.setPromptTokens(usageNode.get("prompt_tokens").asInt());
            }
            if (usageNode.has("completion_tokens")) {
                usage.setCompletionTokens(usageNode.get("completion_tokens").asInt());
            }
            if (usageNode.has("total_tokens")) {
                usage.setTotalTokens(usageNode.get("total_tokens").asInt());
            } else if (usage.getPromptTokens() != null && usage.getCompletionTokens() != null) {
                usage.setTotalTokens(usage.getPromptTokens() + usage.getCompletionTokens());
            }
            
            chunk.setUsage(usage);
            log.debug("Ollama usage 信息: {}", usage);
        }
        
        log.debug("成功解析 Ollama 格式数据: model={}, content='{}'", 
                chunk.getModel(), 
                chunk.getChoices() != null && !chunk.getChoices().isEmpty() && chunk.getChoices().get(0).getDelta() != null 
                    ? chunk.getChoices().get(0).getDelta().getContent() : "null");
        
        return chunk;
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
            // 1. 使用必填的会话ID
            Long conversationId = request.getConversationId();
            log.info("使用会话: conversationId={}, userId={}, modelId={}", 
                    conversationId, userId, model.getId());
            
            // 验证会话是否属于当前用户
            Conversations existingConversation = conversationsService.getById(conversationId);
            if (existingConversation == null || !existingConversation.getUserId().equals(userId)) {
                throw new RuntimeException("会话不存在或无权限访问");
            }
            
            // 更新会话的模型ID（如果需要）
            updateConversationModelId(conversationId, model.getId());
            
            // 2. 保存用户消息（只保存最新的用户消息，避免重复保存历史记录）
            ChatMessage latestUserMessage = null;
            for (ChatMessage message : request.getMessages()) {
                if ("user".equals(message.getRole())) {
                    latestUserMessage = message; // 保留最后一条用户消息
                }
            }
            
            // 只保存最新的用户消息
            if (latestUserMessage != null) {
                Messages userMessage = new Messages();
                userMessage.setConversationId(conversationId);
                userMessage.setRole(latestUserMessage.getRole());
                userMessage.setContent(latestUserMessage.getContent());
                userMessage.setModelId(model.getId().intValue());
                userMessage.setTokensConsumed(0); // 用户消息不消耗 token
                userMessage.setCreatedAt(new Date());
                
                messagesService.save(userMessage);
                log.debug("保存最新用户消息: {}", userMessage.getId());
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
            
            String jsonData = null;
            
            if (line.startsWith("data: ")) {
                // 已有 data: 前缀，提取 JSON 部分用于解析
                jsonData = line.substring(6).trim();
                
                // 转发时去掉 data: 前缀，让 SseEmitter 自动添加
                emitter.send(SseEmitter.event()
                    .data(jsonData)
                    .reconnectTime(0L));
                    
            } else if (line.startsWith("event:") || line.startsWith("id:") || line.startsWith("retry:")) {
                // 其他 SSE 字段原样转发
                emitter.send(SseEmitter.event().data(line));
                log.debug("转发 SSE 元数据: {}", line);
                continue;
                
            } else {
                // 纯 JSON 数据（没有 data: 前缀），直接转发
                jsonData = line;
                
                // 直接转发数据，让 SseEmitter 自动添加 data: 前缀
                emitter.send(SseEmitter.event()
                    .data(jsonData)
                    .reconnectTime(0L));
            }
            
            // 处理 JSON 数据用于本地解析和积累
            if (jsonData != null) {
                if ("[DONE]".equals(jsonData)) {
                    log.debug("收到流结束标记，已转发，等待流正常结束");
                    continue;
                }
                
                // 解析 JSON chunk 用于本地处理
                ChatCompletionChunk chunk = parseChunk(jsonData);
                if (chunk != null) {
                    // 积累内容用于数据库保存
                    if (chunk.getChoices() != null && !chunk.getChoices().isEmpty()) {
                        ChatCompletionChoice choice = chunk.getChoices().get(0);
                        
                        // 处理 delta 内容（兼容 OpenAI 和 Ollama 格式）
                        if (choice.getDelta() != null && choice.getDelta().getContent() != null) {
                            String content = choice.getDelta().getContent();
                            fullContent.append(content);
                            log.debug("拼接流式内容: '{}'", content);
                        }
                        
                        // 检查是否是流结束标记
                        if ("stop".equals(choice.getFinishReason())) {
                            log.debug("收到流结束标记 (finishReason=stop)");
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
            }
        }
    }
    
    /**
     * 在流式响应出错时保存已收到的数据
     */
    private void saveStreamDataOnError(Long userId, AiModels model, ChatCompletionRequest request, 
                                     String content, ChatCompletionUsage usage) {
        try {
            if (content != null && !content.trim().isEmpty()) {
                log.info("尝试保存部分流式响应内容到数据库");
                
                // 创建模拟响应对象
                ChatCompletionResponse mockResponse = new ChatCompletionResponse();
                ChatCompletionChoice choice = new ChatCompletionChoice();
                ChatMessage message = new ChatMessage();
                message.setRole("assistant");
                message.setContent(content);
                choice.setMessage(message);
                mockResponse.setChoices(java.util.Arrays.asList(choice));
                
                // 如果没有usage，创建估算的
                if (usage == null) {
                    usage = createEstimatedUsage(request, mockResponse);
                }
                mockResponse.setUsage(usage);
                
                // 保存记录，但不计费（避免重复扣费）
                saveConversationRecord(userId, model, request, mockResponse, usage, BigDecimal.ZERO);
                log.info("成功保存部分流式响应内容");
            }
        } catch (Exception e) {
            log.error("保存部分流式响应内容失败", e);
        }
    }
    
    /**
     * 更新会话的模型ID
     * 临时解决方案：直接通过Mapper更新，因为原始Conversations实体不包含modelId字段
     */
    private void updateConversationModelId(Long conversationId, Integer modelId) {
        try {
            // 使用注入的ConversationsMapper来更新model_id字段
            int rows = conversationsMapper.updateModelId(conversationId, modelId);
            if (rows > 0) {
                log.info("成功更新会话 {} 的模型ID为 {}", conversationId, modelId);
            } else {
                log.warn("更新会话模型ID未影响任何行: conversationId={}, modelId={}", conversationId, modelId);
            }
        } catch (Exception e) {
            log.error("更新会话模型ID失败: conversationId={}, modelId={}", conversationId, modelId, e);
        }
    }
}
