package su.sue.aiproject.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import su.sue.aiproject.domain.Users;
import su.sue.aiproject.mapper.UsersMapper;
import su.sue.aiproject.service.UsersService;

import java.util.ArrayList;

/**
* @author Akaio
* @description 针对表【users】的���据库操作Service实现
* @createDate 2025-07-07 14:39:59
*/
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users>
    implements UsersService{

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = getBaseMapper().selectOne(new QueryWrapper<Users>().eq("username", username));
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPasswordHash(), new ArrayList<>());
    }
}





