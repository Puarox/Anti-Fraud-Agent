package rulai.vo.table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CostTableVO {
    private String id;
    private String customerId;
    private String costType;
    private BigDecimal amount;
    private String description;
    private LocalDate billingDate;
    private LocalDate dueDate;
    private String status;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;

    // 关联客户信息
    private String customerName;
    private String customerPhone;
    private String customerStatus;
    private String customerType;
}