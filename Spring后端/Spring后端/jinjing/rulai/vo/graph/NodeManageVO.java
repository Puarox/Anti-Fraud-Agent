package rulai.vo.graph;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

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
public class NodeManageVO implements Serializable {

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
     * 节点描述
     */
    private String description;

    /**
     * 节点出边列表
     */
    private List<EdgeManageVO> edges;

    /**
     * 节点话术列表
     */
    private List<ScriptManageVO> scripts;
}
