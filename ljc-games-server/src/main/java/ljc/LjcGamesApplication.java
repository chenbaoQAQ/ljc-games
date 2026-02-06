package ljc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("ljc.mapper")
public class LjcGamesApplication {

    public static void main(String[] args) {
        SpringApplication.run(LjcGamesApplication.class, args);
    }

}
