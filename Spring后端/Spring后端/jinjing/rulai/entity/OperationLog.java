package rulai.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;
/**
 * <p>
 * 存储系统操作日志，用于追踪用户操作历史
 * </p>
 *
 * @author Mistry
 * @since 2026-01-06
 */
@Getter
@Setter
@ToString
@TableName("operation_log")
public class OperationLog implements Serializable {

    /**
     * 日志ID
     */
    private String id;

    /**
     * 操作用户ID
     */
    private String userId;

    /**
     * 操作类型 CREATE, READ, UPDATE, DELETE, OTHER
     */
    private String type;

    /**
     * 操作的表名
     */
    private String tableName;

    /**
     * 请求URL
     */
    private String url;

    /**
     * 请求
     */
    private String request;

    /**
     * 返回
     */
    private String respond;

    /**
     * 预留字段，可以用于 DELETE UPDATE 和OTHER记录操作前后变化
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 逻辑删除标识（0-未删除，1-已删除）
     */
    @TableLogic
    private Integer isDeleted;
}
