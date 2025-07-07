package su.sue.aiproject.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import su.sue.aiproject.domain.RegisterRequest;
import su.sue.aiproject.domain.Users;
import su.sue.aiproject.mapper.UsersMapper;
import su.sue.aiproject.service.UsersService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

// 添加日志依赖
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* @author Akaio
* @description 针对表【users】的数据库操作Service实现
* @createDate 2025-07-07 14:39:59
*/
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users>
    implements UsersService{

    private static final Logger logger = LoggerFactory.getLogger(UsersServiceImpl.class);

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String emailOrUsername) throws UsernameNotFoundException {
        logger.info("尝试加载用户: {}", emailOrUsername);
        
        // 先尝试按邮箱查找，再按用户名查找
        Users user = getBaseMapper().selectOne(new QueryWrapper<Users>().eq("email", emailOrUsername));
        if (user == null) {
            logger.info("按邮箱未找到用户，尝试按用户名查找");
            user = getBaseMapper().selectOne(new QueryWrapper<Users>().eq("username", emailOrUsername));
        }
        
        if (user == null) {
            logger.error("用户不存在: {}", emailOrUsername);
            throw new UsernameNotFoundException("User not found with email or username: " + emailOrUsername);
        }
        
        logger.info("找到用户: {} (ID: {}), 邮箱: {}", user.getUsername(), user.getId(), user.getEmail());
        logger.info("数据库中存储的密码哈希: {}", user.getPasswordHash() != null ? user.getPasswordHash().substring(0, 20) + "..." : "null");
        
        // 使用邮箱作为用户名返回，这样与前端传递的邮箱保持一致
        return new org.springframework.security.core.userdetails.User(
            user.getEmail() != null ? user.getEmail() : user.getUsername(), 
            user.getPasswordHash(), 
            new ArrayList<>()
        );
    }

    @Override
    public boolean existsByUsername(String username) {
        return getBaseMapper().selectCount(new QueryWrapper<Users>().eq("username", username)) > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        return getBaseMapper().selectCount(new QueryWrapper<Users>().eq("email", email)) > 0;
    }

    @Override
    public void registerUser(RegisterRequest registerRequest) {
        Users user = new Users();
        user.setUsername(registerRequest.getUsername());
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setCreditBalance(new BigDecimal("10.00")); // 赠送10积分
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        getBaseMapper().insert(user);
    }
    
    @Override
    public Users getUserByEmail(String email) {
        return getBaseMapper().selectOne(new QueryWrapper<Users>().eq("email", email));
    }
}
