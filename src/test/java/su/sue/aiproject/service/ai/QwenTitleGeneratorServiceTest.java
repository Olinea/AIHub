package su.sue.aiproject.service.ai;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import su.sue.aiproject.domain.dto.ChatMessage;
import su.sue.aiproject.domain.dto.GenerateTitleRequest;
import su.sue.aiproject.domain.dto.GenerateTitleResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * QwenTitleGeneratorService 测试类
 */
@ExtendWith(MockitoExtension.class)
class QwenTitleGeneratorServiceTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private QwenTitleGeneratorService qwenTitleGeneratorService;

    @Test
    void testGenerateTitleWithValidRequest() {
        // 准备测试数据
        ChatMessage userMessage = new ChatMessage();
        userMessage.setRole("user");
        userMessage.setContent("请介绍一下Spring Boot的基本概念");

        ChatMessage assistantMessage = new ChatMessage();
        assistantMessage.setRole("assistant");
        assistantMessage.setContent("Spring Boot是一个基于Spring框架的开发工具，它简化了Spring应用的创建和部署过程...");

        List<ChatMessage> messages = Arrays.asList(userMessage, assistantMessage);

        GenerateTitleRequest request = new GenerateTitleRequest();
        request.setConversationId(123L);
        request.setMessages(messages);

        // 由于依赖外部API，这里只测试方法不会抛出异常
        // 在实际测试中，应该mock WebClient的行为
        assertDoesNotThrow(() -> {
            GenerateTitleResponse response = qwenTitleGeneratorService.generateTitle(request, 1L);
            assertNotNull(response);
            assertNotNull(response.getTitle());
            assertEquals(123L, response.getConversationId());
        });
    }

    @Test
    void testGenerateSummaryTitleWithValidRequest() {
        // 准备测试数据
        ChatMessage userMessage1 = new ChatMessage();
        userMessage1.setRole("user");
        userMessage1.setContent("请介绍一下Spring Boot");

        ChatMessage assistantMessage1 = new ChatMessage();
        assistantMessage1.setRole("assistant");
        assistantMessage1.setContent("Spring Boot是一个开发框架...");

        ChatMessage userMessage2 = new ChatMessage();
        userMessage2.setRole("user");
        userMessage2.setContent("如何创建一个Spring Boot项目？");

        ChatMessage assistantMessage2 = new ChatMessage();
        assistantMessage2.setRole("assistant");
        assistantMessage2.setContent("可以使用Spring Initializr来快速创建项目...");

        List<ChatMessage> messages = Arrays.asList(
            userMessage1, assistantMessage1, userMessage2, assistantMessage2
        );

        GenerateTitleRequest request = new GenerateTitleRequest();
        request.setConversationId(123L);
        request.setMessages(messages);

        // 测试方法不会抛出异常
        assertDoesNotThrow(() -> {
            GenerateTitleResponse response = qwenTitleGeneratorService.generateSummaryTitle(request, 1L);
            assertNotNull(response);
            assertNotNull(response.getTitle());
            assertEquals(123L, response.getConversationId());
        });
    }

    @Test
    void testGenerateTitleWithEmptyMessages() {
        GenerateTitleRequest request = new GenerateTitleRequest();
        request.setConversationId(123L);
        request.setMessages(Arrays.asList());

        // 测试空消息列表的处理
        assertDoesNotThrow(() -> {
            GenerateTitleResponse response = qwenTitleGeneratorService.generateTitle(request, 1L);
            assertNotNull(response);
            // 应该返回默认标题
            assertEquals("新对话", response.getTitle());
        });
    }
}
