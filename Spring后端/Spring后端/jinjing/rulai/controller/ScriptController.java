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
import rulai.dto.ScriptCreateDTO;
import rulai.dto.ScriptModifyDTO;
import rulai.entity.Edge;
import rulai.entity.Script;
import rulai.httpstatus.exceptions.BadRequestException;
import rulai.service.IEdgeService;
import rulai.service.IScriptService;
import rulai.utils.QueryHelper;
import rulai.vo.graph.ScriptManageVO;
import rulai.weblogger.WebLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static rulai.utils.ReflectionUtils.isEmpty;

/**
 * <p>
 * 话术内容表 前端控制器
 * </p>
 *
 * @author Mistry
 * @since 2025-12-30
 */
@Controller
@RequestMapping("/script")
public class ScriptController {
    @Autowired
    private IScriptService scriptService;

    /**
     * 新建话术
     * @param dto
     * @return
     */
    @WebLog
    @ResponseBody
    @PostMapping("/create")
    @SuppressWarnings("rawtypes")
    @AllowAnonymous
    @ResponseStatus(HttpStatus.CREATED)
    public APIResponse createScript(@Valid @RequestBody ScriptCreateDTO dto) {
        Script script = scriptService.createScript(dto);
        return APIResponse.success(scriptService.getScript(script.getId()));
    }

    /**
     * 删除话术
     * @param scriptId
     * @return
     */
    @WebLog
    @ResponseBody
    @DeleteMapping("/{scriptId}")
    @SuppressWarnings("rawtypes")
    public APIResponse deleteScript(@PathVariable String scriptId){
        scriptService.deleteScript(scriptId);
        return APIResponse.success();
    }

    /**
     * 根据ID获取话术
     * @param scriptId
     * @return
     */
    @WebLog
    @ResponseBody
    @GetMapping("/{scriptId}")
    @SuppressWarnings("rawtypes")
    @AllowAnonymous
    public APIResponse getScript(@PathVariable String scriptId) {
        return APIResponse.success(scriptService.getScript(scriptId));
    }

    /**
     * 更新话术信息
     * @param scriptId
     * @param dto
     * @return
     */
    @WebLog
    @ResponseBody
    @PutMapping("/{scriptId}")
    @SuppressWarnings({"rawtypes"})
    public APIResponse updateScript(@PathVariable String scriptId, @Valid @RequestBody ScriptModifyDTO dto) {
        if(isEmpty(dto)) {
            throw new BadRequestException("请求中不含任何有效属性");
        }
        dto.setId(scriptId);
        scriptService.updateScript(dto);
        return APIResponse.success();
    }

    /**
     * 话术分页查询
     * @param pageParam
     * @return
     */
    @WebLog
    @ResponseBody
    @PostMapping("/page")
    public APIResponse<List<Script>> listPageScript(@RequestBody PageParam pageParam) {
        Page<Script> page = new Page<>(pageParam.getPageNum(), pageParam.getPageSize());
        QueryWrapper<Script> queryWrapper = new QueryWrapper<>();
        HashMap<String, Object> param = pageParam.getParam();

        QueryHelper.handle(queryWrapper, param);

        IPage<Script> result = scriptService.page(page, queryWrapper);
        return APIResponse.paginated(
                result.getTotal(),
                (int)result.getCurrent() ,
                (int)result.getSize(), result.getRecords()
        );
    }

    /**
     * 查询全部话术
     * @param param
     * @return
     */
    @WebLog
    @ResponseBody
    @PostMapping("/list")
    public APIResponse<List<Script>> listScript(@RequestBody HashMap<String, Object> param) {
        QueryWrapper<Script> queryWrapper = new QueryWrapper<>();
        QueryHelper.handle(queryWrapper, param);
        List<Script> result = scriptService.list(queryWrapper);
        return APIResponse.success(result);
    }

    /**
     * 根据节点Id查询话术
     * @param nodeId
     * @return
     */
    @WebLog
    @ResponseBody
    @GetMapping("/list/node/{nodeId}")
    public APIResponse<List<ScriptManageVO>> listNodeScript(@PathVariable String nodeId) {
        return APIResponse.success(scriptService.listNodeScriptByNodeId(nodeId));
    }

    /**
     * 查询通用话术
     * @return
     */
    @WebLog
    @ResponseBody
    @GetMapping("/list/common")
    public APIResponse<List<ScriptManageVO>> listCommonScript() {
        return APIResponse.success(scriptService.listCommonScript());
    }

    /**
     * 查询反馈话术
     * @return
     */
    @WebLog
    @ResponseBody
    @GetMapping("/list/feedback")
    public APIResponse<List<ScriptManageVO>> listFeedbackScript() {
        return APIResponse.success(scriptService.listFeedbackScript());
    }
}
