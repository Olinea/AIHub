package su.sue.aiproject.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import su.sue.aiproject.domain.AiModels;
import su.sue.aiproject.domain.ApiResponse;
import su.sue.aiproject.service.AiModelsService;

import java.util.List;

@RestController
@RequestMapping("/api/ai-models")
@RequiredArgsConstructor
@Tag(name = "AI模型", description = "AI模型相关接口 - 普通用户可访问")
public class AiModelController {

    private final AiModelsService aiModelsService;

    @GetMapping("/enabled")
    @Operation(summary = "获取启用的AI模型列表", description = "获取所有启用状态的AI模型供用户选择")
    public ResponseEntity<ApiResponse<List<AiModels>>> getEnabledAiModels() {
        QueryWrapper<AiModels> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_enabled", true)
                   .orderBy(true, true, "provider", "model_name");
        
        List<AiModels> models = aiModelsService.list(queryWrapper);
        return ResponseEntity.ok(ApiResponse.success("获取启用的AI模型列表成功", models));
    }
}
