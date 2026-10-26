package rulai.dto;

import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import rulai.annotation.EnumValid;
import rulai.annotation.NotBlankIfPresent;
import rulai.constant.USER_CONSTANT;

import java.time.LocalDateTime;

@Data
public class UserDetailModifyDTO {
    /**
     * 邮箱格式验证
     */
    private String email;

    /**
     * 国际格式电话
     */
    private String phone;
    private Integer privilege;
    /**
     * 用户昵称
     */
    private String alias;


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
