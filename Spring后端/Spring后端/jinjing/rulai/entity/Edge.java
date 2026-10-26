package rulai.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
/**
 * <p>
 * 服务流程边表
 * </p>
 *
 * @author Mistry
 * @since 2026-01-05
 */
@Getter
@Setter
@ToString
@TableName(autoResultMap = true)
public class Edge implements Serializable {

    /**
     * 边唯一ID
     */
    private String id;

    /**
     * 边类型
     */
    private String type;

    /**
     * 有向边起点id
     */
    private String sourceId;

    /**
     * 有向边终点id
     */
    private String targetId;

    /**
     * 边名
     */
    private String name;

    /**
     * 边转移条件配置，以JSON储存
     */
    @TableField(value = "conditions", typeHandler = JacksonTypeHandler.class)
    private String conditions;

    /**
     * 绘图所用的其他数据，以JSON储存
     */
    @TableField(value = "graphical", typeHandler = JacksonTypeHandler.class)
    private String graphical;

    /**
     * 边描述
     */
    private String description;

    /**
     * 逻辑删除标识（0-未删除，1-已删除）
     */
    @TableLogic
    private Integer isDeleted;
}
