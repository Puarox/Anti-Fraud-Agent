package rulai.service;

import rulai.dto.EdgeCreateDTO;
import rulai.dto.EdgeModifyDTO;
import rulai.entity.Edge;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 服务流程边表 服务类
 * </p>
 *
 * @author Mistry
 * @since 2025-12-29
 */
public interface IEdgeService extends IService<Edge> {
    public Edge createEdge(EdgeCreateDTO dto);
    public Boolean updateEdge(EdgeModifyDTO dto);
    public Edge getEdge(String edgeId);
    public void deleteEdge(String edgeId);
    public List<Edge> getInEdges(String nodeId);
    public List<Edge> getOutEdges(String nodeId);
    public void batchDelete(List<String > edgeIds);
}
