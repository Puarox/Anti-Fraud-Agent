package rulai.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
/**
 * <p>
 * 客户与套餐的订阅关系
 * </p>
 *
 * @author Mistry
 * @since 2025-12-29
 */
@Getter
@Setter
@ToString
@TableName("customer_bundle")
public class CustomerBundle implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关联ID
     */
    private String id;

    /**
     * 客户ID
     */
    private String customerId;

    /**
     * 套餐ID
     */
    private String bundleId;

    /**
     * 生效日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 状态
     */
    private String status;

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
