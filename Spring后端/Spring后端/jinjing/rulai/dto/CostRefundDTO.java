package rulai.dto;

import lombok.Data;
import rulai.entity.Cost;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CostRefundDTO {

    private String originalCostId ;
    /**
     * 客户ID
     */
    private String customerId;

}
