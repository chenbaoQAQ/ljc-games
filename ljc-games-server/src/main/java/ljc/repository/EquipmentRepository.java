package ljc.repository;

import ljc.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Integer> {
    // 关键方法：通过武将ID查询他身上穿的所有装备
    List<Equipment> findByOwnerGeneralId(Integer ownerGeneralId);
}