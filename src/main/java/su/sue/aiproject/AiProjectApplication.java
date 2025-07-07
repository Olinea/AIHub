package su.sue.aiproject;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("su.sue.aiproject.mapper")
public class AiProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiProjectApplication.class, args);
    }

}
