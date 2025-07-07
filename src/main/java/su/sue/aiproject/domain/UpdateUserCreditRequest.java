package su.sue.aiproject.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "更新用户积分请求")
public class UpdateUserCreditRequest {
    
    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", required = true)
    private Long userId;
    
    @NotNull(message = "积分余额不能为空")
    @DecimalMin(value = "0.0", message = "积分余额不能为负数")
    @Schema(description = "积分余额", required = true)
    private BigDecimal creditBalance;
}
