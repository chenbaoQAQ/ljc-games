package ljc.repository;

import ljc.entity.UserStageProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserStageProgressRepository extends JpaRepository<UserStageProgress, Long> {
    /**
     * 查询特定玩家在特定关卡、特定难度下的通关记录
     */
    Optional<UserStageProgress> findByUserIdAndStageIdAndDifficulty(Integer userId, Integer stageId, String difficulty);
}