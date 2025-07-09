package su.sue.aiproject.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import su.sue.aiproject.domain.Users;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;

/**
 * 用户主体信息，实现Spring Security的UserDetails接口
 */
@Data
@AllArgsConstructor
public class UserPrincipal implements UserDetails {
    
    private Long id;
    private String username;
    private String email;
    private String password;
    private BigDecimal creditBalance;
    private Integer isAdmin;
    
    public static UserPrincipal create(Users user) {
        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getCreditBalance(),
                user.getIsAdmin()
        );
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (isAdmin != null && isAdmin == 1) {
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public String getUsername() {
        return email; // 使用邮箱作为用户名
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
}
