package rulai.controller;

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
import rulai.convertor.CustomerBundleConverterImpl;
import rulai.dto.*;
import rulai.entity.Cost;
import rulai.entity.CustomerBundle;
import rulai.httpstatus.exceptions.BadRequestException;
import rulai.httpstatus.exceptions.ResourceNotFoundException;
import rulai.mapper.CostMapper;
import rulai.mapper.CustomerBundleMapper;
import rulai.service.impl.CostServiceImpl;
import rulai.service.impl.CustomerBundleServiceImpl;
import rulai.vo.CustomerBundleDetailVO;
import rulai.vo.CustomerBundleVO;
import rulai.vo.table.CostTableVO;
import rulai.vo.table.CustomerBundleTableVO;
import rulai.weblogger.WebLog;

import java.util.List;

import static rulai.utils.ReflectionUtils.isEmpty;

/**
 * 客户与套餐的订阅关系 前端控制器
 *
 * @author Mistry
 * @since 2025-12-26
 */
@Controller
@RequestMapping("/customer-bundle")
public class CustomerBundleController {
    @Autowired
    private CustomerBundleServiceImpl customerBundleService;
    @Autowired
    private CustomerBundleConverterImpl  customerBundleConverter ;
    @Autowired
    private CustomerBundleMapper customerBundleMapper;

    @WebLog
    @ResponseBody
    @PostMapping("/create")
    @SuppressWarnings("rawtypes")
    @AllowAnonymous
    @ResponseStatus(HttpStatus.CREATED)
    @AutoLog(type = "CREATE", tableName = "customer_bundle")
    public APIResponse createCustomerBundle(@Valid @RequestBody CustomerBundleCreateDTO dto) {
        CustomerBundleVO customerBundleVO = customerBundleService.createCustomerBundle(dto);
        return APIResponse.success(customerBundleMapper.selectById(customerBundleVO.getId()));
    }

    /**
     * 取消套餐
     * @param customerBundleId
     * @return
     */
    @WebLog
    @ResponseBody
    @PutMapping("/cancel/{customerBundleId}")
    @SuppressWarnings("rawtypes")
    @AutoLog(type = "DELETE", tableName = "customer_bundle")
    public APIResponse cancelCustomerBundle(@PathVariable String customerBundleId){
        customerBundleService.cancelCustomerBundle(customerBundleId);
        return APIResponse.success();
    }
    @WebLog
    @ResponseBody
    @GetMapping("/{customerBundleId}")
    @SuppressWarnings("rawtypes")
    @AllowAnonymous
    @AutoLog(type = "READ", tableName = "customer_bundle")
    public APIResponse getCustomerBundle(@PathVariable String customerBundleId) {
        CustomerBundle customerBundle = customerBundleMapper.selectById(customerBundleId);
        if (customerBundle == null) {
            throw new ResourceNotFoundException("未找到该用户套餐");
        }
        else {
            return APIResponse.success(customerBundleConverter.toTO(customerBundleMapper.selectById(customerBundleId)));
        }

    }

    @WebLog
    @ResponseBody
    @PutMapping("/{customerBundleId}")
    @SuppressWarnings({"rawtypes"})
    @AutoLog(type = "UPDATE", tableName = "customer_bundle")
    public APIResponse updateCustomerBundle(@PathVariable String customerBundleId, @Valid @RequestBody CustomerBundleModifyDTO dto) {
        if(isEmpty(dto)) {
            throw new BadRequestException("请求中不含任何有效属性");
        }
        dto.setId(customerBundleId);
        customerBundleService.updateCost(dto);
        return APIResponse.success();
    }
    @GetMapping("/page")
    @ResponseBody
    @WebLog
    @Dev_Permission
    @AutoLog(type = "READ", tableName = "customer_bundle")
    public IPage<CustomerBundleTableVO> pageCustomerBundle(CustomerBundleQueryDTO queryDTO) {
        return customerBundleService.pageCustomerBundle(queryDTO);
    }

    @WebLog
    @ResponseBody
    @PostMapping("/list")
    @AutoLog(type = "READ", tableName = "customer_bundle")
    public APIResponse<List<CustomerBundleTableVO>> listCost(CustomerBundleQueryDTO queryDTO) {
        return APIResponse.success(customerBundleService.listCustomerBundle(queryDTO));
    }

    @WebLog
    @ResponseBody
    @GetMapping("/meta")
    @AutoLog(type = "READ", tableName = "customer_bundle")
    public APIResponse<CustomerBundleDetailVO> getCustomerBundleMeta(@Valid @RequestParam String customerId) {
        CustomerBundleDetailVO customerBundleDetailVO = customerBundleService.getCustomerBundleMeta(customerId);
        return APIResponse.success(customerBundleDetailVO);
    }
}
