package rulai.vo.table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import rulai.vo.CustomerBundleVO;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class CustomerBundleTableVO extends CustomerBundleVO {
    // 关联客户信息
    private String customerName;
    private String customerPhone;

    // 关联套餐信息
    private String bundleName;
    private BigDecimal bundlePrice;
    private String bundleType;

    // 统计信息
    private BigDecimal usedAmount;      // 已使用金额
    private BigDecimal remainingAmount; // 剩余金额
}