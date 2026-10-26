package rulai.vo.table;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
@Data
public class BundleTableVO {
    private String id;
    private String name;
    private String description;
    // 月费
    private BigDecimal price;
    // 流量限制(GB)
    private BigDecimal dataLimit;
    // 通话时长(分钟)
    private Integer callMinutes;
    // 短信条数
    private Integer smsCount;

    /**
     *  默认有效期(天)
     */

    private Integer validityDays;
    /**
     *   active/suspended/terminated
     */
    private String state;

    // 客户套餐关联信息
    private String customerBundleId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String bundleStatus;
}
