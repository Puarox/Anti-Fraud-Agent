package rulai.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CustomerBundleQueryDTO extends PageQueryParam {
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
}
