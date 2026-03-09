package ljc.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface TroopTreeNodeConfigMapper {

    @Select("SELECT * FROM troop_tree_node_config WHERE (#{civ} = 'ALL' OR civ = #{civ}) ORDER BY civ, tier, node_id")
    List<ljc.entity.TroopTreeNodeConfigTbl> selectByCiv(String civ);
    
    @Select("SELECT * FROM troop_tree_node_config WHERE node_id = #{nodeId}")
    ljc.entity.TroopTreeNodeConfigTbl selectById(Long nodeId);
}
