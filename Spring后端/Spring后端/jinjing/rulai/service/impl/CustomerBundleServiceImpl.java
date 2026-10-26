package rulai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import rulai.convertor.ICustomerBundleConverter;
import rulai.dto.CostQueryDTO;
import rulai.dto.CustomerBundleCreateDTO;
import rulai.dto.CustomerBundleModifyDTO;
import rulai.dto.CustomerBundleQueryDTO;
import rulai.entity.Bundle;
import rulai.entity.Cost;
import rulai.entity.Customer;
import rulai.entity.CustomerBundle;
import rulai.httpstatus.exceptions.BadRequestException;
import rulai.httpstatus.exceptions.ResourceNotFoundException;
import rulai.mapper.BundleMapper;
import rulai.mapper.CustomerBundleMapper;
import rulai.service.ICustomerBundleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import rulai.utils.ReflectionUtils;
import rulai.vo.BundleVO;
import rulai.vo.CustomerBundleDetailVO;
import rulai.vo.CustomerBundleVO;
import rulai.vo.CustomerDetailVO;
import rulai.vo.table.CostTableVO;
import rulai.vo.table.CustomerBundleTableVO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * <p>
 * 客户与套餐的订阅关系 服务实现类
 * </p>
 *
 * @author Mistry
 * @since 2025-12-26
 */
@Slf4j
@Service
public class CustomerBundleServiceImpl extends ServiceImpl<CustomerBundleMapper, CustomerBundle> implements ICustomerBundleService {
    @Autowired
    private BundleMapper bundleMapper;
    @Autowired
    private ICustomerBundleConverter customerBundleConverter;
    @Autowired
    private BundleServiceImpl bundleService;
    @Autowired
    private CustomerServiceImpl customerService;
    @Autowired
    private CustomerBundleMapper customerBundleMapper;
    /**
     * 设置套餐信息
     */
    public void setBundleInfo(String customerId, CustomerDetailVO customerDetailVO) {
        try {
            // 3.1 查询客户订阅的套餐关联
            QueryWrapper<CustomerBundle> bundleQuery = new QueryWrapper<>();
            bundleQuery.eq("customer_id", customerId)
                    .orderByDesc("start_date");

            List<CustomerBundle> customerBundles = customerBundleMapper.selectList(bundleQuery);

            if (customerBundles != null && !customerBundles.isEmpty()) {
                List<BundleVO> bundleVOList = new ArrayList<>();
                BigDecimal totalBundleCost = BigDecimal.ZERO;

                for (CustomerBundle customerBundle : customerBundles) {
                    // 3.2 查询套餐详情
                    Bundle bundle = bundleMapper.selectById(customerBundle.getBundleId());
                    if (bundle != null && "ACTIVE".equals(bundle.getState())) {
                        BundleVO bundleVO = bundleService.convertToBundleVO(bundle, customerBundle);
                        bundleVOList.add(bundleVO);

                        // 累计套餐费用
                        if (bundle.getPrice() != null) {
                            totalBundleCost = totalBundleCost.add(bundle.getPrice());
                        }
                    }
                }
                customerDetailVO.setCurrentMonthBundleAmount(bundleVOList.size());
                customerDetailVO.setBundleList(bundleVOList);
            } else {
                // 如果没有套餐
                customerDetailVO.setBundleList(new ArrayList<>());
            }

        } catch (Exception e) {
            log.error("查询客户套餐信息失败，customerId: {}", customerId, e);
            customerDetailVO.setBundleList(new ArrayList<>());
        }
    }



    @Override
    public CustomerBundleVO createCustomerBundle(CustomerBundleCreateDTO dto) {
        String customerId = dto.getCustomerId();
        //1. 判断用户是否可以办理套餐
        if(!customerService.isNormalCustomer(customerId)){
            throw new BadRequestException("用户状态不正常");
        }
        CustomerBundle customerBundle = customerBundleConverter.fromCreateDTO(dto);
        LocalDateTime localDateTime = LocalDateTime.now();
        customerBundle.setId(UUID.randomUUID().toString());
        customerBundle.setCreatedAt(localDateTime);
        customerBundle.setStatus("ACTIVE");
        CustomerBundleVO customerBundleVO = customerBundleConverter.toTO(customerBundle);
        customerBundleMapper.insert(customerBundle);
        return customerBundleVO;
    }

