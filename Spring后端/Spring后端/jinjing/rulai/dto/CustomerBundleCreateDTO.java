package rulai.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CustomerBundleCreateDTO {

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


}
