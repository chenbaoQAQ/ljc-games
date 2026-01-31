package ljc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StageConfigRepository extends JpaRepository<StageConfig, Integer> {
    Optional<StageConfig> findByStageName(String stageName);
}
