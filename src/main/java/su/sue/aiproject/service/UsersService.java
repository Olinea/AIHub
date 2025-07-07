package su.sue.aiproject.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.security.core.userdetails.UserDetailsService;
import su.sue.aiproject.domain.RegisterRequest;
import su.sue.aiproject.domain.Users;

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
}
