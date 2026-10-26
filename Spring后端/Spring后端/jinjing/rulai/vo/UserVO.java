package rulai.vo;


import lombok.Data;

import java.time.LocalDateTime;
@Data
public class UserVO {
    private String id;
    /**
     * 唯一登录名
     */
    private String username;
    /**
     * 邮箱格式验证
     */
    private String email;

    /**
     * 国际格式电话
     */
    private String phone;
    /**
     * 用户昵称
     */
    private String alias;

    /**
     * 头像CDN地址
     */
    private String avatarUrl;
    /**
     * 状态
     */
    private String status;
    /**
     * 权限位掩码
     */
    private Integer privilege;
    /**
     * 坐席性别
     */
    private String gender;
    /**
     * 坐席生日
     */
    private LocalDateTime birthday;
}
