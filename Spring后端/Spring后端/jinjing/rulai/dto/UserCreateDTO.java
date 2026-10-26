package rulai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;
import rulai.annotation.EnumValid;
import rulai.constant.USER_CONSTANT;

import java.time.LocalDateTime;

@Data
public class UserCreateDTO {

    /**
     * 唯一登录名
     */
    @NotBlank(message = "用户名不能为空")

    private String username;

    /**
     * 原始
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 邮箱格式验证
     */
    @NotBlank(message = "邮箱不能为空")
    private String email;

    /**
     * 国际格式电话
     */
    @NotBlank(message = "电话不能为空")
    private String phone;


    /**
     * 用户昵称
     */
    @NotBlank(message = "用户需设置初始昵称")
    private String alias;

    /**
     * 头像CDN地址
     */
    private String avatarUrl;

    /**
     * 坐席性别
     */
    @EnumValid(target = USER_CONSTANT.STATUS.class)
    private String gender;

    /**
     * 坐席生日
     */
    @PastOrPresent(message = "生日时刻必须早于当前时刻")
    private LocalDateTime birthday;
}
