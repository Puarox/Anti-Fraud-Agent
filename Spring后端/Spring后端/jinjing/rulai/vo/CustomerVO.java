package rulai.vo;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class CustomerVO {
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
}
