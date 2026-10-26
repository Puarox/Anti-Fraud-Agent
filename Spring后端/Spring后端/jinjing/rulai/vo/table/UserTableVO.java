package rulai.vo.table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import rulai.vo.UserVO;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserTableVO extends UserVO {
    // 扩展统计字段
    private Integer todayRecordCount;    // 今日服务记录数
    private Integer monthlyRecordCount;  // 本月服务记录数
    private Double avgSatisfactionScore; // 平均满意度评分
    private String department;           // 所属部门
}