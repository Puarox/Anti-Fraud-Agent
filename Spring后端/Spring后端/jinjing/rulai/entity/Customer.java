package rulai.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;
/**
 * <p>
 * 存储电信客户信息
 * </p>
 *
 * @author Mistry
 * @since 2025-12-29
 */
@Getter
@Setter
@ToString
public class Customer implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 客户ID
     */
    private String id;

    /**
     * 客户手机号
     */
    private String phone;

    /**
     * 客户姓名
     */
    private String name;

    /**
     * 账户状态
     */
    private String status;

    /**
     * 客户类型
     */
    private String customerType;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 客户性别
     */
    private String gender;

    /**
     * 客户生日
     */
    private LocalDateTime birthday;

    /**
     * 客户身份证号
     */
    private String idNumber;

    /**
     * 客户家庭住址
     */
    private String address;

    /**
     * 客户邮箱
     */
    private String email;

    /**
     * 逻辑删除标识（0-未删除，1-已删除）
     */
    @TableLogic
    private Integer isDeleted;
}
