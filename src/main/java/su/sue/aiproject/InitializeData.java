package su.sue.aiproject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import su.sue.aiproject.domain.AiModels;
import su.sue.aiproject.service.AiModelsService;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class InitializeData implements CommandLineRunner {

    @Autowired
    private AiModelsService aiModelsService;

    @Override
    public void run(String... args) throws Exception {
        // 检查是否已经存在 deepseek-chat 模型
        if (aiModelsService.getModelByProviderAndName("deepseek", "deepseek-chat") == null) {
            // 创建 DeepSeek 模型
            AiModels deepSeekModel = new AiModels();
            deepSeekModel.setModelName("deepseek-chat");
            deepSeekModel.setProvider("deepseek");
            deepSeekModel.setApiEndpoint("https://api.deepseek.com/chat/completions");
            deepSeekModel.setApiKey("sk-8db834dd2164453388e989baca30c453");
            deepSeekModel.setCostPer1kTokens(new BigDecimal("0.014"));
            deepSeekModel.setRateLimitPerMinute(60);
            deepSeekModel.setIsEnabled(true);
            deepSeekModel.setCreatedAt(LocalDateTime.now());
            deepSeekModel.setUpdatedAt(LocalDateTime.now());

            aiModelsService.save(deepSeekModel);
            System.out.println("✅ DeepSeek 模型已成功添加到数据库");
        } else {
            System.out.println("ℹ️ DeepSeek 模型已存在，跳过初始化");
        }
    }
}
