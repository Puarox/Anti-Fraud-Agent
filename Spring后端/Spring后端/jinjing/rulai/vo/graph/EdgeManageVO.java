package rulai.vo.graph;

import com.baomidou.mybatisplus.annotation.TableField;
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
public class EdgeManageVO implements Serializable {

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
    private String conditions;

    /**
     * 绘图所用的其他数据，以JSON储存
     */
    private String graphical;

    /**
     * 边描述
     */
    private String description;
}
