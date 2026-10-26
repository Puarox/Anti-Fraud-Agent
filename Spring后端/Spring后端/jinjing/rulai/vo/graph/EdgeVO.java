package rulai.vo.graph;

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
 * @since 2025-12-29
 */
@Getter
@Setter
@ToString
public class EdgeVO implements Serializable {

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
}
