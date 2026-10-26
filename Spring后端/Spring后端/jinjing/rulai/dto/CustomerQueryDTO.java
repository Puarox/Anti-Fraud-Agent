package rulai.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class CustomerQueryDTO extends PageQueryParam {
    /**
     * 客户手机号
     */
    private String phone;

    /**
     * 客户姓名
     */
    private String name;

    /**
     * 账户状态
     */
    private String status;

    private String customerType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean hasArrears;     // 是否有欠费
}