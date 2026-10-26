package rulai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import rulai.convertor.ICustomerConverter;
import rulai.dto.CustomerCreateDTO;
import rulai.dto.CustomerModifyDTO;
import rulai.dto.CustomerQueryDTO;
import rulai.entity.Customer;
import rulai.httpstatus.exceptions.BadRequestException;
import rulai.httpstatus.exceptions.ResourceNotFoundException;
import rulai.mapper.CustomerMapper;
import rulai.service.ICustomerService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import rulai.utils.RedisUtils;
import rulai.utils.ReflectionUtils;
import rulai.vo.CustomerDetailVO;
import rulai.vo.CustomerVO;
import rulai.vo.table.CustomerTableVO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.*;
/**
 * <p>
 * 存储电信客户信息 服务实现类
 * </p>
 *
 * @author Mistry
 * @since 2025-12-26
 */
@Service
@Slf4j
public class CustomerServiceImpl extends ServiceImpl<CustomerMapper, Customer> implements ICustomerService {
    @Autowired
    private CustomerMapper customerMapper;
    @Autowired
    private ICustomerConverter customerConverter;
    @Autowired
    private CustomerBundleServiceImpl customerBundleService;

    @Autowired
    private CostServiceImpl costServiceImpl;
    @Autowired
    private RedisUtils redisUtils;


    // Redis缓存配置
    private static final String CUSTOMER_DETAIL_KEY_PREFIX = "customer:detail:";
    private static final long CACHE_EXPIRE_SECONDS = 30L;

    /**
     * 分页查询客户列表
     */
    @Override
    public IPage<CustomerTableVO> pageCustomers(CustomerQueryDTO queryDTO) {
        Page<CustomerTableVO> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        QueryWrapper<CustomerTableVO> wrapper = buildQueryWrapper(queryDTO);
        return customerMapper.selectCustomerPage(page, wrapper);
    }

    @Override
    public CustomerDetailVO getCustomerDetail(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("客户ID不能为空");
        }

        // 0. 先尝试从Redis获取
        CustomerDetailVO cachedDetail = getFromCache(id);
        if (cachedDetail != null) {
            log.info("从缓存获取客户详情，ID: {}", id);
            return cachedDetail;
        }

        // 创建返回对象


        // 1. 查询Customer表
        Customer customer = customerMapper.selectById(id);
        if (customer == null) {
            throw new ResourceNotFoundException("客户不存在，ID: " + id);
        }

        // 设置基本信息
        CustomerDetailVO customerDetailVO = customerConverter.toDetailVO(customer);
        // 2. 查询Costs表得到Balance和CostList

        costServiceImpl.setCostInfo(id, customerDetailVO);

        // 3. 查询Customer_Bundle表得到BundleList
        customerBundleService.setBundleInfo(id, customerDetailVO);

        // 4. 存入Redis缓存
        saveToCache(id, customerDetailVO);

