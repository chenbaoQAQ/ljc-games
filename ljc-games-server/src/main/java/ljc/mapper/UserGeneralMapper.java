package ljc.mapper;
import ljc.entity.UserGeneralTbl;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserGeneralMapper {
    // 注意：这个 insert 需要返回自增 ID，XML 里要配置
    int insert(UserGeneralTbl entity);
}