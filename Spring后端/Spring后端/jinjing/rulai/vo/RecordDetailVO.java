package rulai.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import rulai.entity.OperationLog;
import rulai.pojo.Footprint;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 坐席与客户的通话记录和通话过程操作轨迹
 * </p>
 *
 * @author Mistry
 * @since 2026-01-05
 */
@Getter
@Setter
@ToString
public class RecordDetailVO implements Serializable {

    /**
     * 通话ID
     */
    private String id;

    /**
     * 客户ID
     */
    private String customerId;

    /**
     * 坐席ID
     */
    private String userId;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 满意度评分
     */
    private Byte satisfactionScore;

    /**
     * 通话摘要
     */
    private String summary;

    /**
     * 完全上下文
     */
    private String fullContext;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 服务轨迹内容
     */
    private List<Footprint> trace;

    /**
     * 客服操作记录
     */
    private List<OperationLog> operationLogs;
}
