package rulai.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CostQueryDTO extends PageQueryParam {

    // 精确查询
    private String id;
    private String customerId;
    private String costType;
    private String status;

    // 模糊查询
    private String customerName;
    private String customerPhone;
    private String description;

    // 金额范围查询
    private BigDecimal minAmount;
    private BigDecimal maxAmount;

    // 时间范围查询
    private LocalDate billingStartDate;
    private LocalDate billingEndDate;
    private LocalDate dueStartDate;       // 缴费截止日期范围
    private LocalDate dueEndDate;
    private LocalDateTime paidStartTime;  // 缴费时间范围
    private LocalDateTime paidEndTime;
    private LocalDateTime createStartTime; // 创建时间范围
    private LocalDateTime createEndTime;

    // 业务条件
    private Boolean overdueOnly;         // 只查询逾期账单
    private Boolean unpaidOnly;          // 只查询未支付账单
    private Boolean paidOnly;           // 只查询已支付账单
    private Integer minOverdueDays;      // 最小逾期天数
}