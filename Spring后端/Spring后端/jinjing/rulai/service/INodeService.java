package rulai.service;

import rulai.dto.NodeCreateDTO;
import rulai.dto.NodeModifyDTO;
import rulai.entity.Edge;
import rulai.entity.Node;
import com.baomidou.mybatisplus.extension.service.IService;
import rulai.vo.graph.NodeManageVO;

/**
 * <p>
 * 服务流程节点表 服务类
 * </p>
 *
 * @author Mistry
 * @since 2025-12-29
 */
public interface INodeService extends IService<Node> {
    public Node createNode(NodeCreateDTO dto);
    public void updateNode(NodeModifyDTO dto);
    public NodeManageVO getNode(String nodeId);
    public void deleteNode(String nodeId);
    public Edge link(String srcId, String tgtId);
    public Node getStart();
    public Node getEnd();
}
