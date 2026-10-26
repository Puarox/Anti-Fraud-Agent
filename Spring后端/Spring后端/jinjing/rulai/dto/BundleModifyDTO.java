package rulai.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BundleModifyDTO {
    /**
     * 套餐ID
     */
    private String id;

    /**
     * 套餐名称
     */
    private String name;

    /**
     * 套餐描述
     */
    private String description;

    /**
     * 月费
     */
    private BigDecimal price;

    /**
     * 流量限制(GB)
     */
    private BigDecimal dataLimit;

    /**
     * 通话时长(分钟)
     */
    private Integer callMinutes;

    /**
     * 短信条数
     */
    private Integer smsCount;

    /**
     * 生效时间
     */
    private LocalDateTime effectiveDate;

    /**
     * 失效时间
     */
    private LocalDateTime expiryDate;

    /**
     * 默认有效期(天)
     */
    private Integer validityDays;

    /**
     * 套餐是否可办理状态
     */
    private String state;


}
