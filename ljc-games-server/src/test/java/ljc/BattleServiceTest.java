package ljc;

import ljc.repository.UnitConfigRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest // 必须加这个，才能让 Spring 启动并连接数据库
class BattleServiceTest {

    @Autowired
    private UnitConfigRepository unitConfigRepository;

    @Test
    void testDatabaseConnection() {
        // 使用 Java 17 最标准的写法，消灭红线
        java.util.Optional<ljc.entity.UnitConfig> configOptional = unitConfigRepository.findByUnitName("INFANTRY");

        if (configOptional.isPresent()) {
            ljc.entity.UnitConfig config = configOptional.get();
            System.out.println("--- 数据库连接成功 ---");
            System.out.println("当前兵种: " + config.getUnitName());
            System.out.println("基础攻击力: " + config.getBaseAtk());
        } else {
            System.out.println("警告：数据库连上了，但没找到 INFANTRY 的数据，请检查 data.sql 是否执行成功。");
        }
    }
}