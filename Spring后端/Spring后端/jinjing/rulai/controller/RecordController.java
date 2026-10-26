package rulai.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import rulai.annotation.AllowAnonymous;
import rulai.common.APIResponse;
import rulai.common.PageParam;
import rulai.entity.Record;
import rulai.httpstatus.exceptions.BadRequestException;
import rulai.service.IRecordService;
import rulai.utils.QueryHelper;
import rulai.weblogger.WebLog;

import java.util.HashMap;
import java.util.List;


/**
 * <p>
 * 坐席与客户的通话记录和通话过程操作轨迹 前端控制器
 * </p>
 *
 * @author Mistry
 * @since 2025-12-26
 */
@Controller
@RequestMapping("/record")
public class RecordController {
    @Autowired
    IRecordService recordService;

    /**
     * 删除记录
     * @param recordId
     * @return
     */
    @WebLog
    @ResponseBody
    @DeleteMapping("/{recordId}")
    @SuppressWarnings("rawtypes")
    public APIResponse deleteRecord(@PathVariable String recordId){
        
        Record record = recordService.getById(recordId);
        if(record == null){
            throw new BadRequestException("记录不存在");
        }
        recordService.removeById(recordId);
        return APIResponse.success();
    }

    /**
     * 根据ID获取记录
     * @param recordId
     * @return
     */
    @WebLog
    @ResponseBody
    @GetMapping("/{recordId}")
    @SuppressWarnings("rawtypes")
    @AllowAnonymous
    public APIResponse getRecord(@PathVariable String recordId) {
        return APIResponse.success(recordService.getRecord(recordId));
    }


    /**
     * 记录分页查询
     * @param pageParam
     * @return
     */
    @WebLog
    @ResponseBody
    @PostMapping("/page")
    public APIResponse<List<Record>> listPageRecord(@RequestBody PageParam pageParam) {
        Page<Record> page = new Page<>(pageParam.getPageNum(), pageParam.getPageSize());
        QueryWrapper<Record> queryWrapper = new QueryWrapper<>();
        HashMap<String, Object> param = pageParam.getParam();

        QueryHelper.handle(queryWrapper, param);

        IPage<Record> result = recordService.page(page, queryWrapper);
        return APIResponse.paginated(
                result.getTotal(),
                (int)result.getCurrent() ,
                (int)result.getSize(), result.getRecords()
        );
    }

    /**
     * 查询全部记录
     * @param param
     * @return
     */
    @WebLog
    @ResponseBody
    @PostMapping("/list")
    public APIResponse<List<Record>> listRecord(@RequestBody HashMap<String, Object> param) {
        QueryWrapper<Record> queryWrapper = new QueryWrapper<>();
        QueryHelper.handle(queryWrapper, param);
        List<Record> result = recordService.list(queryWrapper);
        return APIResponse.success(result);
    }
}
