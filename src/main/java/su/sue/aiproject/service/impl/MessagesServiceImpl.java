package su.sue.aiproject.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import su.sue.aiproject.domain.Messages;
import su.sue.aiproject.service.MessagesService;
import su.sue.aiproject.mapper.MessagesMapper;
import org.springframework.stereotype.Service;

/**
* @author Akaio
* @description 针对表【messages】的数据库操作Service实现
* @createDate 2025-07-07 14:39:51
*/
@Service
public class MessagesServiceImpl extends ServiceImpl<MessagesMapper, Messages>
    implements MessagesService{

}




