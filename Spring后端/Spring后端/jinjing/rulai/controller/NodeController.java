package rulai.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import rulai.annotation.AllowAnonymous;
import rulai.common.APIResponse;
import rulai.common.PageParam;
import rulai.convertor.INodeConverter;
import rulai.dto.NodeCreateDTO;
import rulai.dto.NodeModifyDTO;
import rulai.entity.Edge;
import rulai.entity.Node;
import rulai.httpstatus.exceptions.BadRequestException;
import rulai.service.IEdgeService;
import rulai.service.INodeService;
import rulai.utils.QueryHelper;
import rulai.weblogger.WebLog;

import java.util.HashMap;
import java.util.List;

import static rulai.utils.ReflectionUtils.isEmpty;

/**
 * <p>
 * 服务流程节点表 前端控制器
 * </p>
 *
 * @author Mistry
 * @since 2025-12-29
 */
@Controller
@RequestMapping("/node")
public class NodeController {

    @Autowired
    private INodeService nodeService;

    /**
     * 新建节点
     * @param dto
     * @return
     */
    @WebLog
    @ResponseBody
    @PostMapping("/create")
    @SuppressWarnings("rawtypes")
    @AllowAnonymous
    @ResponseStatus(HttpStatus.CREATED)
    public APIResponse createNode(@Valid @RequestBody NodeCreateDTO dto) {
        Node node = nodeService.createNode(dto);
        System.out.println("node = " + node);
        return APIResponse.success(nodeService.getNode(node.getId()));
    }

    /**
     * 删除节点
     * @param nodeId
     * @return
     */
    @WebLog
    @ResponseBody
    @DeleteMapping("/{nodeId}")
    @SuppressWarnings("rawtypes")
    public APIResponse deleteNode(@PathVariable String nodeId){
        nodeService.deleteNode(nodeId);
        return APIResponse.success();
    }

    /**
     * 根据ID获取节点
     * @param nodeId
     * @return
     */
    @WebLog
    @ResponseBody
    @GetMapping("/{nodeId}")
    @SuppressWarnings("rawtypes")
    @AllowAnonymous
    public APIResponse getNode(@PathVariable String nodeId) {
        return APIResponse.success(nodeService.getNode(nodeId));
    }

    /**
     * 更新节点信息
     * @param nodeId
     * @param dto
     * @return
     */
    @WebLog
    @ResponseBody
    @PutMapping("/{nodeId}")
    @SuppressWarnings({"rawtypes"})
    public APIResponse updateNode(@PathVariable String nodeId, @Valid @RequestBody NodeModifyDTO dto) {
        if(isEmpty(dto)) {
            throw new BadRequestException("请求中不含任何有效属性");
        }
        dto.setId(nodeId);
        nodeService.updateNode(dto);
        return APIResponse.success();
    }

    /**
     * 节点分页查询
     * @param pageParam
     * @return
     */
    @WebLog
    @ResponseBody
    @PostMapping("/page")
    public APIResponse<List<Node>> listPageNode(@RequestBody PageParam pageParam) {
        Page<Node> page = new Page<>(pageParam.getPageNum(), pageParam.getPageSize());
        QueryWrapper<Node> queryWrapper = new QueryWrapper<>();
        HashMap<String, Object> param = pageParam.getParam();

        QueryHelper.handle(queryWrapper, param);

        IPage<Node> result = nodeService.page(page, queryWrapper);
        return APIResponse.paginated(
                result.getTotal(),
                (int)result.getCurrent() ,
                (int)result.getSize(), result.getRecords()
        );
    }

    /**
     * 根据条件查询全部节点
     * @param param
     * @return
     */
    @WebLog
    @ResponseBody
    @PostMapping("/list")
    public APIResponse<List<Node>> listNode(@RequestBody HashMap<String, Object> param) {
        QueryWrapper<Node> queryWrapper = new QueryWrapper<>();
        QueryHelper.handle(queryWrapper, param);
        List<Node> result = nodeService.list(queryWrapper);
        return APIResponse.success(result);
    }

    /**
     * 在两个节点之间作有向边
     * @param srcId
     * @param tgtId
     * @return
     */
    @WebLog
    @ResponseBody
    @PostMapping("/link")
    @SuppressWarnings("rawtypes")
    @AllowAnonymous
    @ResponseStatus(HttpStatus.CREATED)
    public APIResponse linkNodes(@RequestParam String srcId, @RequestParam String tgtId) {
        Edge edge = nodeService.link(srcId, tgtId);
        return APIResponse.success(edge);
    }
}
