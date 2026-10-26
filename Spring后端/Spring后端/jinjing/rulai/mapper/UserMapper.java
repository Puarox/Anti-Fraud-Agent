package rulai.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import rulai.entity.User;
import rulai.vo.table.UserTableVO;

import java.util.List;

/**
 * <p>
 * 存储坐席用户信息 Mapper 接口
 * </p>
 *
 * @author Mistry
 * @since 2025-12-29
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    /**
     * 分页查询用户列表
     */
    IPage<UserTableVO> selectUserPage(
            IPage<UserTableVO> page,
            @Param("ew") Wrapper<UserTableVO> wrapper
    );

    /**
     * 查询用户列表（不分页）
     */
    List<UserTableVO> selectUserList(
            @Param("ew") Wrapper<UserTableVO> wrapper
    );
}

