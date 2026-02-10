package ljc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("ljc.mapper")
public class LjcGamesApplication {

    public static void main(String[] args) {
        System.out.println("====== LJC GAMES SERVER STARTING (VERSION CHECK) ======");
        SpringApplication.run(LjcGamesApplication.class, args);
    }

}
