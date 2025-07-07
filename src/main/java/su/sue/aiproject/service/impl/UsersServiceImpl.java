package su.sue.aiproject.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import java.util.List;

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
        user.setIsAdmin(0); // 默认为普通用户
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        getBaseMapper().insert(user);
    }
    
    @Override
    public Users getUserByEmail(String email) {
        return getBaseMapper().selectOne(new QueryWrapper<Users>().eq("email", email));
    }
    
    // 管理员相关方法实现
    @Override
    public Page<Users> getAllUsers(int current, int size) {
        Page<Users> page = new Page<>(current, size);
        return getBaseMapper().selectPage(page, new QueryWrapper<Users>().orderByDesc("created_at"));
    }
    
    @Override
    public boolean updateUserAdmin(Long userId, Integer isAdmin) {
        try {
            Users user = getBaseMapper().selectById(userId);
            if (user != null) {
                user.setIsAdmin(isAdmin);
                user.setUpdatedAt(new Date());
                return getBaseMapper().updateById(user) > 0;
            }
            return false;
        } catch (Exception e) {
            logger.error("更新用户管理员状态失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean updateUserCredit(Long userId, BigDecimal creditBalance) {
        try {
            Users user = getBaseMapper().selectById(userId);
            if (user != null) {
                user.setCreditBalance(creditBalance);
                user.setUpdatedAt(new Date());
                return getBaseMapper().updateById(user) > 0;
            }
            return false;
        } catch (Exception e) {
            logger.error("更新用户积分失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean deleteUser(Long userId) {
        try {
            return getBaseMapper().deleteById(userId) > 0;
        } catch (Exception e) {
            logger.error("删除用户失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public List<Users> searchUsers(String keyword) {
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper
            .like("username", keyword)
            .or()
            .like("email", keyword)
        );
        return getBaseMapper().selectList(queryWrapper);
    }
}
