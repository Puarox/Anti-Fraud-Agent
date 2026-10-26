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
 * 服务流程节点表
 * </p>
 *
 * @author Mistry
 * @since 2026-01-05
 */
@Getter
@Setter
@ToString
@TableName(autoResultMap = true)
public class Node implements Serializable {

    /**
     * 节点唯一ID
     */
    private String id;

    /**
     * 节点名
     */
    private String name;

    /**
     * 节点类型
     */
    private String type;

    /**
     * 节点X坐标
     */
    private Double x;

    /**
     * 节点Y坐标
     */
    private Double y;

    /**
     * 绘图所用的其他数据，以JSON储存
     */
    @TableField(value = "graphical", typeHandler = JacksonTypeHandler.class)
    private String graphical;

    /**
     * 节点支持的操作配置，以JSON储存
     */
    @TableField(value = "operations", typeHandler = JacksonTypeHandler.class)
    private String operations;

    /**
     * 节点描述
     */
    private String description;

    /**
     * 逻辑删除标识（0-未删除，1-已删除）
     */
    @TableLogic
    private Integer isDeleted;
}
