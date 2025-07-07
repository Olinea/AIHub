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

/**
* @author Akaio
* @description 针对表【users】的数据库操作Service实现
* @createDate 2025-07-07 14:39:59
*/
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users>
    implements UsersService{

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = getBaseMapper().selectOne(new QueryWrapper<Users>().eq("username", username));
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPasswordHash(), new ArrayList<>());
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
}
