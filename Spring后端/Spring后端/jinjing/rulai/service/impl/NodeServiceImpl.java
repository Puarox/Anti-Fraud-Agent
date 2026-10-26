package rulai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import rulai.constant.NODE_CONSTANT;
import rulai.convertor.IEdgeConverter;
import rulai.convertor.INodeConverter;
import rulai.convertor.IScriptConverter;
import rulai.dto.EdgeCreateDTO;
import rulai.dto.NodeCreateDTO;
import rulai.dto.NodeModifyDTO;
import rulai.entity.Edge;
import rulai.entity.Node;
import rulai.entity.Script;
import rulai.httpstatus.exceptions.BadRequestException;
import rulai.httpstatus.exceptions.ResourceNotFoundException;
import rulai.mapper.NodeMapper;
import rulai.service.IEdgeService;
import rulai.service.INodeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import rulai.service.IScriptService;
import rulai.vo.graph.EdgeManageVO;
import rulai.vo.graph.NodeManageVO;
import rulai.vo.graph.ScriptManageVO;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务流程节点表 服务实现类
 * </p>
 *
 * @author Mistry
 * @since 2025-12-29
 */
@Service
public class NodeServiceImpl extends ServiceImpl<NodeMapper, Node> implements INodeService {

    @Autowired
    INodeConverter nodeConverter;

    @Autowired
    IEdgeService edgeService;

    @Autowired
    IEdgeConverter edgeConverter;

    @Autowired
    NodeMapper nodeMapper;

    @Autowired
    IScriptConverter scriptConverter;

    @Autowired
    IScriptService scriptService;

    @Override
    public Node createNode(NodeCreateDTO dto) {
        Node node = nodeConverter.fromDTO(dto);
        node.setId(UUID.randomUUID().toString());

        if(node.getType().equals(NODE_CONSTANT.TYPE.START.name()) && getStart() != null){
            throw new BadRequestException("服务流程不允许出现多个起点");
        }

        if(node.getType().equals(NODE_CONSTANT.TYPE.END.name()) && getEnd() != null){
            throw new BadRequestException("服务流程不允许出现多个终点");
        }

        if(save(node)) {
            return getById(node.getId());
        } else {
            throw new ResourceNotFoundException("新建失败");
        }
    }

    @Override
    public void updateNode(NodeModifyDTO dto) {
        Node node = nodeConverter.fromDTO(dto);

        if(node.getType().equals(NODE_CONSTANT.TYPE.START.name())){
            Node start = getStart();
            start.setType(NODE_CONSTANT.TYPE.READ.name());
            updateById(start);
        }

        if(node.getType().equals(NODE_CONSTANT.TYPE.END.name())){
            Node end = getEnd();
            end.setType(NODE_CONSTANT.TYPE.READ.name());
            updateById(end);
        }

        if(updateById(node)) {

        } else {
            throw new ResourceNotFoundException("目标节点不存在");
        }
    }

    @Override
    public NodeManageVO getNode(String nodeId) {
        Node node = getById(nodeId);
        if (node == null) {
            throw new ResourceNotFoundException("节点不存在");
        }
        NodeManageVO nodeManageVO = nodeConverter.toNodeManageVO(node);

        List<Edge> edges = edgeService.getOutEdges(nodeId);
        List<EdgeManageVO> edgeManageVOList = edges.stream().map(edgeConverter::toEdgeManageVO).collect(Collectors.toList());
        nodeManageVO.setEdges(edgeManageVOList);

        List<Script> scripts = scriptService.getNodeScript(nodeId);
        List<ScriptManageVO> scriptManageVOList = scripts.stream().map(scriptConverter::toScriptManageVO).collect(Collectors.toList());
        nodeManageVO.setScripts(scriptManageVOList);
        return nodeManageVO;
    }

    @Transactional
    @Override
    public void deleteNode(String nodeId) {
        Node node = getById(nodeId);
        if(node == null) {
            throw new ResourceNotFoundException("节点不存在");
        }
        if(node.getType().equals(NODE_CONSTANT.TYPE.START.name())){
            throw new BadRequestException("起点不可删除");
        }
        if(node.getType().equals(NODE_CONSTANT.TYPE.END.name())){
            throw new BadRequestException("终点不可删除");
        }

        edgeService.batchDelete(edgeService.getInEdges(nodeId).stream().map(Edge::getId).collect(Collectors.toList()));
        edgeService.batchDelete(edgeService.getOutEdges(nodeId).stream().map(Edge::getId).collect(Collectors.toList()));
        removeById(nodeId);
    }

    @Override
    public Edge link(String srcId, String tgtId) {

        Node src = nodeMapper.selectById(srcId);
        Node tgt = nodeMapper.selectById(tgtId);

        if (src == null) {
            throw new ResourceNotFoundException(String.format("边起点 %s 不存在",  srcId));
        }
        if (tgt == null) {
            throw new ResourceNotFoundException(String.format("边终点 %s 不存在",  tgtId));
        }

        EdgeCreateDTO edgeCreateDTO = new EdgeCreateDTO();
        edgeCreateDTO.setSourceId(srcId);
        edgeCreateDTO.setTargetId(tgtId);
        edgeCreateDTO.setName(String.format("%s - %s", src.getName(), tgt.getName()));
        return edgeService.createEdge(edgeCreateDTO);
    }

    @Override
    public Node getStart() {
        LambdaQueryWrapper<Node> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Node::getType, NODE_CONSTANT.TYPE.START.name());

        Node start = nodeMapper.selectOne(queryWrapper);
        if (start == null) {
            start = new Node();
            start.setType(NODE_CONSTANT.TYPE.START.name());
            start.setId(UUID.randomUUID().toString());
            start.setName(NODE_CONSTANT.TYPE.START.name());
            save(start);
        }
        return start;
    }

    @Override
    public Node getEnd() {
        LambdaQueryWrapper<Node> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Node::getType, NODE_CONSTANT.TYPE.END.name());

        Node end = nodeMapper.selectOne(queryWrapper);
        if (end == null) {
            end = new Node();
            end.setType(NODE_CONSTANT.TYPE.END.name());
            end.setId(UUID.randomUUID().toString());
            end.setName(NODE_CONSTANT.TYPE.END.name());
            save(end);
        }

        return  end;
    }
}
