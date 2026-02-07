package ljc;

import ljc.entity.UserTbl;
import ljc.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LjcGamesApplicationTests {

    // 自动注入我们要测的 Mapper
    @Autowired
    private UserMapper userMapper;

    @Test
    void testUserInsertAndSelect() {
        System.out.println("====== 点火测试开始 ======");

        // 1. 准备一个假用户
        UserTbl newUser = new UserTbl();
        newUser.setUsername("caocao_test");
        newUser.setNickname("曹孟德");
        newUser.setPasswordHash("123456"); // 暂时明文，以后再加密
        newUser.setInitialCiv("CN");
        newUser.setGold(1000L);
        newUser.setDiamond(100L);


        // 2. 插入数据库
        System.out.println("正在插入用户...");
        int rows = userMapper.insert(newUser);
        System.out.println("插入受影响行数: " + rows);

        // 验证 ID 是否回填 (MyBatis 的神奇之处)
        System.out.println("新用户的 ID 是: " + newUser.getId());

        // 3. 查出来看看
        System.out.println("正在回查数据...");
        UserTbl dbUser = userMapper.selectById(newUser.getId());

        System.out.println("查到的用户名: " + dbUser.getNickname());
        System.out.println("查到的金币: " + dbUser.getGold());

        System.out.println("====== 点火测试成功！ ======");
    }
}