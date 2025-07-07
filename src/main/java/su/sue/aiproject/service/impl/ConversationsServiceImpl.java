package su.sue.aiproject.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import su.sue.aiproject.domain.Conversations;
import su.sue.aiproject.service.ConversationsService;
import su.sue.aiproject.mapper.ConversationsMapper;
import org.springframework.stereotype.Service;

/**
* @author Akaio
* @description 针对表【conversations】的数据库操作Service实现
* @createDate 2025-07-07 14:39:48
*/
@Service
public class ConversationsServiceImpl extends ServiceImpl<ConversationsMapper, Conversations>
    implements ConversationsService{

}




