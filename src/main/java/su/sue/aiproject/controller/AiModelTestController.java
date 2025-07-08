package su.sue.aiproject.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import su.sue.aiproject.domain.AiModels;
import su.sue.aiproject.domain.ApiResponse;
import su.sue.aiproject.service.AiModelsService;
import su.sue.aiproject.service.AiApiTestService;

@RestController
@RequestMapping("/api/admin/ai-models")
@RequiredArgsConstructor
@Tag(name = "AI模型测试", description = "AI模型API密钥测试相关接口")
@PreAuthorize("hasRole('ADMIN')")
public class AiModelTestController {

    private final AiModelsService aiModelsService;
    private final AiApiTestService aiApiTestService;

    @PostMapping("/{id}/test-connection")
    @Operation(summary = "测试API连接", description = "测试指定AI模型的API连接是否正常，支持OpenAI兼容格式及主流AI服务商")
    public ResponseEntity<ApiResponse<String>> testApiConnection(
            @Parameter(description = "模型ID") @PathVariable Integer id) {
        
        // 获取模型信息
        AiModels model = aiModelsService.getModelWithDecryptedKeys(id);
        if (model == null) {
            return ResponseEntity.notFound().build();
        }

        // 验证必要字段
        if (model.getApiKey() == null || model.getApiKey().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("API密钥未配置"));
        }

        if (model.getApiEndpoint() == null || model.getApiEndpoint().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("API端点未配置"));
        }

        if (model.getModelName() == null || model.getModelName().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("模型名称未配置"));
        }

        // 执行实际的API连接测试
        try {
            String testResult = aiApiTestService.testApiConnection(model);
            
            // 构建详细的测试报告
            StringBuilder detailReport = new StringBuilder();
            detailReport.append("🔍 测试模型信息:\n");
            detailReport.append("模型名称: ").append(model.getModelName()).append("\n");
            detailReport.append("提供商: ").append(model.getProvider()).append("\n");
            detailReport.append("API端点: ").append(model.getApiEndpoint()).append("\n");
            detailReport.append("API密钥: ").append(maskApiKey(model.getApiKey())).append("\n");
            
            if (model.getOrganizationId() != null && !model.getOrganizationId().trim().isEmpty()) {
                detailReport.append("组织ID: ").append(model.getOrganizationId()).append("\n");
            }
            if (model.getProjectId() != null && !model.getProjectId().trim().isEmpty()) {
                detailReport.append("项目ID: ").append(model.getProjectId()).append("\n");
            }
            
            detailReport.append("\n📊 测试结果:\n");
            detailReport.append(testResult);
            
            // 根据测试结果判断是否成功
            if (isTestSuccessful(testResult)) {
                return ResponseEntity.ok(ApiResponse.success("API连接测试完成", detailReport.toString()));
            } else {
                // 区分不同类型的错误
                if (isConfigurationError(testResult)) {
                    // 配置错误，返回400
                    return ResponseEntity.badRequest()
                            .body(new ApiResponse<>(400, "API连接测试失败（配置错误）", detailReport.toString()));
                } else {
                    // 连接错误，返回500
                    return ResponseEntity.internalServerError()
                            .body(new ApiResponse<>(500, "API连接测试失败（连接错误）", detailReport.toString()));
                }
            }
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("API连接测试失败: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/show-config")
    @Operation(summary = "显示模型配置", description = "显示指定AI模型的完整配置信息")
    public ResponseEntity<ApiResponse<AiModels>> showModelConfig(
            @Parameter(description = "模型ID") @PathVariable Integer id) {
        
        // 获取模型信息
        AiModels model = aiModelsService.getModelWithDecryptedKeys(id);
        if (model == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(ApiResponse.success("获取模型配置成功", model));
    }

    /**
     * 掩码API密钥，只显示前几位和后几位
     */
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() <= 8) {
            return "***";
        }
        if (apiKey.length() <= 16) {
            return apiKey.substring(0, 4) + "***" + apiKey.substring(apiKey.length() - 4);
        }
        return apiKey.substring(0, 8) + "***" + apiKey.substring(apiKey.length() - 8);
    }

    /**
     * 判断测试是否成功
     */
    private boolean isTestSuccessful(String testResult) {
        return testResult != null && testResult.contains("✅");
    }

    /**
     * 判断是否为配置错误
     */
    private boolean isConfigurationError(String testResult) {
        if (testResult == null) {
            return false;
        }
        
        // 检查常见的配置错误关键字
        String lowerResult = testResult.toLowerCase();
        return lowerResult.contains("unauthorized") || 
               lowerResult.contains("401") ||
               lowerResult.contains("invalid authentication") ||
               lowerResult.contains("api key") ||
               lowerResult.contains("forbidden") ||
               lowerResult.contains("403") ||
               lowerResult.contains("invalid_request_error") ||
               lowerResult.contains("authentication_error") ||
               lowerResult.contains("invalid api key") ||
               lowerResult.contains("not found") ||
               lowerResult.contains("404");
    }
}
