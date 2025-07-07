package su.sue.aiproject.service;

import su.sue.aiproject.domain.AiModels;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Akaio
* @description 针对表【ai_models】的数据库操作Service
* @createDate 2025-07-07 14:39:26
*/
public interface AiModelsService extends IService<AiModels> {

    /**
     * 获取模型信息（仅供内部服务调用）
     * @param id 模型ID
     * @return 模型信息
     */
    AiModels getModelWithDecryptedKeys(Integer id);

}
