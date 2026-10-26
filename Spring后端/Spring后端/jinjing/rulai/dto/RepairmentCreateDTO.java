package rulai.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class RepairmentCreateDTO {


    /**
     * 客户ID
     */
    private String customerId;

    /**
     * 创建客服ID
     */
    private String userId;

    /**
     * 维修标题
     */
    private String title;

    /**
     * 问题描述
     */
    private String description;

    /**
     * 维修地址
     */
    private String address;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 预约时间
     */
    
    private LocalDateTime appointmentTime;

    /**
     * 订单状态
     */
    private String status;

    /**
     * 优先级
     */
    private String priority;

    /**
     * 处理结果
     */
    private String result;
}
