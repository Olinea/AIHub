package su.sue.aiproject.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * WebClient配置类
 */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        // 配置HTTP客户端 - 增加超时时间以支持流式响应
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(120)) // 增加到120秒以支持长时间的流式响应
                .followRedirect(true);
        
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> {
                    configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024); // 10MB
                    // 设置默认字符编码为 UTF-8
                    configurer.defaultCodecs().enableLoggingRequestDetails(true);
                });
    }
    
    @Bean
    public WebClient webClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder.build();
    }
}
