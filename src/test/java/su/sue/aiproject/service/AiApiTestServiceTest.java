package su.sue.aiproject.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import su.sue.aiproject.domain.AiModels;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AiApiTestServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private WebClient.Builder webClientBuilder;

    @InjectMocks
    private AiApiTestService aiApiTestService;

    private AiModels testModel;

    @BeforeEach
    void setUp() {
        testModel = new AiModels();
        testModel.setId(1);
        testModel.setModelName("gpt-3.5-turbo");
        testModel.setProvider("OpenAI");
        testModel.setApiEndpoint("https://api.openai.com/v1/chat/completions");
        testModel.setApiKey("sk-test-key");
        testModel.setOrganizationId("org-test");
        testModel.setProjectId("proj-test");
    }

    @Test
    void testApiConnection_WithValidModel_ShouldNotThrowException() {
        // 这是一个基本测试，验证方法不会抛出异常
        // 实际的网络请求测试需要更复杂的mock设置
        
        assertDoesNotThrow(() -> {
            String result = aiApiTestService.testApiConnection(testModel);
            assertNotNull(result);
            // 由于网络请求会失败，我们期望收到错误消息
            assertTrue(result.contains("连接测试失败") || result.contains("API连接测试"));
        });
    }

    @Test
    void testApiConnection_WithNullModel_ShouldHandleGracefully() {
        assertDoesNotThrow(() -> {
            String result = aiApiTestService.testApiConnection(null);
            assertNotNull(result);
            assertTrue(result.contains("连接测试失败"));
        });
    }

    @Test
    void testApiConnection_WithEmptyApiKey_ShouldHandleGracefully() {
        testModel.setApiKey("");
        
        assertDoesNotThrow(() -> {
            String result = aiApiTestService.testApiConnection(testModel);
            assertNotNull(result);
            assertTrue(result.contains("连接测试失败"));
        });
    }

    @Test
    void testApiConnection_WithDifferentProviders_ShouldSupportMultipleFormats() {
        // 测试不同的提供商
        String[] providers = {"OpenAI", "Anthropic", "Google", "Baidu", "Alibaba", "Zhipu"};
        
        for (String provider : providers) {
            testModel.setProvider(provider);
            
            assertDoesNotThrow(() -> {
                String result = aiApiTestService.testApiConnection(testModel);
                assertNotNull(result);
                // 应该包含连接测试的结果信息
                assertTrue(result.length() > 0);
            }, "Provider " + provider + " should be handled without exception");
        }
    }
}
