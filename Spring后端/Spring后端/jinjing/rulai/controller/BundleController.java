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
import rulai.dto.BundleCreateDTO;
import rulai.dto.BundleModifyDTO;
import rulai.dto.BundleQueryDTO;

import rulai.entity.Bundle;

import rulai.httpstatus.exceptions.BadRequestException;
import rulai.mapper.BundleMapper;

import rulai.service.impl.BundleServiceImpl;

import rulai.utils.QueryHelper;
import rulai.vo.BundleVO;
import rulai.vo.table.BundleTableVO;

import rulai.weblogger.WebLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static rulai.utils.ReflectionUtils.isEmpty;

/**
 *
 * 存储电信套餐信息 前端控制器
 *
 *
 * @author Mistry
 * @since 2025-12-26
 */
@Controller
@RequestMapping("/bundle")
public class BundleController {
    @Autowired
    private BundleServiceImpl bundleService;

    @Autowired
    private BundleMapper bundleMapper;

    @WebLog
    @ResponseBody
    @PostMapping("/create")
    @SuppressWarnings("rawtypes")
    @AllowAnonymous
    @ResponseStatus(HttpStatus.CREATED)
    public APIResponse createBundle(@Valid @RequestBody BundleCreateDTO dto) {
        Bundle bundle = bundleService.createBundle(dto);
        return APIResponse.success(bundleMapper.selectById(bundle.getId()));
    }

    @WebLog
    @ResponseBody
    @DeleteMapping("/{bundleId}")
    @SuppressWarnings("rawtypes")
    public APIResponse deleteBundle(@PathVariable String bundleId){
        if(bundleService.deleteBundle(bundleId)){return APIResponse.success();}
        else {return APIResponse.success("删除错误");}

    }

/*    @WebLog
    @ResponseBody
    @DeleteMapping("/{bundleId}/subtree")
    @SuppressWarnings("rawtypes")
    public APIResponse deleteSubtree(@PathVariable String bundleId){
        bundleService.deleteSubtree(bundleId);
        return APIResponse.success();
    }*/

    @WebLog
    @ResponseBody
    @GetMapping("/{bundleId}")
    @SuppressWarnings("rawtypes")
    @AllowAnonymous
    @AutoLog(type = "READ", tableName = "bundle")
    public APIResponse getBundle(@PathVariable String bundleId) {
        return APIResponse.success(bundleMapper.selectById(bundleId));
    }

    @WebLog
    @ResponseBody
    @PutMapping("/{bundleId}")
    @SuppressWarnings({"rawtypes"})
    public APIResponse updateBundle(@PathVariable String bundleId, @Valid @RequestBody BundleModifyDTO dto) {
        if(isEmpty(dto)) {
            throw new BadRequestException("请求中不含任何有效属性");
        }
        dto.setId(bundleId);
        bundleService.updateBundle(dto);
        return APIResponse.success();
    }
    @GetMapping("/page")
    @ResponseBody
    @WebLog
    @Dev_Permission
    public IPage<BundleTableVO> pageCustomers(BundleQueryDTO queryDTO) {
        return bundleService.pageBundles(queryDTO);
    }

    @WebLog
    @ResponseBody
    @PostMapping("/list")
    public APIResponse<List<BundleTableVO>> listBundle(BundleQueryDTO queryDTO) {
        return APIResponse.success(bundleService.listBundles(queryDTO));
    }
}
