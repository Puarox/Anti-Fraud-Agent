package rulai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;
import rulai.annotation.EnumValid;
import rulai.constant.USER_CONSTANT;

import java.time.LocalDateTime;

@Data
public class UserModifyDTO {


    private String username;





    private String email;


    private String phone;




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
