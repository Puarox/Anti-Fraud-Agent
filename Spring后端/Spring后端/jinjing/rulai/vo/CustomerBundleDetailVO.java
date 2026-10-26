package rulai.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CustomerBundleDetailVO {
    String customerId ;
    BigDecimal totalSmsCount ;
    BigDecimal totalDataCount ;
    BigDecimal totalCallCount ;

    BigDecimal totalBundleCount ;
    BigDecimal totalBundleCost ;
}
