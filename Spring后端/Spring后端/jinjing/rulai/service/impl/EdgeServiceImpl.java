package rulai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import rulai.convertor.IEdgeConverter;
import rulai.dto.EdgeCreateDTO;
import rulai.dto.EdgeModifyDTO;
import rulai.entity.Edge;
import rulai.httpstatus.exceptions.ResourceNotFoundException;
import rulai.mapper.EdgeMapper;
import rulai.service.IEdgeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * <p>
 * 服务流程边表 服务实现类
 * </p>
 *
 * @author Mistry
 * @since 2025-12-29
 */
@Service
public class EdgeServiceImpl extends ServiceImpl<EdgeMapper, Edge> implements IEdgeService {

    @Autowired
    IEdgeConverter edgeConverter;

    @Override
    public Edge createEdge(EdgeCreateDTO dto) {
        Edge edge = edgeConverter.fromDTO(dto);
        edge.setId(UUID.randomUUID().toString());

        if(save(edge)) {
            return getById(edge.getId());
        } else {
            throw new ResourceNotFoundException("新建失败");
        }

    }

    @Override
    public Boolean updateEdge(EdgeModifyDTO dto) {
        Edge edge = edgeConverter.fromDTO(dto);
        if(updateById(edge)) {
            return true;
        } else {
            throw new ResourceNotFoundException("目标边不存在");
        }
    }

    @Override
    public Edge getEdge(String edgeId) {
        Edge edge = getById(edgeId);
        if (edge == null) {
            throw new ResourceNotFoundException("边不存在");
        }
        return edge;
    }

    @Override
    public List<Edge> getInEdges(String nodeId) {
        QueryWrapper<Edge> wrapper = new QueryWrapper<>();
        wrapper.eq("target_id", nodeId);
        List<Edge> edgeList = list(wrapper);
        return edgeList;
    }

    @Override
    public List<Edge> getOutEdges(String nodeId) {
        QueryWrapper<Edge> wrapper = new QueryWrapper<>();
        wrapper.eq("source_id", nodeId);
        List<Edge> edgeList = list(wrapper);
        return edgeList;
    }

    @Override
    public void deleteEdge(String edgeId) {
        Edge edge = getById(edgeId);
        if(edge == null) {
            throw new ResourceNotFoundException("边不存在");
        }
        removeById(edgeId);
    }

    @Transactional
    @Override
    public void batchDelete(List<String > edgeIds) {

        if(edgeIds == null || edgeIds.isEmpty()) {
            return;
        }

        LambdaQueryWrapper<Edge> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Edge::getId, edgeIds);
        List<Edge> edges = this.list(queryWrapper);

        if(edges.size() == edgeIds.size()) {
            removeBatchByIds(edgeIds);
        } else {
            throw new ResourceNotFoundException(String.format("找到的用户与输入不一致，预期%s，找到%s", edges.size(), edgeIds.size()));
        }
    }
}
