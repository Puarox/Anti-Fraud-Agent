package rulai.dto;

import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import rulai.annotation.EnumValid;
import rulai.constant.USER_CONSTANT;

import java.time.LocalDateTime;


/**
 * @author 22076
 */
@Data
public class UserQueryDTO extends PageQueryParam  {
    /**
     * 用户ID
     */
    private String id;

    /**
     * 用户名
     */
    private String username;


    /**
     * 用户昵称
     */
    private String alias;



    /**
     * 角色权限编码
     */
    private Integer privilege;

    /**
     * 状态
     */
    private String status;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 坐席性别
     */
    private String gender;

    /**
     * 坐席生日
     */
    private LocalDateTime birthday;
}
