package rulai.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
/**
 * <p>
 * 存储电信套餐信息
 * </p>
 *
 * @author Mistry
 * @since 2025-12-29
 */
@Getter
@Setter
@ToString
public class Bundle implements Serializable {

    private static final long serialVersionUID = 1L;

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
