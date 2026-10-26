package rulai.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import rulai.annotation.AllowAnonymous;
import rulai.common.APIResponse;
import rulai.common.PageParam;
import rulai.dto.EdgeModifyDTO;
import rulai.entity.Edge;
import rulai.httpstatus.exceptions.BadRequestException;
import rulai.service.IEdgeService;
import rulai.utils.QueryHelper;
import rulai.weblogger.WebLog;

import java.util.HashMap;
import java.util.List;

import static rulai.utils.ReflectionUtils.isEmpty;

/**
 * <p>
 * 服务流程边表 前端控制器
 * </p>
 *
 * @author Mistry
 * @since 2025-12-29
 */
@Controller
@RequestMapping("/edge")
public class EdgeController {

    @Autowired
    IEdgeService edgeService;

    /**
     * 删除边
     * @param edgeId
     * @return
     */
    @WebLog
    @ResponseBody
    @DeleteMapping("/{edgeId}")
    @SuppressWarnings("rawtypes")
    public APIResponse deleteEdge(@PathVariable String edgeId){
        edgeService.deleteEdge(edgeId);
        return APIResponse.success();
    }

    /**
     * 根据ID获取边
     * @param edgeId
     * @return
     */
    @WebLog
    @ResponseBody
    @GetMapping("/{edgeId}")
    @SuppressWarnings("rawtypes")
    @AllowAnonymous
    public APIResponse getEdge(@PathVariable String edgeId) {
        return APIResponse.success(edgeService.getEdge(edgeId));
    }

    /**
     * 更新边信息
     * @param edgeId
     * @param dto
     * @return
     */
    @WebLog
    @ResponseBody
    @PutMapping("/{edgeId}")
    @SuppressWarnings({"rawtypes"})
    public APIResponse updateEdge(@PathVariable String edgeId, @Valid @RequestBody EdgeModifyDTO dto) {
        if(isEmpty(dto)) {
            throw new BadRequestException("请求中不含任何有效属性");
        }
        dto.setId(edgeId);
        edgeService.updateEdge(dto);
        return APIResponse.success();
    }

    /**
     * 边分页查询
     * @param pageParam
     * @return
     */
    @WebLog
    @ResponseBody
    @PostMapping("/page")
    public APIResponse<List<Edge>> listPageEdge(@RequestBody PageParam pageParam) {
        Page<Edge> page = new Page<>(pageParam.getPageNum(), pageParam.getPageSize());
        QueryWrapper<Edge> queryWrapper = new QueryWrapper<>();
        HashMap<String, Object> param = pageParam.getParam();

        QueryHelper.handle(queryWrapper, param);

        IPage<Edge> result = edgeService.page(page, queryWrapper);
        return APIResponse.paginated(
                result.getTotal(),
                (int)result.getCurrent() ,
                (int)result.getSize(), result.getRecords()
        );
    }

    /**
     * 查询全部边
     * @param param
     * @return
     */
    @WebLog
    @ResponseBody
    @PostMapping("/list")
    public APIResponse<List<Edge>> listEdge(@RequestBody HashMap<String, Object> param) {
        QueryWrapper<Edge> queryWrapper = new QueryWrapper<>();
        QueryHelper.handle(queryWrapper, param);
        List<Edge> result = edgeService.list(queryWrapper);
        return APIResponse.success(result);
    }

}
