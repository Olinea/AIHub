package su.sue.aiproject.mapper;

import su.sue.aiproject.domain.Conversations;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;

/**
* @author Akaio
* @description 针对表【conversations】的数据库操作Mapper
* @createDate 2025-07-07 14:39:48
* @Entity su.sue.aiproject.domain.Conversations
*/
public interface ConversationsMapper extends BaseMapper<Conversations> {

    /**
     * 更新会话的模型ID
     */
    @Update("UPDATE conversations SET model_id = #{modelId} WHERE id = #{conversationId}")
    int updateModelId(@Param("conversationId") Long conversationId, @Param("modelId") Integer modelId);

}




