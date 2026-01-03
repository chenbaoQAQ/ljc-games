package ljc.repository;

import ljc.entity.UserStageProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserStageProgressRepository extends JpaRepository<UserStageProgress, Long> {
    Optional<UserStageProgress> findByUserIdAndStageIdAndDifficulty(Integer userId, Integer stageId, String difficulty);
}
