package rulai.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
/**
 * <p>
 * 客户费用账单记录
 * </p>
 *
 * @author Mistry
 * @since 2025-12-29
 */
@Getter
@Setter
@ToString
public class Cost implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 费用ID
     */
    private String id;

    /**
     * 客户ID
     */
    private String customerId;

    /**
     * 费用类型
     */
    private String costType;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 费用说明
     */
    private String description;

    /**
     * 账单日期
     */
    private LocalDate billingDate;

    /**
     * 缴费截止日
     */
    private LocalDate dueDate;

    /**
     * 缴费状态
     */
    private String status;

    /**
     * 缴费时间
     */
    private LocalDateTime paidAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 逻辑删除标识（0-未删除，1-已删除）
     */
    @TableLogic
    private Integer isDeleted;
}
