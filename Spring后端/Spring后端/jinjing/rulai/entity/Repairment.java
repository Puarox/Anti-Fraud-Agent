package rulai.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 维修订单信息
 * @TableName repairment
 */
@Data
@TableName(value ="repairment")
public class Repairment {
    /**
     * 订单ID
     */
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
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标识（0-未删除，1-已删除）
     */
    @TableLogic
    private Integer isDeleted;
}