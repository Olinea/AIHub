package su.sue.aiproject.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import su.sue.aiproject.domain.Users;
import su.sue.aiproject.service.UsersService;
import su.sue.aiproject.mapper.UsersMapper;
import org.springframework.stereotype.Service;

/**
* @author Akaio
* @description 针对表【users】的数据库操作Service实现
* @createDate 2025-07-07 14:39:59
*/
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users>
    implements UsersService{

}




