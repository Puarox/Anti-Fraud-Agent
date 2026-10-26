package rulai.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class CostVO {
    private String id;
    private String costType;
    private BigDecimal amount;
    private String description;
    private LocalDate billingDate;
    private LocalDate dueDate;
    private String status;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
