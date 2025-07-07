package su.sue.aiproject.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import su.sue.aiproject.domain.AiModels;
import su.sue.aiproject.service.AiModelsService;
import su.sue.aiproject.mapper.AiModelsMapper;
import org.springframework.stereotype.Service;

/**
* @author Akaio
* @description 针对表【ai_models】的数据库操作Service实现
* @createDate 2025-07-07 14:39:26
*/
@Service
public class AiModelsServiceImpl extends ServiceImpl<AiModelsMapper, AiModels>
    implements AiModelsService{

    @Override
    public AiModels getModelWithDecryptedKeys(Integer id) {
        // 直接返回模型信息，无需解密
        return this.getById(id);
    }

}




