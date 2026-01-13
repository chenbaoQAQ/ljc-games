package ljc.repository;

import ljc.entity.UserGeneral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserGeneralRepository extends JpaRepository<UserGeneral, Integer> {
}