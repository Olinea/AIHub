package su.sue.aiproject.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import su.sue.aiproject.domain.AiModels;
import su.sue.aiproject.service.AiModelsService;
import su.sue.aiproject.service.AiApiTestService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AiModelTestController.class)
public class AiModelTestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AiModelsService aiModelsService;

    @MockBean
    private AiApiTestService aiApiTestService;

    @Autowired
    private ObjectMapper objectMapper;

    private AiModels testModel;

    @BeforeEach
    void setUp() {
        testModel = new AiModels();
        testModel.setId(1);
        testModel.setModelName("gpt-4");
        testModel.setProvider("OpenAI");
        testModel.setApiEndpoint("https://api.openai.com/v1/chat/completions");
        testModel.setApiKey("sk-test-key-12345");
        testModel.setOrganizationId("org-test");
        testModel.setProjectId("proj-test");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testApiConnection_Success_ShouldReturn200() throws Exception {
        // Mock successful API test
        when(aiModelsService.getModelWithDecryptedKeys(1)).thenReturn(testModel);
        when(aiApiTestService.testApiConnection(any(AiModels.class)))
                .thenReturn("✅ OpenAI兼容API连接测试成功\n响应模型: gpt-4\n响应内容: Hello! This is a test message.");

        mockMvc.perform(post("/api/admin/ai-models/1/test-connection")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("API连接测试完成"))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testApiConnection_ConfigurationError_ShouldReturn400() throws Exception {
        // Mock configuration error (401 Unauthorized)
        when(aiModelsService.getModelWithDecryptedKeys(1)).thenReturn(testModel);
        when(aiApiTestService.testApiConnection(any(AiModels.class)))
                .thenReturn("❌ OpenAI兼容API连接测试失败\n状态码: 401 UNAUTHORIZED\n错误信息: Unauthorized\nAPI错误: Invalid authentication credentials");

        mockMvc.perform(post("/api/admin/ai-models/1/test-connection")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("API连接测试失败（配置错误）"))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testApiConnection_ConnectionError_ShouldReturn500() throws Exception {
        // Mock connection error (timeout)
        when(aiModelsService.getModelWithDecryptedKeys(1)).thenReturn(testModel);
        when(aiApiTestService.testApiConnection(any(AiModels.class)))
                .thenReturn("连接测试失败: Connection timed out: getsockopt: api.openai.com/199.59.148.201:443");

        mockMvc.perform(post("/api/admin/ai-models/1/test-connection")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpected(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("API连接测试失败（连接错误）"))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testApiConnection_ModelNotFound_ShouldReturn404() throws Exception {
        // Mock model not found
        when(aiModelsService.getModelWithDecryptedKeys(999)).thenReturn(null);

        mockMvc.perform(post("/api/admin/ai-models/999/test-connection")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testApiConnection_MissingApiKey_ShouldReturn400() throws Exception {
        // Mock model with missing API key
        testModel.setApiKey(null);
        when(aiModelsService.getModelWithDecryptedKeys(1)).thenReturn(testModel);

        mockMvc.perform(post("/api/admin/ai-models/1/test-connection")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("API密钥未配置"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testApiConnection_MissingApiEndpoint_ShouldReturn400() throws Exception {
        // Mock model with missing API endpoint
        testModel.setApiEndpoint(null);
        when(aiModelsService.getModelWithDecryptedKeys(1)).thenReturn(testModel);

        mockMvc.perform(post("/api/admin/ai-models/1/test-connection")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpected(jsonPath("$.message").value("API端点未配置"));
    }
}
