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

@RestController
@RequestMapping("/api/admin/ai-models")
@RequiredArgsConstructor
@Tag(name = "AI模型测试", description = "AI模型API密钥测试相关接口")
@PreAuthorize("hasRole('ADMIN')")
public class AiModelTestController {

    private final AiModelsService aiModelsService;

    @PostMapping("/{id}/test-connection")
    @Operation(summary = "测试API连接", description = "测试指定AI模型的API连接是否正常")
    public ResponseEntity<ApiResponse<String>> testApiConnection(
            @Parameter(description = "模型ID") @PathVariable Integer id) {
        
        // 获取模型信息
        AiModels model = aiModelsService.getModelWithDecryptedKeys(id);
        if (model == null) {
            return ResponseEntity.notFound().build();
        }

        // 这里应该实际调用对应的AI服务API进行测试
        // 为了演示，我们只是检查必要字段是否存在
        if (model.getApiKey() == null || model.getApiKey().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("API密钥未配置"));
        }

        if (model.getApiEndpoint() == null || model.getApiEndpoint().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("API端点未配置"));
        }

        // TODO: 实际实现中应该调用对应AI服务的API进行连接测试
        // 这里只是模拟测试成功
        String testResult = String.format("模型 %s (%s) 连接测试通过\n" +
                "API端点: %s\n" +
                "API密钥: %s...\n" +
                "组织ID: %s\n" +
                "项目ID: %s", 
                model.getModelName(), 
                model.getProvider(),
                model.getApiEndpoint(),
                model.getApiKey().length() > 10 ? model.getApiKey().substring(0, 10) : model.getApiKey(),
                model.getOrganizationId(),
                model.getProjectId());

        return ResponseEntity.ok(ApiResponse.success("API连接测试完成", testResult));
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
}
