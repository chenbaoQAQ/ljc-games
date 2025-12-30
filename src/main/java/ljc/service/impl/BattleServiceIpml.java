package ljc.service.impl;

import ljc.entity.General;
import ljc.service.BattleService;
import org.springframework.stereotype.Service;

@Service
public class BattleServiceIpml implements BattleService {

    @Override
    public double processBattleEffect(General general) {
        // process--处理,这是处理战斗的类

        // 1. 获取当前血量和最大血量
        // Integer 是包装类，虽然能直接算，但作为严谨的后端，我们要习惯用 double 转换
        double currentHp = general.getCurrentHp();
        double maxHp = general.getMaxHp();

        // 2. 计算血量比例 (Ratio)
        // 英语小课：Ratio / Percentage (比例 / 百分比)
        double hpRatio = currentHp / maxHp;
        System.out.println("当前武将：" + general.getName() + " 的血量比例为：" + hpRatio);
        return 1.0;
    }
}
