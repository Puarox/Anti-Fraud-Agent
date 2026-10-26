package rulai.vo.table;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class RepairmentTableVO {
    @TableId
    private String id;

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
    private Date appointmentTime;

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

    /**
     * 完成时间
     */
    private Date completedAt;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}
