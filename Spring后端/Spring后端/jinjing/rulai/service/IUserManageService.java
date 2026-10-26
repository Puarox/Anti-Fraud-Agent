package rulai.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import rulai.dto.UserCreateDTO;
import rulai.dto.UserModifyDTO;
import rulai.dto.UserQueryDTO;
import rulai.entity.User;
import rulai.vo.UserVO;
import rulai.vo.table.UserTableVO;

import java.util.List;

@Service
public interface IUserManageService {
    /**
     * 创建用户（管理端）
     * @param createDTO
     * @return
     */
    User createUser(UserCreateDTO createDTO);

    /**
     * 根据ID获取用户信息
     * @param userId
     * @return
     */
    UserVO getById(String userId);

    /**
     * 修改
     * @param userId
     * @param dto
     */
    void updateUser(String userId, UserModifyDTO dto);

    /**
     * page
     * @param dto
     * @return
     */
    IPage<UserTableVO> pageUsers(UserQueryDTO dto);

    /**
     * list
     * @param dto
     * @return
     */
    List<UserTableVO> listCosts(UserQueryDTO dto);

    /**
     * 删除用户
     * @param userId
     */
    void deleteUser(String userId);
}