    @Override
    public void updateCost(CustomerBundleModifyDTO dto) {
        CustomerBundle modified = customerBundleConverter.fromModfiyDTO(dto);
        CustomerBundle customerBundle = customerBundleMapper.selectById(dto.getId());
        if (customerBundle != null) {
            ReflectionUtils.update(customerBundle,modified);
            updateById(modified);
        }
        else {
            throw new ResourceNotFoundException("需要更新的订单不存在");
        }
    }

    @Override
    public IPage<CustomerBundleTableVO> pageCustomerBundle(CustomerBundleQueryDTO queryDTO) {
        Page<CustomerBundleTableVO> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        QueryWrapper<CustomerBundleTableVO> queryWrapper = buildQueryWrapper(queryDTO);
        return customerBundleMapper.selectCustomerBundlePage(page,queryWrapper);
    }

    @Override
    public List<CustomerBundleTableVO> listCustomerBundle(CustomerBundleQueryDTO queryDTO) {

        QueryWrapper<CustomerBundleTableVO> queryWrapper = buildQueryWrapper(queryDTO);
        return customerBundleMapper.selectCustomerBundles(queryWrapper);
    }

    @Override
    public CustomerBundleDetailVO getCustomerBundleMeta(String customerId) {
        CustomerBundleDetailVO customerBundleDetailVO = new CustomerBundleDetailVO();
        //1 .获取用户有效套餐列表
        CustomerBundleQueryDTO customerBundleQueryDTO = new CustomerBundleQueryDTO();
        customerBundleQueryDTO.setCustomerId(customerId);
        customerBundleQueryDTO.setStatus("ACTIVE");
        List<CustomerBundleTableVO> customerBundleTableVOList = listCustomerBundle(customerBundleQueryDTO);
        // 2. 计算各类数据
        if (customerBundleTableVOList == null || customerBundleTableVOList.isEmpty()) {
            customerBundleDetailVO.setTotalBundleCount(BigDecimal.ZERO);
            customerBundleDetailVO.setTotalBundleCost(BigDecimal.ZERO);
            customerBundleDetailVO.setTotalCallCount(BigDecimal.ZERO);
            customerBundleDetailVO.setTotalSmsCount(BigDecimal.ZERO);
            customerBundleDetailVO.setTotalDataCount(BigDecimal.ZERO);
        } else {
            customerBundleDetailVO.setTotalBundleCount(BigDecimal.valueOf(customerBundleTableVOList.size()));

            // 初始化统计变量
            BigDecimal totalCost = BigDecimal.ZERO;
            BigDecimal TotalCallCount = BigDecimal.ZERO;
            BigDecimal totalSmsCount = BigDecimal.ZERO;
            BigDecimal totalDataCount = BigDecimal.ZERO;

            // 遍历套餐列表进行累加计算
            for (CustomerBundleTableVO customerBundle : customerBundleTableVOList) {
                if (customerBundle.getBundlePrice() != null) {
                    totalCost = totalCost.add(customerBundle.getBundlePrice());
                }
                Bundle currentBundle = bundleMapper.selectById(customerBundle.getBundleId());
                if(currentBundle == null){throw new ResourceNotFoundException("找不到对应的套餐");}
                else{
                    TotalCallCount = TotalCallCount.add(BigDecimal.valueOf(currentBundle.getCallMinutes()));
                    totalSmsCount = totalSmsCount.add(BigDecimal.valueOf(currentBundle.getSmsCount()));
                    totalDataCount = totalDataCount.add(currentBundle.getDataLimit());
                }


            }

            customerBundleDetailVO.setTotalBundleCost(totalCost);
            customerBundleDetailVO.setTotalCallCount(TotalCallCount);
            customerBundleDetailVO.setTotalSmsCount(totalSmsCount);
            customerBundleDetailVO.setTotalDataCount(totalDataCount);
        }

        // 3. 设置客户基本信息
        customerBundleDetailVO.setCustomerId(customerId);

        // 4. 返回结果
        return customerBundleDetailVO;
    }

