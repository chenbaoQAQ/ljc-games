package ljc.mapper;

import ljc.entity.UserTbl;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    /**
     * 新增用户 (注册用)
     * keyProperty="id": 告诉 MyBatis 把数据库生成的自增 ID 回填给 UserTbl 对象的 id 属性
     */
    int insert(UserTbl user);

    /**
     * 根据用户名查询 (登录校验用)
     */
    UserTbl selectByUsername(@Param("username") String username);

    /**
     * 根据 ID 查询 (初始化数据用)
     */
    UserTbl selectById(@Param("id") Long id);
}