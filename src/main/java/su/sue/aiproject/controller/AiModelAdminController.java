package su.sue.aiproject.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import su.sue.aiproject.domain.*;
import su.sue.aiproject.service.AiModelsService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/ai-models")
@RequiredArgsConstructor
@Tag(name = "AI模型管理", description = "AI模型管理相关接口 - 仅管理员可访问")
@PreAuthorize("hasRole('ADMIN')")
public class AiModelAdminController {

    private final AiModelsService aiModelsService;

    @PostMapping
    @Operation(summary = "添加AI模型", description = "添加新的AI模型")
    public ResponseEntity<ApiResponse<AiModels>> addAiModel(@Validated @RequestBody AddAiModelRequest request) {
        // 检查模型名称是否已存在
        QueryWrapper<AiModels> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("model_name", request.getModelName())
                   .eq("provider", request.getProvider());
        
        if (aiModelsService.count(queryWrapper) > 0) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("该提供商下的模型名称已存在"));
        }

        // 创建新模型
        AiModels aiModel = new AiModels();
        BeanUtils.copyProperties(request, aiModel);
        
        // 设置创建时间
        aiModel.setCreatedAt(LocalDateTime.now());
        aiModel.setUpdatedAt(LocalDateTime.now());
        
        boolean saved = aiModelsService.save(aiModel);
        if (saved) {
            return ResponseEntity.ok(ApiResponse.success("AI模型添加成功", aiModel));
        } else {
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("AI模型添加失败"));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新AI模型", description = "更新指定ID的AI模型信息")
    public ResponseEntity<ApiResponse<AiModels>> updateAiModel(
            @Parameter(description = "模型ID") @PathVariable Integer id,
            @Validated @RequestBody UpdateAiModelRequest request) {
        
        // 检查模型是否存在
        AiModels existingModel = aiModelsService.getById(id);
        if (existingModel == null) {
            return ResponseEntity.notFound().build();
        }

        // 检查模型名称是否与其他模型重复（排除当前模型）
        QueryWrapper<AiModels> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("model_name", request.getModelName())
                   .eq("provider", request.getProvider())
                   .ne("id", id);
        
        if (aiModelsService.count(queryWrapper) > 0) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("该提供商下的模型名称已存在"));
        }

        // 更新模型信息
        existingModel.setModelName(request.getModelName());
        existingModel.setProvider(request.getProvider());
        existingModel.setApiEndpoint(request.getApiEndpoint());
        existingModel.setOrganizationId(request.getOrganizationId());
        existingModel.setProjectId(request.getProjectId());
        existingModel.setExtraHeaders(request.getExtraHeaders());
        existingModel.setCostPer1kTokens(request.getCostPer1kTokens());
        existingModel.setRateLimitPerMinute(request.getRateLimitPerMinute());
        existingModel.setIsEnabled(request.getIsEnabled());
        existingModel.setUpdatedAt(LocalDateTime.now());
        
        // 只在提供新密钥时才更新
        if (StringUtils.hasText(request.getApiKey())) {
            existingModel.setApiKey(request.getApiKey());
        }
        if (StringUtils.hasText(request.getApiSecret())) {
            existingModel.setApiSecret(request.getApiSecret());
        }
        
        boolean updated = aiModelsService.updateById(existingModel);
        if (updated) {
            return ResponseEntity.ok(ApiResponse.success("AI模型更新成功", existingModel));
        } else {
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("AI模型更新失败"));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除AI模型", description = "删除指定ID的AI模型")
    public ResponseEntity<ApiResponse<Void>> deleteAiModel(
            @Parameter(description = "模型ID") @PathVariable Integer id) {
        
        // 检查模型是否存在
        AiModels existingModel = aiModelsService.getById(id);
        if (existingModel == null) {
            return ResponseEntity.notFound().build();
        }

        boolean deleted = aiModelsService.removeById(id);
        if (deleted) {
            return ResponseEntity.ok(ApiResponse.<Void>success("AI模型删除成功", null));
        } else {
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("AI模型删除失败"));
        }
    }

    @GetMapping
    @Operation(summary = "获取AI模型列表", description = "分页获取AI模型列表")
    public ResponseEntity<ApiResponse<Page<AiModels>>> getAiModels(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "模型名称") @RequestParam(required = false) String modelName,
            @Parameter(description = "提供商") @RequestParam(required = false) String provider,
            @Parameter(description = "是否启用") @RequestParam(required = false) Boolean isEnabled) {
        
        Page<AiModels> page = new Page<>(current, size);
        QueryWrapper<AiModels> queryWrapper = new QueryWrapper<>();
        
        if (modelName != null && !modelName.trim().isEmpty()) {
            queryWrapper.like("model_name", modelName);
        }
        if (provider != null && !provider.trim().isEmpty()) {
            queryWrapper.like("provider", provider);
        }
        if (isEnabled != null) {
            queryWrapper.eq("is_enabled", isEnabled);
        }
        
        queryWrapper.orderByDesc("id");
        
        Page<AiModels> result = aiModelsService.page(page, queryWrapper);
        return ResponseEntity.ok(ApiResponse.success("获取AI模型列表成功", result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取AI模型详情", description = "根据ID获取AI模型详情")
    public ResponseEntity<ApiResponse<AiModels>> getAiModel(
            @Parameter(description = "模型ID") @PathVariable Integer id) {
        
        AiModels aiModel = aiModelsService.getById(id);
        if (aiModel == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(ApiResponse.success("获取AI模型详情成功", aiModel));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "切换AI模型状态", description = "启用或禁用AI模型")
    public ResponseEntity<ApiResponse<AiModels>> toggleAiModelStatus(
            @Parameter(description = "模型ID") @PathVariable Integer id) {
        
        AiModels aiModel = aiModelsService.getById(id);
        if (aiModel == null) {
            return ResponseEntity.notFound().build();
        }
        
        aiModel.setIsEnabled(!aiModel.getIsEnabled());
        boolean updated = aiModelsService.updateById(aiModel);
        
        if (updated) {
            String status = aiModel.getIsEnabled() ? "启用" : "禁用";
            return ResponseEntity.ok(ApiResponse.success("AI模型" + status + "成功", aiModel));
        } else {
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("AI模型状态切换失败"));
        }
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有AI模型", description = "获取所有AI模型（不分页）")
    public ResponseEntity<ApiResponse<List<AiModels>>> getAllAiModels() {
        List<AiModels> models = aiModelsService.list();
        return ResponseEntity.ok(ApiResponse.success("获取所有AI模型成功", models));
    }
}