    @Override
    public Boolean checkBundleForeignKey(String bundleId) {
        CustomerBundleQueryDTO queryDTO = new CustomerBundleQueryDTO();
        queryDTO.setBundleId(bundleId);
        queryDTO.setStatus("ACTIVE");

        List<CustomerBundleTableVO> customerBundleTableVOList = listCustomerBundle(queryDTO);
        if (customerBundleTableVOList == null || customerBundleTableVOList.isEmpty()) {
            return false;
        }else
        {
            return true ;
        }
    }

    /**
     * 取消用户订单
     * @param customerBundleId
     */
    public void cancelCustomerBundle(String customerBundleId) {
        CustomerBundle customerBundle = customerBundleMapper.selectById(customerBundleId);
        if (customerBundle == null) {
            throw  new ResourceNotFoundException("该用户套餐不存在");
        }else if(!Objects.equals(customerBundle.getStatus(), "ACTIVE")) {
            throw new BadRequestException("只能取消状态为ACTIVE的套餐，当前状态为：" + customerBundle.getStatus());
        }
        else {

            customerBundle.setStatus("CANCELLED");
            customerBundleMapper.updateById(customerBundle);
        }
    }
    /**
     * 构建查询条件
     */
    private QueryWrapper<CustomerBundleTableVO> buildQueryWrapper(CustomerBundleQueryDTO queryDTO) {
        QueryWrapper<CustomerBundleTableVO> wrapper = new QueryWrapper<>();

        // 1. 精确查询条件
        if (StringUtils.isNotBlank(queryDTO.getId())) {
            wrapper.eq("cb.id", queryDTO.getId());
        }

        if (StringUtils.isNotBlank(queryDTO.getCustomerId())) {
            wrapper.eq("cb.customer_id", queryDTO.getCustomerId());
        }

        if (StringUtils.isNotBlank(queryDTO.getBundleId())) {
            wrapper.eq("cb.bundle_id", queryDTO.getBundleId());
        }

        if (StringUtils.isNotBlank(queryDTO.getStatus())) {
            wrapper.eq("cb.status", queryDTO.getStatus());
        }

        // 2. 时间范围查询
        if (queryDTO.getStartDate() != null && queryDTO.getEndDate() != null) {
            wrapper.between("cb.start_date", queryDTO.getStartDate(), queryDTO.getEndDate());
        } else {
            if (queryDTO.getStartDate() != null) {
                wrapper.ge("cb.start_date", queryDTO.getStartDate());
            }
            if (queryDTO.getEndDate() != null) {
                wrapper.le("cb.end_date", queryDTO.getEndDate());
            }
        }

        // 3. 创建时间查询
        if (queryDTO.getCreatedAt() != null) {
            // 这里假设createdAt是精确到日期的查询，如果需要范围查询可以调整
            wrapper.eq("cb.created_at", queryDTO.getCreatedAt());
        }
        // 确保至少有 WHERE 条件，避免只有 ORDER BY
        if (wrapper.isEmptyOfWhere()) {
            wrapper.apply("1=1");
        }
        // 4. 排序逻辑（如果有排序字段的话）
        applySorting(wrapper, queryDTO);

        return wrapper;
    }
    private void applySorting(QueryWrapper<CustomerBundleTableVO> wrapper, CustomerBundleQueryDTO queryDTO) {
        if (StringUtils.isNotBlank(queryDTO.getSortField())) {
            // 如果有排序字段，使用指定的排序
            boolean isAsc = !"desc".equalsIgnoreCase(queryDTO.getSortOrder());
            wrapper.orderBy(true, isAsc, queryDTO.getSortField());
        } else {
            // 如果没有指定排序字段，使用默认排序
            wrapper.orderByDesc("cb.created_at");
        }
    }
}
