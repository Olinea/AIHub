package su.sue.aiproject.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "更新用户管理员状态请求")
public class UpdateUserAdminRequest {
    
    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", required = true)
    private Long userId;
    
    @NotNull(message = "管理员状态不能为空")
    @Schema(description = "是否为管理员 (0: 普通用户, 1: 管理员)", required = true)
    private Integer isAdmin;
}
