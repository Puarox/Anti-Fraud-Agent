package rulai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import rulai.constant.COST_CONSTANT;
import rulai.convertor.ICostConverter;
import rulai.dto.CostCreateDTO;
import rulai.dto.CostModifyDTO;
import rulai.dto.CostQueryDTO;
import rulai.dto.CostRefundDTO;
import rulai.entity.Cost;
import rulai.httpstatus.exceptions.BadRequestException;
import rulai.httpstatus.exceptions.ResourceNotFoundException;
import rulai.mapper.CostMapper;
import rulai.service.ICostService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import rulai.utils.ReflectionUtils;
import rulai.vo.CostVO;
import rulai.vo.CustomerDetailVO;
import rulai.vo.table.CostTableVO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 客户费用账单记录 服务实现类
 * </p>
 *
 * @author Mistry
 * @since 2025-12-26
 */
@Service
@Slf4j
public class CostServiceImpl extends ServiceImpl<CostMapper, Cost> implements ICostService {

    @Autowired
    private CostMapper costMapper;

    @Autowired
    private ICostConverter costConverter;
    /**
     * 创建cost
     * @param dto
     * @return
     */
    @Override
    public Cost createCost(CostCreateDTO dto) {
        Cost cost = costConverter.fromDTO(dto);
        LocalDateTime dateTime = LocalDateTime.now();
        cost.setCreatedAt(dateTime);
        cost.setId(UUID.randomUUID().toString());
        if(save(cost)) {
            return getById(cost.getId());
        } else {
            throw new ResourceNotFoundException("新建失败");
        }
    }
    @Override
    public Boolean updateCost(CostModifyDTO dto) {
        Cost modified = costConverter.fromDTO(dto);
        Cost cost = getById(modified.getId());

        if(cost == null) {
            throw new ResourceNotFoundException("目标费用不存在");
        } else {
            ReflectionUtils.update(cost, modified);
        }
        updateById(cost);
        return true;
    }
    /**
     * 获得消费信息
     * @param customerId
     * @return
     */
    public List<CostVO> getCostList(String customerId)
    {
        QueryWrapper<Cost> costQuery = new QueryWrapper<>();
        costQuery.eq("customer_id", customerId)
                .orderByDesc("billing_date");
        List<Cost> costList = costMapper.selectList(costQuery);
        List<CostVO> costVOList = new ArrayList<>();
        for (Cost cost : costList) {
            CostVO costVO = convertToCostVO(cost);
            costVOList.add(costVO);
        }
        return costVOList ;
    }

    public void setCostInfo(String customerId, CustomerDetailVO customerDetailVO) {
        try {
            // 2.1 查询所有费用记录
            QueryWrapper<Cost> costQuery = new QueryWrapper<>();
            costQuery.eq("customer_id", customerId)
                    .orderByDesc("created_at");

            List<Cost> costList = costMapper.selectList(costQuery);

            if (costList != null && !costList.isEmpty()) {
                // 转换为CostVO列表
                List<CostVO> costVOList = costList.stream()
                        .map(this::convertToCostVO)
                        .collect(Collectors.toList());

                customerDetailVO.setCostList(costVOList);

                // 2.2 计算总余额
                BigDecimal totalBalance = calculateTotalBalance(costList);
                customerDetailVO.setBalance(totalBalance);

                // 2.3 计算本月费用
                BigDecimal currentMonthCost = calculateCurrentMonthCost(costList);
                customerDetailVO.setCurrentMonthCost(currentMonthCost);

                //2.4 计算未支付费用
                BigDecimal unpaidBalance = totalBalance.subtract(currentMonthCost);
                customerDetailVO.setUnpaidBillAmount(unpaidBalance);
                customerDetailVO.setUnpaidBillAmount(BigDecimal.ZERO);
                // 2.5 获取最近账单
                if (!costList.isEmpty()) {
                    Cost recentCost = costList.get(0);
                    customerDetailVO.setRecentBillDate(recentCost.getBillingDate());
                    customerDetailVO.setRecentBillAmount(recentCost.getAmount());

                }
            } else {
                // 如果没有费用记录，设置默认值
                customerDetailVO.setCostList(new ArrayList<>());
                customerDetailVO.setBalance(BigDecimal.ZERO);
                customerDetailVO.setCurrentMonthCost(BigDecimal.ZERO);
                customerDetailVO.setUnpaidBillAmount(BigDecimal.ZERO);
            }

        } catch (Exception e) {
            log.error("查询客户费用信息失败，customerId: {}", customerId, e);
            // 设置默认值，不抛出异常
            customerDetailVO.setCostList(new ArrayList<>());
            customerDetailVO.setBalance(BigDecimal.ZERO);
        }
    }

