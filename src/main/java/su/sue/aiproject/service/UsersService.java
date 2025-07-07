package su.sue.aiproject.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.security.core.userdetails.UserDetailsService;
import su.sue.aiproject.domain.RegisterRequest;
import su.sue.aiproject.domain.Users;

import java.math.BigDecimal;
import java.util.List;

/**
* @author Akaio
* @description 针对表【users】的数据库操作Service
* @createDate 2025-07-07 14:39:59
*/
public interface UsersService extends IService<Users>, UserDetailsService {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    void registerUser(RegisterRequest registerRequest);
    
    Users getUserByEmail(String email);
    
    // 管理员相关方法
    Page<Users> getAllUsers(int current, int size);
    
    boolean updateUserAdmin(Long userId, Integer isAdmin);
    
    boolean updateUserCredit(Long userId, BigDecimal creditBalance);
    
    boolean deleteUser(Long userId);
    
    List<Users> searchUsers(String keyword);
}
