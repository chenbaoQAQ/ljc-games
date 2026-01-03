package ljc.entity;

import lombok.Data;
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "user_stage_progress")
public class UserStageProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer userId;
    private Integer stageId;
    private String difficulty; // NORMAL, HARD, NIGHTMARE

    private boolean firstCleared = false; // 是否已领取该难度首通
}
