package ljc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BattleLog {
    private Integer turn;       // 第几回合 (0表示战前/战后)
    private String content;     // 战报文本内容

    //TODO
    // 以后扩展：前端可能需要特效ID，如下
    // private String effectId; // 比如 "ATK_ANIMATION"
}