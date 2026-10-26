package rulai.vo.graph;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * <p>
 * 节点VO
 * </p>
 *
 * @author Mistry
 * @since 2025-12-31
 */
@Getter
@Setter
@ToString
public class NodeVO {
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
    private String graphical;

    /**
     * 节点支持的操作配置，以JSON储存
     */
    private String operations;

    /**
     * 节点描述
     */
    private String description;

    /**
     * 该节点的所有出边
     */
    private List<EdgeVO> outEdges;
}
