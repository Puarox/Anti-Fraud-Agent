package rulai.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import rulai.annotation.AllowAnonymous;
import rulai.annotation.AutoLog;
import rulai.annotation.Dev_Permission;
import rulai.common.APIResponse;
import rulai.dto.*;
import rulai.entity.Cost;
import rulai.httpstatus.exceptions.BadRequestException;
import rulai.mapper.CostMapper;
import rulai.service.impl.CostServiceImpl;
import rulai.service.impl.CustomerServiceImpl;
import rulai.utils.QueryHelper;
import rulai.vo.table.CostTableVO;
import rulai.vo.table.CustomerTableVO;
import rulai.weblogger.WebLog;

import java.util.HashMap;
import java.util.List;

import static rulai.utils.ReflectionUtils.isEmpty;

/**
 * 客户费用账单记录 前端控制器
 * @author Mistry
 * @since 2025-12-26
 */
@Controller
@RequestMapping("/cost")
public class CostController {
    @Autowired
    private CostServiceImpl costService;

    @Autowired
    private CostMapper costMapper;

    @WebLog
    @ResponseBody
    @PostMapping("/create")
    @SuppressWarnings("rawtypes")
    @AllowAnonymous
    @ResponseStatus(HttpStatus.CREATED)
    @AutoLog(type = "CREATE", tableName = "cost")
    public APIResponse createCost(@Valid @RequestBody CostCreateDTO dto) {
        Cost cost = costService.createCost(dto);
        return APIResponse.success(costMapper.selectById(cost.getId()));
    }

    @WebLog
    @ResponseBody
    @DeleteMapping("/{costId}")
    @SuppressWarnings("rawtypes")
    @AutoLog(type = "DELETE", tableName = "cost")
    public APIResponse deleteCost(@PathVariable String costId){
        costService.deleteCost(costId);
        return APIResponse.success();
    }


    @WebLog
    @ResponseBody
    @GetMapping("/{costId}")
    @SuppressWarnings("rawtypes")
    @AllowAnonymous
    @AutoLog(type = "READ", tableName = "cost")
    public APIResponse getCost(@PathVariable String costId) {
        return APIResponse.success(costMapper.selectById(costId));
    }

    @WebLog
    @ResponseBody
    @PutMapping("/{costId}")
    @SuppressWarnings({"rawtypes"})
    @AutoLog(type = "UPDATE", tableName = "cost")
    public APIResponse updateCost(@PathVariable String costId, @Valid @RequestBody CostModifyDTO dto) {
        if(isEmpty(dto)) {
            throw new BadRequestException("请求中不含任何有效属性");
        }
        dto.setId(costId);
        costService.updateCost(dto);
        return APIResponse.success();
    }
    @GetMapping("/page")
    @ResponseBody
    @WebLog
    @Dev_Permission
    @AutoLog(type = "READ", tableName = "cost")
    public IPage<CostTableVO> pageCosts(CostQueryDTO queryDTO) {
        return costService.pageCosts(queryDTO);
    }

    @WebLog
    @ResponseBody
    @PostMapping("/list")
    @AutoLog(type = "READ", tableName = "cost")
    public APIResponse<List<CostTableVO>> listCost(CostQueryDTO queryDTO) {

        return APIResponse.success(costService.listCosts(queryDTO));
    }

    @WebLog
    @ResponseBody
    @PostMapping("/refund")
    @AutoLog(type = "CREATE", tableName = "cost")
    public APIResponse refundCost(CostRefundDTO refundDTO) {
        costService.refundCost(refundDTO);
        return APIResponse.success("退款成功");
    }
}