    /**
     * 计算总余额
     * 逻辑：收入（refund）为正，支出（其他类型）为负
     */
    private BigDecimal calculateTotalBalance(List<Cost> costList) {
        BigDecimal balance = BigDecimal.ZERO;

        for (Cost cost : costList) {
            if (Arrays.asList(COST_CONSTANT.TYPE.REFUND.toString(),COST_CONSTANT.TYPE.RECHARGE.toString()).contains(cost.getCostType())) {
                // 退款是收入
                balance = balance.add(cost.getAmount());
            } else if (Arrays.asList(COST_CONSTANT.TYPE.PENALTY.toString(),COST_CONSTANT.TYPE.MONTHLY.toString(),COST_CONSTANT.TYPE.OTHER.toString()).contains(cost.getCostType())) {
                // 其他费用是支出
                balance = balance.subtract(cost.getAmount());
            }
        }
        return balance.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateCurrentMonthCost(List<Cost> costList) {
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfMonth = now.withDayOfMonth(1);

        return costList.stream()
                .filter(cost -> cost.getBillingDate() != null)
                .filter(cost -> {
                    LocalDate billingDate = cost.getBillingDate();
                    return !billingDate.isBefore(firstDayOfMonth) && !billingDate.isAfter(now);
                })
                .filter(cost -> !"refund".equals(cost.getCostType()))  // 退款不计入费用
                .map(Cost::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
    /**
     * 计算未支付金额
     */
    private BigDecimal calculateUnpaidAmount(List<Cost> costList) {
        return costList.stream()
                .filter(cost -> "unpaid".equals(cost.getStatus()))
                .map(Cost::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
    /**
     * Cost 转换为 CostVO
     */
    private CostVO convertToCostVO(Cost cost) {
        CostVO vo = new CostVO();
        vo.setId(cost.getId());
        vo.setCostType(cost.getCostType());
        vo.setAmount(cost.getAmount());
        vo.setDescription(cost.getDescription());
        vo.setBillingDate(cost.getBillingDate());
        vo.setDueDate(cost.getDueDate());
        vo.setStatus(cost.getStatus());
        vo.setPaidAt(cost.getPaidAt());
        vo.setCreatedAt(cost.getCreatedAt());
        return vo;
    }
    /**
     * 分页查询费用记录
     */
    public IPage<CostTableVO> pageCosts(CostQueryDTO queryDTO) {
        Page<CostTableVO> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        QueryWrapper<CostTableVO> wrapper = buildQueryWrapper(queryDTO);
        return costMapper.selectCostPage(page, wrapper);
    }

    /**
     * 构建查询条件
     */
    private QueryWrapper<CostTableVO> buildQueryWrapper(CostQueryDTO queryDTO) {
        QueryWrapper<CostTableVO> wrapper = new QueryWrapper<>();
        // 1. 精确查询条件
        if (StringUtils.isNotBlank(queryDTO.getId())) {
            wrapper.eq("cost.id", queryDTO.getId());
        }

        if (StringUtils.isNotBlank(queryDTO.getCustomerId())) {
            wrapper.eq("cost.customer_id", queryDTO.getCustomerId());
        }

        if (StringUtils.isNotBlank(queryDTO.getCostType())) {
            wrapper.eq("cost.cost_type", queryDTO.getCostType());
        }

        if (StringUtils.isNotBlank(queryDTO.getStatus())) {
            wrapper.eq("cost.status", queryDTO.getStatus());
        }

        // 2. 模糊查询条件
        if (StringUtils.isNotBlank(queryDTO.getCustomerName())) {
            wrapper.like("c.name", queryDTO.getCustomerName());
        }

        if (StringUtils.isNotBlank(queryDTO.getCustomerPhone())) {
            wrapper.like("c.phone", queryDTO.getCustomerPhone());
        }

        if (StringUtils.isNotBlank(queryDTO.getDescription())) {
            wrapper.like("cost.description", queryDTO.getDescription());
        }

        // 3. 金额范围查询
        if (queryDTO.getMinAmount() != null) {
            wrapper.ge("cost.amount", queryDTO.getMinAmount());
        }

        if (queryDTO.getMaxAmount() != null) {
            wrapper.le("cost.amount", queryDTO.getMaxAmount());
        }

        // 4. 时间范围查询
        if (queryDTO.getBillingStartDate() != null && queryDTO.getBillingEndDate() != null) {
            wrapper.between("cost.billing_date", queryDTO.getBillingStartDate(), queryDTO.getBillingEndDate());
        }

        if (queryDTO.getDueStartDate() != null && queryDTO.getDueEndDate() != null) {
            wrapper.between("cost.due_date", queryDTO.getDueStartDate(), queryDTO.getDueEndDate());
        }

        if (queryDTO.getPaidStartTime() != null && queryDTO.getPaidEndTime() != null) {
            wrapper.between("cost.paid_at", queryDTO.getPaidStartTime(), queryDTO.getPaidEndTime());
        }

        // 5. 业务条件查询
        if (Boolean.TRUE.equals(queryDTO.getOverdueOnly())) {
            wrapper.eq("cost.status", "overdue");
        }

        if (Boolean.TRUE.equals(queryDTO.getUnpaidOnly())) {
            wrapper.eq("cost.status", "unpaid");
        }

        if (Boolean.TRUE.equals(queryDTO.getPaidOnly())) {
            wrapper.eq("cost.status", "paid");
        }

        if (queryDTO.getMinOverdueDays() != null) {
            wrapper.ge("DATEDIFF(CURRENT_DATE, cost.due_date)", queryDTO.getMinOverdueDays());
        }
        // 确保至少有 WHERE 条件，避免只有 ORDER BY
        if (wrapper.isEmptyOfWhere()) {
            wrapper.apply("1=1");
        }
        // 6. 排序逻辑
        applySorting(wrapper, queryDTO);

        return wrapper;
    }

    /**
     * 应用排序规则
     */
    private void applySorting(QueryWrapper<CostTableVO> wrapper, CostQueryDTO queryDTO) {
        if (StringUtils.isNotBlank(queryDTO.getSortField())) {
            boolean isAsc = "asc".equalsIgnoreCase(queryDTO.getSortOrder());
            if (isAsc) {
                wrapper.orderByAsc(queryDTO.getSortField());
            } else {
                wrapper.orderByDesc(queryDTO.getSortField());
            }
        }
    }

    /**
     * 根据ID查询费用详情
     */
    public CostTableVO getCostById(String costId) {
        return costMapper.selectCostDetail(costId);
    }

    /**
     * 查询客户费用统计
     */
   /* public List<Map<String, Object>> getCustomerCostSummary(String customerId) {
        return costMapper.selectCustomerCostSummary(customerId);
    }*/



    /**
     * 获取客户的所有费用记录
     */
    public List<CostTableVO> getCustomerCosts(String customerId) {
        CostQueryDTO queryDTO = new CostQueryDTO();
        queryDTO.setCustomerId(customerId);
        queryDTO.setSortField("billing_date");
        queryDTO.setSortOrder("desc");

        QueryWrapper<CostTableVO> wrapper = buildQueryWrapper(queryDTO);
        return costMapper.selectCostList(wrapper);
    }

    @Override
    public void deleteCost(String costId) {

    }
    @Override
    public List<CostTableVO> listCosts(CostQueryDTO queryDTO) {

        QueryWrapper<CostTableVO> wrapper = buildQueryWrapper(queryDTO);
        return costMapper.selectCostList(wrapper);
    }

    @Override
    public void refundCost(CostRefundDTO refundDTO) {
        Cost original = costMapper.selectById(refundDTO.getOriginalCostId());
        if(original == null)
        {
            throw new ResourceNotFoundException("无法找到需要退款的订单");
        }

        if(!Objects.equals(original.getCustomerId(), refundDTO.getCustomerId()))
        {
            throw new BadRequestException("退款人付款人信息不一致");

        }
        //2. 确定退款cost信息
        Cost cost = costConverter.fromRefundDTO(refundDTO);
        LocalDateTime dateTime = LocalDateTime.now();
        cost.setCreatedAt(dateTime);
        cost.setAmount(cost.getAmount());
        cost.setCostType(COST_CONSTANT.TYPE.REFUND.toString());
        cost.setId(UUID.randomUUID().toString());
        if(!save(cost)) {
            throw new ResourceNotFoundException("退款失败");
        }
    }
}
