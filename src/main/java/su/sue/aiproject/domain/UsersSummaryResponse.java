package su.sue.aiproject.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户统计响应")
public class UsersSummaryResponse {
    
    @Schema(description = "用户总数")
    private Long total;
    
    @Schema(description = "新增用户统计")
    private NewUsers newUsers;
    
    @Schema(description = "活跃用户统计")
    private ActiveUsers activeUsers;
    
    @Data
    @Schema(description = "新增用户统计")
    public static class NewUsers {
        @Schema(description = "日新增")
        private Long daily;
        
        @Schema(description = "周新增")
        private Long weekly;
        
        @Schema(description = "月新增")
        private Long monthly;
    }
    
    @Data
    @Schema(description = "活跃用户统计")
    public static class ActiveUsers {
        @Schema(description = "日活跃用户")
        private Long dau;
        
        @Schema(description = "周活跃用户")
        private Long wau;
        
        @Schema(description = "月活跃用户")
        private Long mau;
    }
}
