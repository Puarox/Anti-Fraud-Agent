package rulai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import rulai.annotation.AllowAnonymous;
import rulai.common.APIResponse;
import rulai.convertor.IEdgeConverter;
import rulai.convertor.INodeConverter;
import rulai.entity.Edge;
import rulai.service.IEdgeService;
import rulai.service.INodeService;
import rulai.vo.graph.GraphVO;
import rulai.vo.graph.NodeVO;
import rulai.weblogger.WebLog;

import java.util.stream.Collectors;


/**
 * <p>
 * 图控制器
 * </p>
 *
 * @author Mistry
 * @since 2025-12-29
 */
@Controller
@RequestMapping("/graph")
public class GraphController {

    @Autowired
    private INodeService nodeService;

    @Autowired
    private IEdgeService edgeService;

    @Autowired
    private INodeConverter nodeConverter;

    @Autowired
    private IEdgeConverter edgeConverter;

    /**
     * 返回整个图结构
     * @param
     * @return
     */
    @WebLog
    @ResponseBody
    @GetMapping("/structure")
    @SuppressWarnings("rawtypes")
    @AllowAnonymous
    public APIResponse getStructure() {

        GraphVO graphVO = new GraphVO();
        graphVO.setStartNode(nodeService.getStart().getId());
        graphVO.setEndNode(nodeService.getEnd().getId());
        graphVO.setNodes(nodeService.list().stream().map(nodeConverter::toNodeVO).collect(Collectors.toList()));

        for(NodeVO nodeVO : graphVO.getNodes()){
            LambdaQueryWrapper<Edge> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Edge::getSourceId, nodeVO.getId());
            nodeVO.setOutEdges(edgeService.list(queryWrapper).stream().map(edgeConverter::toEdgeVO).collect(Collectors.toList()));
        }

        return APIResponse.success(graphVO);
    }

    /**
     * 返回图起点
     * @param
     * @return
     */
    @WebLog
    @ResponseBody
    @GetMapping("/start")
    @SuppressWarnings("rawtypes")
    @AllowAnonymous
    public APIResponse getStart() {
        return APIResponse.success(nodeService.getStart());
    }

    /**
     * 返回图终点
     * @param
     * @return
     */
    @WebLog
    @ResponseBody
    @GetMapping("/end")
    @SuppressWarnings("rawtypes")
    @AllowAnonymous
    public APIResponse getEnd() {
        return APIResponse.success(nodeService.getEnd());
    }
}
