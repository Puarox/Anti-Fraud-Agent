package rulai.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import rulai.entity.Bundle;
import rulai.entity.Cost;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@ToString
@Data
public class CustomerDetailVO {
    /**
     * 客户基本信息
     */
    private String id;
    private String name;
    private String phone;
    private String status;
    private String customerType;
    private Date createdAt;

    /**
     * 话费余额
     */
    private BigDecimal balance;
    /**
     * 用户订阅的套餐
     */
    private List<BundleVO> bundleList;

    /**
     * 当前月份总扣费
     */
    private BigDecimal currentMonthCost ;

    /**
     * 用户消费记录信息
     */
    private List<CostVO> costList ;

    /**
     *  最近账单日期最近账单金额
     */
    private LocalDate recentBillDate;
    private BigDecimal recentBillAmount;
    /**
     * 欠费金额
     */
    private BigDecimal unpaidBillAmount;
    /**
     * 最近一个月的套餐数量
     */
    private Integer currentMonthBundleAmount ;

    /**
     * 客户性别
     */
    private String gender;

    /**
     * 客户生日
     */
    private LocalDateTime birthday;

    /**
     * 客户身份证号
     */
    private String idNumber;

    /**
     * 客户家庭住址
     */
    private String address;

    /**
     * 客户邮箱
     */
    private String email;
}
