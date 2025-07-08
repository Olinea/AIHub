package su.sue.aiproject.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import su.sue.aiproject.domain.AiModels;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * AI API连接测试服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiApiTestService {

    private final ObjectMapper objectMapper;
    private final WebClient.Builder webClientBuilder;

    /**
     * 测试AI模型API连接
     * @param model AI模型配置
     * @return 测试结果
     */
    public String testApiConnection(AiModels model) {
        try {
            // 构建WebClient
            WebClient webClient = webClientBuilder
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();

            // 根据提供商类型构建请求
            switch (model.getProvider().toLowerCase()) {
                case "openai":
                    return testOpenAiCompatibleApi(webClient, model);
                case "anthropic":
                case "claude":
                    return testAnthropicApi(webClient, model);
                case "google":
                case "gemini":
                    return testGoogleApi(webClient, model);
                case "baidu":
                case "wenxin":
                    return testBaiduApi(webClient, model);
                case "alibaba":
                case "tongyi":
                    return testAlibabaApi(webClient, model);
                case "zhipu":
                case "chatglm":
                    return testZhipuApi(webClient, model);
                default:
                    // 默认使用OpenAI兼容格式
                    return testOpenAiCompatibleApi(webClient, model);
            }
        } catch (Exception e) {
            log.error("API连接测试失败: {}", e.getMessage(), e);
            return "连接测试失败: " + e.getMessage();
        }
    }

    /**
     * 测试OpenAI兼容的API
     */
    private String testOpenAiCompatibleApi(WebClient webClient, AiModels model) {
        try {
            // 构建测试请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model.getModelName());
            requestBody.put("messages", new Object[]{
                Map.of("role", "user", "content", "Hello, this is a connection test.")
            });
            requestBody.put("max_tokens", 10);
            requestBody.put("temperature", 0.1);

            // 构建请求头
            WebClient.RequestBodySpec request = webClient.post()
                    .uri(model.getApiEndpoint())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + model.getApiKey());

            // 添加组织ID（如果有）
            if (model.getOrganizationId() != null && !model.getOrganizationId().trim().isEmpty()) {
                request.header("OpenAI-Organization", model.getOrganizationId());
            }

            // 添加项目ID（如果有）
            if (model.getProjectId() != null && !model.getProjectId().trim().isEmpty()) {
                request.header("OpenAI-Project", model.getProjectId());
            }

            // 添加额外请求头（如果有）
            if (model.getExtraHeaders() != null && !model.getExtraHeaders().trim().isEmpty()) {
                try {
                    Map<String, String> extraHeaders = objectMapper.readValue(
                            model.getExtraHeaders(), 
                            new TypeReference<Map<String, String>>() {}
                    );
                    extraHeaders.forEach(request::header);
                } catch (Exception e) {
                    log.warn("解析额外请求头失败: {}", e.getMessage());
                }
            }

            // 发送请求
            String response = request
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            // 解析响应
            try {
                JsonNode responseNode = objectMapper.readTree(response);
                
                if (responseNode.has("choices") && responseNode.get("choices").isArray() && 
                    responseNode.get("choices").size() > 0) {
                    return "✅ OpenAI兼容API连接测试成功\n" +
                           "响应模型: " + (responseNode.has("model") ? responseNode.get("model").asText() : "未知") + "\n" +
                           "响应内容: " + responseNode.get("choices").get(0).get("message").get("content").asText();
                } else {
                    return "⚠️ API连接成功但响应格式异常: " + response;
                }
            } catch (Exception e) {
                return "⚠️ API连接成功但响应解析失败: " + e.getMessage();
            }

        } catch (WebClientResponseException e) {
            return handleHttpError("OpenAI兼容API", e);
        }
    }

    /**
     * 测试Anthropic API (Claude)
     */
    private String testAnthropicApi(WebClient webClient, AiModels model) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model.getModelName());
            requestBody.put("max_tokens", 10);
            requestBody.put("messages", new Object[]{
                Map.of("role", "user", "content", "Hello, this is a connection test.")
            });

            String response = webClient.post()
                    .uri(model.getApiEndpoint())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + model.getApiKey())
                    .header("anthropic-version", "2023-06-01")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            try {
                JsonNode responseNode = objectMapper.readTree(response);
                if (responseNode.has("content") && responseNode.get("content").isArray()) {
                    return "✅ Anthropic API连接测试成功\n" +
                           "响应模型: " + (responseNode.has("model") ? responseNode.get("model").asText() : "未知") + "\n" +
                           "响应内容: " + responseNode.get("content").get(0).get("text").asText();
                } else {
                    return "⚠️ API连接成功但响应格式异常: " + response;
                }
            } catch (Exception e) {
                return "⚠️ API连接成功但响应解析失败: " + e.getMessage();
            }

        } catch (WebClientResponseException e) {
            return handleHttpError("Anthropic API", e);
        }
    }

    /**
     * 测试Google API (Gemini)
     */
    private String testGoogleApi(WebClient webClient, AiModels model) {
        try {
            // Google API使用不同的URL模式
            String url = model.getApiEndpoint();
            if (!url.contains("key=")) {
                url += (url.contains("?") ? "&" : "?") + "key=" + model.getApiKey();
            }

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", new Object[]{
                Map.of("parts", new Object[]{
                    Map.of("text", "Hello, this is a connection test.")
                })
            });
            requestBody.put("generationConfig", Map.of("maxOutputTokens", 10));

            String response = webClient.post()
                    .uri(url)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            try {
                JsonNode responseNode = objectMapper.readTree(response);
                if (responseNode.has("candidates") && responseNode.get("candidates").isArray()) {
                    return "✅ Google API连接测试成功\n" +
                           "响应内容: " + responseNode.get("candidates").get(0)
                           .get("content").get("parts").get(0).get("text").asText();
                } else {
                    return "⚠️ API连接成功但响应格式异常: " + response;
                }
            } catch (Exception e) {
                return "⚠️ API连接成功但响应解析失败: " + e.getMessage();
            }

        } catch (WebClientResponseException e) {
            return handleHttpError("Google API", e);
        }
    }

    /**
     * 测试百度API
     */
    private String testBaiduApi(WebClient webClient, AiModels model) {
        // 百度API需要先获取access_token，这里简化处理
        return testOpenAiCompatibleApi(webClient, model);
    }

    /**
     * 测试阿里巴巴API
     */
    private String testAlibabaApi(WebClient webClient, AiModels model) {
        // 阿里云API通常兼容OpenAI格式
        return testOpenAiCompatibleApi(webClient, model);
    }

    /**
     * 测试智谱API
     */
    private String testZhipuApi(WebClient webClient, AiModels model) {
        // 智谱API通常兼容OpenAI格式
        return testOpenAiCompatibleApi(webClient, model);
    }

    /**
     * 处理HTTP错误
     */
    private String handleHttpError(String apiType, WebClientResponseException e) {
        String errorMsg = "❌ " + apiType + "连接测试失败\n" +
                         "状态码: " + e.getStatusCode() + "\n" +
                         "错误信息: " + e.getMessage();
        
        try {
            String responseBody = e.getResponseBodyAsString();
            if (responseBody != null && !responseBody.isEmpty()) {
                try {
                    JsonNode errorNode = objectMapper.readTree(responseBody);
                    if (errorNode.has("error")) {
                        JsonNode error = errorNode.get("error");
                        if (error.has("message")) {
                            errorMsg += "\nAPI错误: " + error.get("message").asText();
                        }
                        if (error.has("type")) {
                            errorMsg += "\n错误类型: " + error.get("type").asText();
                        }
                        if (error.has("code")) {
                            errorMsg += "\n错误代码: " + error.get("code").asText();
                        }
                    }
                } catch (Exception parseEx) {
                    log.debug("解析错误响应失败: {}", parseEx.getMessage());
                    errorMsg += "\n原始错误响应: " + responseBody;
                }
            }
        } catch (Exception ex) {
            log.debug("处理错误响应失败: {}", ex.getMessage());
        }
        
        return errorMsg;
    }
}
