package rulai.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ServiceException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import rulai.annotation.AllowAnonymous;
import rulai.annotation.Dev_Permission;
import rulai.annotation.NotBlankIfPresent;
import rulai.common.APIResponse;


import rulai.dto.CustomerCreateDTO;
import rulai.dto.CustomerModifyDTO;
import rulai.dto.CustomerQueryDTO;

import rulai.entity.Customer;
import rulai.httpstatus.exceptions.BadRequestException;
import rulai.httpstatus.exceptions.ResourceNotFoundException;
import rulai.httpstatus.exceptions.ServiceUnavailableException;

import rulai.mapper.CustomerMapper;
import rulai.service.impl.CustomerServiceImpl;
import rulai.utils.QueryHelper;

import rulai.vo.CustomerDetailVO;
import rulai.vo.CustomerVO;
import rulai.vo.table.CustomerTableVO;
import rulai.weblogger.WebLog;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;
import java.util.LinkedHashMap;

import static rulai.utils.ReflectionUtils.isEmpty;

/**
 * 存储电信客户信息
 * @author Mistry
 * @since 2025-12-26
 */
@Controller
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;
    @Autowired
    private CustomerServiceImpl customerService;
    @Autowired
    private CustomerMapper customerMapper;
    /**
     * 根据电话号码获得id
     * @param phone
     */
    @GetMapping("/phone")
    @ResponseBody
    @WebLog
    @Dev_Permission
    public APIResponse<String> getCustomerByPhone(@NotBlank String phone)
    {
        String id = customerService.getCustomerIdByPhone(phone);
        if(id == null|| id.isEmpty())
        {
            throw new ResourceNotFoundException("未找到用户");
        }
        else{
            return APIResponse.success(id);
        }
    }


    /**
     * 根据id得到客户的业务信息
     * @param id
     */
    @GetMapping("/detail")
    @ResponseBody
    @WebLog
    @Dev_Permission
    public APIResponse<CustomerDetailVO> getCustomerDetail(@RequestParam @NotBlank String id)
    {
        CustomerDetailVO customerDetailVO = customerService.getCustomerDetail(id);

        return APIResponse.success(customerDetailVO);
    }

    /**
     * 分页查询客户列表
     */
    @GetMapping("/page")
    @ResponseBody
    @WebLog
    @Dev_Permission
    public IPage<CustomerTableVO> pageCustomers(CustomerQueryDTO queryDTO) {
        return customerService.pageCustomers(queryDTO);
    }


    @WebLog
    @ResponseBody
    @PostMapping("/create")
    @SuppressWarnings("rawtypes")
    @AllowAnonymous
    @ResponseStatus(HttpStatus.CREATED)
    public APIResponse createCustomer(@Valid @RequestBody CustomerCreateDTO dto) {
        Customer customer = customerService.createCustomer(dto);
        return APIResponse.success(customerMapper.selectById(customer.getId()));
    }

    @WebLog
    @ResponseBody
    @DeleteMapping("/{customerId}")
    @SuppressWarnings("rawtypes")
    public APIResponse deletecustomer(@PathVariable String customerId){
        customerService.deleteCustomer(customerId);
        return APIResponse.success();
    }

/*    @WebLog
    @ResponseBody
    @DeleteMapping("/{customerId}/subtree")
    @SuppressWarnings("rawtypes")
    public APIResponse deleteSubtree(@PathVariable String customerId){
        customerService.deleteSubtree(customerId);
        return APIResponse.success();
    }*/

    @WebLog
    @ResponseBody
    @GetMapping("/{customerId}")
    @SuppressWarnings("rawtypes")
    @AllowAnonymous
    public APIResponse getCustomer(@PathVariable String customerId) {
        return APIResponse.success(customerMapper.selectById(customerId));
    }

    @WebLog
    @ResponseBody
    @PutMapping("/{customerId}")
    @SuppressWarnings({"rawtypes"})
    public APIResponse updateCustomer(@PathVariable String customerId, @Valid @RequestBody CustomerModifyDTO dto) {
        if(isEmpty(dto)) {
            throw new BadRequestException("请求中不含任何有效属性");
        }
        dto.setId(customerId);
        customerService.updateCustomer(dto);
        return APIResponse.success();
    }


    @WebLog
    @ResponseBody
    @PostMapping("/list")
    public APIResponse<List<CustomerTableVO>> listCustomer(CustomerQueryDTO queryDTO) {
        List<CustomerTableVO> result = customerService.customerList(queryDTO);
        return APIResponse.success(result);
    }
}





