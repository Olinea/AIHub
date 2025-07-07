package su.sue.aiproject.domain;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "JWT认证响应")
public class JwtAuthenticationResponse {
    
    @Schema(description = "访问令牌")
    private String accessToken;
    
    @Schema(description = "令牌类型", example = "Bearer")
    private String tokenType = "Bearer";
    
    @Schema(description = "令牌过期时间(秒)", example = "86400")
    private Long expiresIn;
    
    @Schema(description = "用户邮箱")
    private String email;
    
    @Schema(description = "用户信息")
    private UserInfo user;
    
    public JwtAuthenticationResponse(String accessToken) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
    }
    
    public JwtAuthenticationResponse(String accessToken, Long expiresIn, String email) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
        this.expiresIn = expiresIn;
        this.email = email;
    }
    
    public JwtAuthenticationResponse(String accessToken, Long expiresIn, String email, UserInfo user) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
        this.expiresIn = expiresIn;
        this.email = email;
        this.user = user;
    }
}
