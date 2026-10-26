package rulai.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;
/**
 * <p>
 * 存储坐席用户信息
 * </p>
 *
 * @author Mistry
 * @since 2025-12-29
 */
@Getter
@Setter
@ToString
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private String id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String cryptPassword;

    /**
     * 用户昵称
     */
    private String alias;

    /**
     * 用户头像URL
     */
    private String avatarUrl;

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
     * 状态开始时间
     */
    private LocalDateTime stateBeginAt;

    /**
     * 坐席性别
     */
    private String gender;

    /**
     * 坐席性别
     */
    private LocalDateTime birthday;

    /**
     * 逻辑删除标识（0-未删除，1-已删除）
     */
    @TableLogic
    private Integer isDeleted;
}
