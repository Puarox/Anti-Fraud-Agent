package rulai.vo.table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import rulai.vo.CustomerVO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class CustomerTableVO extends CustomerVO {
    // 扩展统计字段
    private Integer bundleCount;          // 套餐数量
    private BigDecimal totalBalance;     // 总余额
    private BigDecimal currentMonthCost; // 本月费用
    private Integer recordCount;         // 服务记录数量
    private LocalDateTime lastContactTime; // 最后联系时间
}