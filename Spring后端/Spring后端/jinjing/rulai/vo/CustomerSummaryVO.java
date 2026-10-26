package rulai.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 客户详情视图对象
 * 用于前端展示客户完整信息
 *
 * @author Mistry
 * @since 2025-12-29
 */
@Data
public class CustomerSummaryVO {

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
     * 枚举：NORMAL(正常), ARREARS(欠费), SUSPENDED(停机), CANCELLED(注销)
     */
    private String status;
    
    /**
     * 客户类型
     */
    private String customerType;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 账户余额
     */
    private BigDecimal accountBalance;

    /**
     * 总消费
     */
    private BigDecimal totalConsumption;
    
    /**
     * 本月已消费金额
     */
    private BigDecimal monthConsumption;

    /**
     * 办理的套餐数
     */
    private String bundleCount;

    /**
     * 最后服务时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastServiceTime;

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