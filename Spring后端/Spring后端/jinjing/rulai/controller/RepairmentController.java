package rulai.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import rulai.annotation.AllowAnonymous;
import rulai.annotation.AutoLog;
import rulai.annotation.Dev_Permission;
import rulai.common.APIResponse;
import rulai.convertor.RepairmentConverterImpl;
import rulai.dto.CostQueryDTO;
import rulai.dto.RepairmentCreateDTO;
import rulai.dto.RepairmentQueryDTO;
import rulai.entity.Repairment;
import rulai.mapper.RepairmentMapper;
import rulai.service.impl.RepairmentServiceImpl;
import rulai.vo.RepairmentVO;
import rulai.vo.table.CostTableVO;
import rulai.vo.table.RepairmentTableVO;
import rulai.weblogger.WebLog;

import java.util.List;

/**
 *  维修订单记录 前端控制器
 */
@Controller
@RequestMapping("/repairment")
public class RepairmentController {

    @Autowired
    private RepairmentServiceImpl repairmentService;
    @Autowired
    private RepairmentMapper repairmentMapper;
    @Autowired
    private RepairmentConverterImpl converter;

    /**
     * 创建维修订单
     * @param dto
     * @return
     */
    @WebLog
    @PostMapping("/create")
    @ResponseBody
    @AllowAnonymous
    @AutoLog(type = "CREATE", tableName = "repairment")
    public APIResponse createRepairment(@Valid @RequestBody RepairmentCreateDTO dto) {
        Repairment repairment = repairmentService.createRepairment(dto);
        return APIResponse.success(repairment.getId());
    }

    /**
     * 根据ID获得单个订单信息
     * @param repairmentId
     * @return
     */
    @WebLog
    @GetMapping("/{repairmentId}")
    @ResponseBody
    @AllowAnonymous
    @AutoLog(type = "READ", tableName = "repairment")
    public APIResponse<RepairmentVO> getRepairment(@PathVariable String repairmentId) {
        return APIResponse.success(converter.toVO(repairmentMapper.selectById(repairmentId)));
    }

    /**
     * 分页条件查询
     * @param queryDTO
     * @return
     */
    @GetMapping("/page")
    @ResponseBody
    @WebLog
    @Dev_Permission
    @AutoLog(type = "READ", tableName = "repairment")
    public APIResponse<IPage<RepairmentTableVO>> pageRepairment(RepairmentQueryDTO queryDTO) {
        return APIResponse.success(repairmentService.pageRepairment(queryDTO));
    }

    /**
     * list查询
     * @param queryDTO
     * @return
     */
    @WebLog
    @ResponseBody
    @PostMapping("/list")
    @AutoLog(type = "READ", tableName = "repairment")
    public APIResponse<List<RepairmentTableVO>> listRepairment(RepairmentQueryDTO queryDTO) {
        return APIResponse.success(repairmentService.listRepairments(queryDTO));
    }

}
