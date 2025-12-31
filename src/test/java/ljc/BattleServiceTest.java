package ljc;

import ljc.entity.UnitConfig;
import ljc.repository.UnitConfigRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

@SpringBootTest // 必须加这个，才能让 Spring 启动并连接数据库
class BattleServiceTest {

    @Autowired
    private UnitConfigRepository unitConfigRepository;

    @Test
    void testDatabaseConnection() {
        // 使用标准的 Optional 处理方式
        Optional<UnitConfig> configOptional = unitConfigRepository.findByUnitName("INFANTRY");

        if (configOptional.isPresent()) {
            UnitConfig config = configOptional.get();
            System.out.println("--- 数据库连接成功 ---");
            System.out.println("兵种名称: " + config.getUnitName());
            System.out.println("基础攻击力: " + config.getBaseAtk());
        } else {
            System.out.println("--- 数据库连接成功，但未找到名为 INFANTRY 的数据 ---");
        }
    }
}