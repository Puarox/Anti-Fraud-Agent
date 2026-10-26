package rulai.service;

import rulai.dto.UserCreateDTO;
import rulai.dto.UserDetailModifyDTO;
import rulai.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import rulai.vo.UserLoginVO;
import rulai.vo.UserVO;

/**
 * <p>
 * 存储坐席用户信息 服务类
 * </p>
 *
 * @author Mistry
 * @since 2025-12-26
 */
public interface IUserService extends IService<User> {

    UserLoginVO login(String username, String password);

    void logout(String token);



    void register(UserCreateDTO userDTO);

    UserVO getUserDetail(String token);

    boolean isExited(String username);

    void changePassword(String oldPassword, String newPassword, String token);

    User getUserById(String id);

    User getUserByUsername(String repairUsername);

    void updateUserMessage(UserDetailModifyDTO userDTO, String token);

    /**
     * 更新用户状态
     * @param status
     */
    void updateStatus(String status,String token);
}