        return customerDetailVO;
    }
    /**
     * 从缓存获取客户详情
     */
    private CustomerDetailVO getFromCache(String customerId) {
        try {
            String cacheKey = CUSTOMER_DETAIL_KEY_PREFIX + customerId;
            return redisUtils.get(cacheKey, CustomerDetailVO.class);
        } catch (Exception e) {
            log.warn("从Redis获取缓存未命中，customerId: {}", customerId, e);
            return null;
        }
    }
    /**
     * 保存到缓存
     */
    private void saveToCache(String customerId, CustomerDetailVO customerDetailVO) {
        try {
            String cacheKey = CUSTOMER_DETAIL_KEY_PREFIX + customerId;
            redisUtils.set(cacheKey, customerDetailVO, CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
            log.info("客户详情已缓存，ID: {}", customerId);
        } catch (Exception e) {
            log.warn("保存到Redis缓存失败，customerId: {}", customerId, e);
            // 缓存失败不影响主流程
        }
    }
    /**
     * 清除缓存
     */
    public void clearCustomerCache(String customerId) {
        try {
            String cacheKey = CUSTOMER_DETAIL_KEY_PREFIX + customerId;
            redisUtils.delete(cacheKey);
            log.info("已清除客户缓存，ID: {}", customerId);
        } catch (Exception e) {
            log.error("清除缓存失败，customerId: {}", customerId, e);
        }
    }

    /**
     * 批量获取客户详情（带缓存）
     */
    public List<CustomerDetailVO> batchGetCustomerDetails(List<String> customerIds) {
        if (customerIds == null || customerIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<CustomerDetailVO> result = new ArrayList<>();
        List<String> needQueryIds = new ArrayList<>();

        // 1. 先尝试从缓存获取
        for (String customerId : customerIds) {
            CustomerDetailVO cached = getFromCache(customerId);
            if (cached != null) {
                result.add(cached);
            } else {
                needQueryIds.add(customerId);
            }
        }

        // 2. 查询数据库获取剩余数据
        if (!needQueryIds.isEmpty()) {
            for (String customerId : needQueryIds) {
                try {
                    CustomerDetailVO detail = getCustomerDetail(customerId);
                    result.add(detail);
                } catch (Exception e) {
                    log.error("获取客户详情失败，customerId: {}", customerId, e);
                }
            }
        }

        return result;
    }

    /**
     * 创建
     * @param dto
     * @return
     */
    @Override
    public Customer createCustomer(CustomerCreateDTO dto) {
        Customer customer = customerConverter.fromCreateDTO(dto);
        LocalDateTime dateTime = LocalDateTime.now();
        customer.setCreatedAt(dateTime);
        customer.setId(UUID.randomUUID().toString());
        if(save(customer)) {
            return getById(customer.getId());
        } else {
            throw new ResourceNotFoundException("新建客户失败");
        }
    }
    @Override
    public void deleteCustomer(String customerId) {

    }

    @Override
    public void updateCustomer(CustomerModifyDTO dto) {
        Customer modified = customerConverter.fromModifyDTO(dto);
        Customer customer = getById(modified.getId());

        if(customer == null) {
            throw new ResourceNotFoundException("目标客户不存在");
        } else {
            ReflectionUtils.update(customer, modified);
        }
        updateById(customer);
    }



    @Override
    public List<CustomerTableVO> customerList(CustomerQueryDTO queryDTO) {
        QueryWrapper<CustomerTableVO> wrapper = buildQueryWrapper(queryDTO);
        return customerMapper.selectCustomerList(wrapper);
    }

    /**
     *
     * @param customerId
     * @return
     */
    @Override
    public boolean isNormalCustomer(String customerId) {
        if(customerId == null || customerId.trim().isEmpty()) {
            throw new BadRequestException("未提供用户id");
        }
        Customer customer = customerMapper.selectById(customerId);
        if(customer == null) {
            throw new ResourceNotFoundException("用户未找到");
        }
        return Objects.equals(customer.getStatus(), "NORMAL");
    }

    private QueryWrapper<CustomerTableVO> buildQueryWrapper(CustomerQueryDTO queryDTO) {
        QueryWrapper<CustomerTableVO> wrapper = new QueryWrapper<>();

        // 1. 精确查询条件
        if (StringUtils.isNotBlank(queryDTO.getPhone())) {
            wrapper.eq("phone", queryDTO.getPhone());
        }

        if (StringUtils.isNotBlank(queryDTO.getName())) {
            wrapper.like("name", queryDTO.getName());
        }

        if (StringUtils.isNotBlank(queryDTO.getStatus())) {
            wrapper.eq("status", queryDTO.getStatus());
        }

        if (StringUtils.isNotBlank(queryDTO.getCustomerType())) {
            wrapper.eq("customer_type", queryDTO.getCustomerType());
        }

        // 2. 时间范围查询
        if (queryDTO.getStartTime() != null && queryDTO.getEndTime() != null) {
            wrapper.between("created_at", queryDTO.getStartTime(), queryDTO.getEndTime());
        }

        // 3. 是否有欠费查询
        if (Boolean.TRUE.equals(queryDTO.getHasArrears())) {
            wrapper.exists("SELECT 1 FROM cost WHERE cost.customer_id = customer.id AND cost.status IN ('unpaid', 'overdue')");
        } else if (Boolean.FALSE.equals(queryDTO.getHasArrears())) {
            wrapper.notExists("SELECT 1 FROM cost WHERE cost.customer_id = customer.id AND cost.status IN ('unpaid', 'overdue')");
        }
        // 确保至少有 WHERE 条件，避免只有 ORDER BY
        if (wrapper.isEmptyOfWhere()) {
            wrapper.apply("1=1");
        }
        return wrapper;
    }

    /**
     * 如函数名
     * @param phone
     * @return
     */
    public String getCustomerIdByPhone(String phone) {
        // 参数校验
        if (StringUtils.isBlank(phone)) {
            return null;
        }
        // 构建查询条件
        QueryWrapper<Customer> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", phone.trim())
                .select("id");

        // 执行查询
        Customer customer = customerMapper.selectOne(queryWrapper);

        // 返回结果
        return customer != null ? customer.getId() : null;
    }
}
