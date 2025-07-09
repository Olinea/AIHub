package su.sue.aiproject.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import su.sue.aiproject.domain.dto.ChatMessage;
import su.sue.aiproject.domain.dto.GenerateTitleRequest;
import su.sue.aiproject.domain.dto.GenerateTitleResponse;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 阿里云Qwen服务，用于生成对话标题
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class QwenTitleGeneratorService {
    
    // 初始化.env配置
    private static final Dotenv dotenv;
    
    static {
        try {
            dotenv = Dotenv.configure()
                    .directory(".")
                    .ignoreIfMissing()
                    .load();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load .env file", e);
        }
    }
    
    // 从环境变量读取阿里云配置，如果没有则从.env文件读取
    private static final String API_KEY = getConfigValue("QWEN_API_KEY", "");
    private static final String BASE_URL = getConfigValue("QWEN_BASE_URL", "https://dashscope.aliyuncs.com/compatible-mode/v1");
    private static final String MODEL = getConfigValue("QWEN_MODEL", "qwen-turbo");
    
    /**
     * 获取配置值，优先从环境变量读取，然后从.env文件读取，最后使用默认值
     */
    private static String getConfigValue(String key, String defaultValue) {
        // 1. 首先尝试从系统环境变量读取
        String value = System.getenv(key);
        if (value != null && !value.trim().isEmpty()) {
            return value;
        }
        
        // 2. 然后尝试从.env文件读取
        if (dotenv != null) {
            value = dotenv.get(key);
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }
        
        // 3. 最后使用默认值
        return defaultValue;
    }
    
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    
    /**
     * 生成对话标题
     * 
     * @param request 生成标题请求
     * @param userId 用户ID（用于日志记录）
     * @return 生成的标题响应
     */
    public GenerateTitleResponse generateTitle(GenerateTitleRequest request, Long userId) {
        try {
            log.info("开始为会话 {} 生成标题", request.getConversationId());
            
            // 构建用于生成标题的提示
            String prompt = buildTitlePrompt(request.getMessages(), request.getPrompt());
            
            // 构建请求体
            Map<String, Object> requestBody = buildRequestBody(prompt);
            
            // 创建WebClient
            WebClient webClient = webClientBuilder
                    .baseUrl(BASE_URL)
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + API_KEY)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();
            
            // 调用API
            String response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();
            
            // 解析响应并提取标题
            String generatedTitle = extractTitleFromResponse(response);
            
            log.info("为会话 {} 生成标题成功: {}", request.getConversationId(), generatedTitle);
            
            return new GenerateTitleResponse(generatedTitle, request.getConversationId());
            
        } catch (Exception e) {
            log.error("生成标题失败，会话ID: {}, 错误: {}", request.getConversationId(), e.getMessage(), e);
            // 返回默认标题
            return new GenerateTitleResponse("新对话", request.getConversationId());
        }
    }
    
    /**
     * 生成对话总汇标题
     * 
     * @param request 生成标题请求
     * @param userId 用户ID（用于日志记录）
     * @return 生成的标题响应
     */
    public GenerateTitleResponse generateSummaryTitle(GenerateTitleRequest request, Long userId) {
        try {
            log.info("开始为会话 {} 生成对话总汇标题", request.getConversationId());
            
            // 构建用于生成总汇标题的提示（使用不同的提示词）
            String prompt = buildSummaryTitlePrompt(request.getMessages());
            
            // 构建请求体
            Map<String, Object> requestBody = buildRequestBody(prompt);
            
            // 创建WebClient
            WebClient webClient = webClientBuilder
                    .baseUrl(BASE_URL)
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + API_KEY)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();
            
            // 调用API
            String response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();
            
            // 解析响应并提取标题
            String generatedTitle = extractTitleFromResponse(response);
            
            log.info("为会话 {} 生成对话总汇标题成功: {}", request.getConversationId(), generatedTitle);
            
            return new GenerateTitleResponse(generatedTitle, request.getConversationId());
            
        } catch (Exception e) {
            log.error("生成对话总汇标题失败，会话ID: {}, 错误: {}", request.getConversationId(), e.getMessage(), e);
            // 返回默认标题
            return new GenerateTitleResponse("对话总汇", request.getConversationId());
        }
    }
    
    /**
     * 构建请求体
     * 
     * @param prompt 提示词
     * @return 请求体Map
     */
    private Map<String, Object> buildRequestBody(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", MODEL);
        requestBody.put("max_tokens", 100);
        requestBody.put("temperature", 0.7);
        requestBody.put("top_p", 1.0);
        
        // 构建消息列表
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        
        requestBody.put("messages", List.of(message));
        
        return requestBody;
    }
    
    /**
     * 构建生成标题的提示词
     * 
     * @param messages 对话消息
     * @param customPrompt 自定义提示词
     * @return 完整的提示词
     */
    private String buildTitlePrompt(List<ChatMessage> messages, String customPrompt) {
        StringBuilder prompt = new StringBuilder();
        
        // 添加基础提示
        if (customPrompt != null && !customPrompt.trim().isEmpty()) {
            prompt.append(customPrompt).append("\n\n");
        } else {
            prompt.append("请为以下对话生成一个简洁、准确的中文标题（不超过20个字符，不要包含引号）：\n\n");
        }
        
        // 添加对话内容（只取前几条消息以避免token过多）
        int messageCount = Math.min(messages.size(), 10);
        for (int i = 0; i < messageCount; i++) {
            ChatMessage message = messages.get(i);
            String role = "user".equals(message.getRole()) ? "用户" : "助手";
            prompt.append(role).append(": ")
                  .append(message.getContent().length() > 200 
                          ? message.getContent().substring(0, 200) + "..." 
                          : message.getContent())
                  .append("\n");
        }
        
        prompt.append("\n请生成标题：");
        
        return prompt.toString();
    }
    
    /**
     * 构建生成对话总汇标题的提示词
     * 
     * @param messages 对话消息
     * @return 完整的提示词
     */
    private String buildSummaryTitlePrompt(List<ChatMessage> messages) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("请基于以下完整的对话历史，生成一个能够概括整个对话主题的标题（不超过25个字符，不要包含引号）：\n\n");
        prompt.append("注意：这是一个对话的总汇标题，需要反映整个对话的核心内容和主要讨论点。\n\n");
        
        // 添加所有对话内容，但适当截断过长的消息
        for (int i = 0; i < messages.size(); i++) {
            ChatMessage message = messages.get(i);
            String role = "user".equals(message.getRole()) ? "用户" : "助手";
            String content = message.getContent();
            
            // 如果消息太长，截断但保留关键信息
            if (content.length() > 300) {
                content = content.substring(0, 150) + "..." + content.substring(content.length() - 100);
            }
            
            prompt.append(role).append(": ").append(content).append("\n");
        }
        
        prompt.append("\n请生成能够反映整个对话核心主题的总汇标题：");
        
        return prompt.toString();
    }
    
    /**
     * 从API响应中提取标题
     * 
     * @param response API响应JSON字符串
     * @return 提取的标题
     */
    private String extractTitleFromResponse(String response) {
        try {
            JsonNode responseNode = objectMapper.readTree(response);
            JsonNode choices = responseNode.get("choices");
            
            if (choices == null || choices.size() == 0) {
                log.warn("API响应中没有choices字段或为空");
                return "新对话";
            }
            
            JsonNode firstChoice = choices.get(0);
            JsonNode message = firstChoice.get("message");
            
            if (message == null) {
                log.warn("API响应中没有message字段");
                return "新对话";
            }
            
            JsonNode content = message.get("content");
            if (content == null) {
                log.warn("API响应中没有content字段");
                return "新对话";
            }
            
            String title = content.asText().trim();
            
            // 清理标题（移除引号、换行符等）
            title = title.replaceAll("^[\"']+|[\"']+$", "");
            title = title.replaceAll("\\n", " ");
            title = title.replaceAll("标题：|标题:", "");
            title = title.trim();
            
            // 限制标题长度
            if (title.length() > 50) {
                title = title.substring(0, 47) + "...";
            }
            
            return title.isEmpty() ? "新对话" : title;
            
        } catch (Exception e) {
            log.error("解析API响应失败: {}", e.getMessage(), e);
            return "新对话";
        }
    }
}
