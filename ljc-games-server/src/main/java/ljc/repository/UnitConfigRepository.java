package ljc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository: 仓库。
 * 继承 JpaRepository 后，Spring 会自动帮你实现增删改查。
 */
@Repository
public interface UnitConfigRepository extends JpaRepository<UnitConfig, Integer> {

    // 这是一个神奇的方法：只要按规范命名，Spring 就会自动生成 SQL
    //Optional->"可选的"；用了它，我们就可以优雅地处理“找不到”的情况。
    Optional<UnitConfig> findByUnitName(String unitName);
}