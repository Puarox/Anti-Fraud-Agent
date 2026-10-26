package rulai.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
/**
 * <p>
 * 存储电信充值规则
 * </p>
 *
 * @author Mistry
 * @since 2025-12-29
 */
@Getter
@Setter
@ToString
@TableName("recharge_rule")
public class RechargeRule implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 规则ID
     */
    private String id;

    /**
     * 规则名称
     */
    private String name;

    /**
     * 规则描述
     */
    private String description;

    /**
     * 支付金额
     */
    private BigDecimal paymentAmount;

    /**
     * 到账总金额
     */
    private BigDecimal creditedAmount;

    /**
     * 生效时间
     */
    private LocalDateTime effectiveDate;

    /**
     * 失效时间
     */
    private LocalDateTime expiryDate;

    /**
     * 每人最大使用次数
     */
    private Integer maxUsagePerUser;

    /**
     * 总使用次数限制
     */
    private Integer totalUsage;

    /**
     * 规则状态
     */
    private String state;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标识（0-未删除，1-已删除）
     */
    @TableLogic
    private Integer isDeleted;
}
