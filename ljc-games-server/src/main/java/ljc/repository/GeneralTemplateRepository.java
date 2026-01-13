package ljc.repository;

import ljc.entity.GeneralTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GeneralTemplateRepository extends JpaRepository<GeneralTemplate, Integer> {
}