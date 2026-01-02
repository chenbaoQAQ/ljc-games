package ljc.entity;

import lombok.Data;
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "user_general")
public class UserGeneral {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer userId;
    private Integer templateId; // 关联武将模版（如：赵云、关羽）

    private String name;        // 武将姓名
    private String personality; // 性格：BRAVE, CAUTIOUS, RASH, CALM

    private int maxHp = 1000;      // 最大血量
    private int currentHp = 1000;  // 当前血量
    private int currentArmyCount;  // 当前兵力人数
    private String status = "HEALTHY"; // 状态：HEALTHY, WOUNDED, KILLED
    // UserGeneral.java
    private int level = 1;        // 武将等级，默认1级
    private int currentExp = 0;   // 当前经验值

    // 2. Getter 和 Setter 方法（这是 IDEA 识别 getCurrentHp 等指令的关键）

    // 获取和设置血量
    public int getCurrentHp() { return currentHp; }
    public void setCurrentHp(int currentHp) { this.currentHp = currentHp; }

    public int getMaxHp() { return maxHp; }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }

    // 获取和设置兵力人数
    public int getCurrentArmyCount() { return currentArmyCount; }
    public void setCurrentArmyCount(int currentArmyCount) { this.currentArmyCount = currentArmyCount; }

    // 获取和设置状态
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public int getCurrentExp() { return currentExp; }
    public void setCurrentExp(int currentExp) { this.currentExp = currentExp; }

}