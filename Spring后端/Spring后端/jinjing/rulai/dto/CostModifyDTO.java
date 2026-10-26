package rulai.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Data
public class CostModifyDTO {
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
}
