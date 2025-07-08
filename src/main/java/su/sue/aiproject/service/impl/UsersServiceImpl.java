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
import su.sue.aiproject.domain.UsersSummaryResponse;
import su.sue.aiproject.mapper.UsersMapper;
import su.sue.aiproject.service.UsersService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
        
        logger.info("找到用户: {} (ID: {}), 邮箱: {}, 是否管理员: {}", user.getUsername(), user.getId(), user.getEmail(), user.getIsAdmin());
        logger.info("数据库中存储的密码哈希: {}", user.getPasswordHash() != null ? user.getPasswordHash().substring(0, 20) + "..." : "null");
        
        // 创建用户权限列表
        List<org.springframework.security.core.GrantedAuthority> authorities = new ArrayList<>();
        if (user.getIsAdmin() != null && user.getIsAdmin() == 1) {
            authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"));
        } else {
            authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"));
        }
        
        // 使用邮箱作为用户名返回，这样与前端传递的邮箱保持一致
        return new org.springframework.security.core.userdetails.User(
            user.getEmail() != null ? user.getEmail() : user.getUsername(), 
            user.getPasswordHash(), 
            authorities
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
    
    @Override
    public UsersSummaryResponse getUsersSummary() {
        try {
            UsersSummaryResponse summary = new UsersSummaryResponse();
            
            // 获取用户总数
            Long total = getBaseMapper().selectCount(null);
            summary.setTotal(total);
            
            // 计算日期范围
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime dayStart = now.truncatedTo(ChronoUnit.DAYS);
            LocalDateTime weekStart = now.minusDays(7);
            LocalDateTime monthStart = now.minusDays(30);
            
            // 新增用户统计
            UsersSummaryResponse.NewUsers newUsers = new UsersSummaryResponse.NewUsers();
            
            QueryWrapper<Users> dailyWrapper = new QueryWrapper<>();
            dailyWrapper.ge("created_at", dayStart);
            Long dailyNew = getBaseMapper().selectCount(dailyWrapper);
            newUsers.setDaily(dailyNew);
            
            QueryWrapper<Users> weeklyWrapper = new QueryWrapper<>();
            weeklyWrapper.ge("created_at", weekStart);
            Long weeklyNew = getBaseMapper().selectCount(weeklyWrapper);
            newUsers.setWeekly(weeklyNew);
            
            QueryWrapper<Users> monthlyWrapper = new QueryWrapper<>();
            monthlyWrapper.ge("created_at", monthStart);
            Long monthlyNew = getBaseMapper().selectCount(monthlyWrapper);
            newUsers.setMonthly(monthlyNew);
            
            summary.setNewUsers(newUsers);
            
            // 活跃用户统计（暂时使用固定值，可以根据实际业务逻辑调整）
            UsersSummaryResponse.ActiveUsers activeUsers = new UsersSummaryResponse.ActiveUsers();
            // 这里可以根据登录记录表或其他活跃指标计算，暂时使用示例数据
            activeUsers.setDau(Math.min(total, 1200L));
            activeUsers.setWau(Math.min(total, 4500L)); 
            activeUsers.setMau(Math.min(total, 8100L));
            
            summary.setActiveUsers(activeUsers);
            
            return summary;
        } catch (Exception e) {
            logger.error("获取用户统计失败: {}", e.getMessage(), e);
            // 返回默认值
            UsersSummaryResponse summary = new UsersSummaryResponse();
            summary.setTotal(0L);
            
            UsersSummaryResponse.NewUsers newUsers = new UsersSummaryResponse.NewUsers();
            newUsers.setDaily(0L);
            newUsers.setWeekly(0L);
            newUsers.setMonthly(0L);
            summary.setNewUsers(newUsers);
            
            UsersSummaryResponse.ActiveUsers activeUsers = new UsersSummaryResponse.ActiveUsers();
            activeUsers.setDau(0L);
            activeUsers.setWau(0L);
            activeUsers.setMau(0L);
            summary.setActiveUsers(activeUsers);
            
            return summary;
        }
    }
}
