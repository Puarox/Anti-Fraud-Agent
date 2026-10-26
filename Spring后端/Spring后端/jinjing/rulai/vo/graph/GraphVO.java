package rulai.vo.graph;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * <p>
 * 图VO
 * </p>
 *
 * @author Mistry
 * @since 2025-12-31
 */
@Getter
@Setter
@ToString
public class GraphVO {
    /**
     * 图理论起点ID
     */
    private String startNode;

    /**
     * 图理论终点ID
     */
    private String endNode;

    /**
     * 图所有点列表
     */
    private List<NodeVO> nodes;
}
