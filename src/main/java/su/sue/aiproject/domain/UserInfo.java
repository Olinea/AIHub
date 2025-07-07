package su.sue.aiproject.domain;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户信息响应")
public class UserInfo {
    
    @Schema(description = "用户ID")
    private Long id;
    
    @Schema(description = "用户名")
    private String username;
    
    @Schema(description = "邮箱")
    private String email;
    
    @Schema(description = "积分余额")
    private BigDecimal creditBalance;
    
    @Schema(description = "创建时间")
    private Date createdAt;
    
    @Schema(description = "更新时间")
    private Date updatedAt;
    
    // 从Users对象创建UserInfo的静态方法
    public static UserInfo fromUsers(Users user) {
        if (user == null) {
            return null;
        }
        return new UserInfo(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getCreditBalance(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
