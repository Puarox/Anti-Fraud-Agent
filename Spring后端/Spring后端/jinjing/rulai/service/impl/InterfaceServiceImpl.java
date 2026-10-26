package rulai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rulai.annotation.InterpolationInterface;
import rulai.constant.*;
import rulai.entity.*;
import rulai.mapper.CustomerBundleMapper;
import rulai.pojo.Dialogue;
import rulai.pojo.Session;
import rulai.service.*;
import rulai.utils.JSONUtils;
import rulai.utils.RedisUtils;
import rulai.utils.interpolation.InterfaceRegistry;
import rulai.vo.BundleVO;
import rulai.vo.CostVO;
import rulai.vo.CustomerDetailVO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static rulai.utils.interpolation.InterpolationUtil.escapeDoubleBraces;
import static rulai.utils.interpolation.InterpolationUtil.restoreDoubleBraces;

/**
 * <p>
 * 解释器接口服务实现类
 * 语法规范：
 * 使用类似于 @InterpolationInterface(displayName = "会话状态", type = INTERFACE_CONSTANT.TYPE.SESSION) 的注解
 * displayName 为传给前端展示的占位符名
 * type为接口类型
 * type为INTERFACE_CONSTANT.TYPE.SESSION时，入参为 Session, Object ...args
 * type为INTERFACE_CONSTANT.TYPE.LOG时，只应出现在反馈话术中，入参为 Session, OperationLog, Object ...args
 * </p>
 *
 * @author Mistry
 * @since 2025-12-30
 */
@Service
@Slf4j
public class InterfaceServiceImpl implements IInterfaceService {

    @Autowired
    ICustomerService customerService;

    @Autowired
    IUserService userService;

    @Autowired
    JSONUtils jsonUtils;

    @Autowired
    ICostService costService;

    @Autowired
    IRepairmentService repairmentService;

    @Autowired
    IBundleService bundleService;

    @Autowired
    ICustomerBundleService customerBundleService;

    @Autowired
    CustomerBundleMapper customerBundleMapper;

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("(?<!\\{)\\{(?!\\{)([^}]+)\\}(?!\\})");
    @Autowired
    private RedisUtils redisUtils;

    @InterpolationInterface(displayName = "会话状态", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public String getStatus(Session session) {
        return session.getStatus();
    }

    @InterpolationInterface(displayName = "会话ID", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public String getSessionId(Session session) {
        return session.getId();
    }

    @InterpolationInterface(displayName = "客户名", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public String getCustomerName(Session session) {
        Customer customer = customerService.getById(session.getCustomerId());
        return customer.getName();
    }

    @InterpolationInterface(displayName = "客户ID", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public String getCustomerId(Session session) {
        Customer customer = customerService.getById(session.getCustomerId());
        return customer.getId();
    }

    @InterpolationInterface(displayName = "客户手机号", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public String getCustomerPhone(Session session) {
        Customer customer = customerService.getById(session.getCustomerId());
        return customer.getPhone();
    }

    @InterpolationInterface(displayName = "客户状态", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public String getCustomerStatus(Session session) {
        Customer customer = customerService.getById(session.getCustomerId());
        return customer.getPhone();
    }

    @InterpolationInterface(displayName = "客户开户时间", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public LocalDateTime getCustomerCreateTime(Session session) {
        Customer customer = customerService.getById(session.getCustomerId());
        return customer.getCreatedAt();
    }

    @InterpolationInterface(displayName = "客户套参数", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public Integer getCustomerBundleCount(Session session) {
        CustomerDetailVO customer = customerService.getCustomerDetail(session.getCustomerId());
        return customer.getBundleList().size();
    }

    @InterpolationInterface(displayName = "客户话费余额", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public BigDecimal getCustomerBalance(Session session) {
        CustomerDetailVO customer = customerService.getCustomerDetail(session.getCustomerId());
        return customer.getBalance();
    }

    @InterpolationInterface(displayName = "坐席名称", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public String getUserName(Session session) {
        User user = userService.getById(session.getUserId());
        return user.getUsername();
    }

    @InterpolationInterface(value="userEmotion",displayName = "用户情绪分数", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public Integer getUserEmotion(Session session) {
        return RandomUtils.nextInt(0, 100);
    }

    @InterpolationInterface(value="getAsrConfidence",displayName = "通话噪音分数", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public Integer getAsrConfidence(Session session) {
        return RandomUtils.nextInt(0, 100);
    }

    @InterpolationInterface(value="getLastUserUtterance",displayName = "用户上一句话", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public String get(Session session) {
        List<Dialogue> contex = session.getContext();
        if(contex.isEmpty()){
            return "";
        } else {
            return contex.get(contex.size()-1).getContent();
        }
    }

    @InterpolationInterface(value="getSystemBusy",displayName = "坐席是否繁忙", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public Boolean getSystemBusy(Session session) {
        int sessionCount = redisUtils.scanKeysWithPrefix(SESSION_CONSTANT.REDIS_PREFIX).size();
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getStatus, USER_CONSTANT.STATUS.AVAILABLE.name());
        List<User> userList= userService.list(queryWrapper);
        return sessionCount > userList.size();
    }

    @InterpolationInterface(value="getCustomerType",displayName = "客户类型", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public String  getCustomerType(Session session) {
        Customer customer = customerService.getById(session.getCustomerId());
        return customer.getCustomerType();
    }

    @InterpolationInterface(value="getMonthlyConsumption",displayName = "客户月消费", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public BigDecimal getMonthlyConsumption(Session session) {
        CustomerDetailVO customer = customerService.getCustomerDetail(session.getCustomerId());
        BigDecimal tot = BigDecimal.valueOf(0.00);
        for(CostVO bundle : customer.getCostList()){
            tot = tot.add(bundle.getAmount());
        }
        return tot;
    }

    @InterpolationInterface(value="getCustomerGender",displayName = "客户性别", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public String getCustomerGender(Session session) {
        System.out.println("session = " + session);
        System.out.println("session customer Id = " + session.getCustomerId());
        Customer customer = customerService.getById(session.getCustomerId());
        System.out.println("customer = " + customer);
        return customer.getGender();
    }

    @InterpolationInterface(value="getBundleCount",displayName = "套餐数量", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public Integer getBundleCount(Session session) {
        CustomerDetailVO customer = customerService.getCustomerDetail(session.getCustomerId());
        return customer.getBundleList().size();
    }

    @InterpolationInterface(value="getTotalCost",displayName = "历史总消费", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public BigDecimal getTotalCost(Session session) {
        CustomerDetailVO customer = customerService.getCustomerDetail(session.getCustomerId());
        BigDecimal tot = BigDecimal.valueOf(0.00);
        for(CostVO bundle : customer.getCostList()){
            tot = tot.add(bundle.getAmount());
        }
        return tot;
    }

    @InterpolationInterface(value="getTotalConsumption",displayName = "历史总消费", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public BigDecimal getTotalConsumption(Session session) {
        return getTotalCost(session);
    }

    @InterpolationInterface(value="getRemainingData",displayName = "剩余流量", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public Double getDataAmount(Session session) {
        CustomerDetailVO customer = customerService.getCustomerDetail(session.getCustomerId());
        double dataAmount = 0.0;
        for(BundleVO bundle : customer.getBundleList()){
            dataAmount += bundle.getDataLimit().doubleValue();
        }
        return dataAmount;
    }

    @InterpolationInterface(value="getRemainingMinutes",displayName = "通话时长", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public Double getCallingMinutes(Session session) {
        CustomerDetailVO customer = customerService.getCustomerDetail(session.getCustomerId());
        double callingMinutes = 0.0;
        for(BundleVO bundle : customer.getBundleList()){
            callingMinutes += bundle.getCallMinutes().doubleValue();
        }
        return callingMinutes;
    }

    @InterpolationInterface(value="getTotalMonthCost",displayName = "月总消费", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public BigDecimal getTotalMonthCost(Session session) {
        LambdaQueryWrapper<Cost> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(Cost::getCreatedAt, LocalDateTime.now().minusDays(1));
        List<Cost> costList = costService.list(queryWrapper);
        BigDecimal tot = BigDecimal.valueOf(0.00);
        for (Cost cost : costList) {
            tot = tot.add(cost.getAmount());
        }
        return tot;
    }

    @InterpolationInterface(value="getRepairCount",displayName = "客户工单数", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public Long getRepairCount(Session session) {
        LambdaQueryWrapper<Repairment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Repairment::getCustomerId, session.getCustomerId());
        return repairmentService.count(queryWrapper);
    }

    @InterpolationInterface(value="getProcessingRepairCount",displayName = "处理中客户工单数", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public Long getProcessingRepairCount(Session session) {
        LambdaQueryWrapper<Repairment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Repairment::getCustomerId, session.getCustomerId()).eq(Repairment::getStatus, REPAIRMENT_CONSTANT.STATUS.PROCESSING);
        return repairmentService.count(queryWrapper);
    }

    @InterpolationInterface(value="getProcessingOrderCount",displayName = "处理中客户工单数", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public Long getProcessingOrderCount(Session session) {
        return getProcessingRepairCount(session);
    }

    @InterpolationInterface(value="getRepairOrderCount",displayName = "处理中客户工单数", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public Long getRepairOrderCount(Session session) {
        return getProcessingRepairCount(session);
    }

    @InterpolationInterface(value="getLatestRepairStatus",displayName = "最新维修", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public String getLatestRepairStatus(Session session) {
        LambdaQueryWrapper<Repairment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Repairment::getCustomerId, session.getCustomerId());
        List<Repairment> repairmentList=repairmentService.list(queryWrapper);
        repairmentList.sort(Comparator.comparing(Repairment::getUpdatedAt).reversed());
        if(!repairmentList.isEmpty()){
            return repairmentList.get(0).getStatus();
        } else {
            return "";
        }
    }

    @InterpolationInterface(value="getAvailableBundleCount",displayName = "可办套餐数", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public Long getAvailableBundleCount(Session session) {
        LambdaQueryWrapper<Bundle> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Bundle::getState, BUNDLE_CONSTANT.STATUS.ACTIVE);
        return bundleService.count(queryWrapper);
    }

    @InterpolationInterface(value="getCheapestBundlePrice",displayName = "最便宜的套餐", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public BigDecimal getCheapestBundlePrice(Session session) {
        LambdaQueryWrapper<Bundle> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Bundle::getState, BUNDLE_CONSTANT.STATUS.ACTIVE);

        List<Bundle> bundleList=bundleService.list(queryWrapper);
        bundleList.sort(Comparator.comparing(Bundle::getPrice));
        return bundleList.get(0).getPrice();
    }

    @InterpolationInterface(value="getMostPopularBundleName",displayName = "最受欢迎的套餐", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public String getMostPopularBundleName(Session session) {
        return customerBundleMapper.getMostPopularBundleName();
    }

    // 接口：{getBundleName}(获取当前套餐名称) 不可实现，因为一个用户会有多个套餐
    // 接口：{getBundleCost}(获取当前套餐月费) 不可实现，因为一个用户会有多个套餐
    // 接口：{getBundleEndDate}(获取合约结束日期) 不可实现，因为一个用户会有多个套餐

    @InterpolationInterface(value="getRefundableAmount",displayName = "可退款总额", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public BigDecimal getRefundableAmount(Session session) {
        LambdaQueryWrapper<Cost> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Cost::getCustomerId, session.getCustomerId());
        queryWrapper.ge(Cost::getPaidAt, LocalDateTime.now().minusMonths(3));
        BigDecimal tot = BigDecimal.valueOf(0.00);
        for (Cost cost : costService.list(queryWrapper)) {
            tot = tot.add(cost.getAmount());
        }
        return tot;
    }

    @InterpolationInterface(displayName = "会话开始时间", type = INTERFACE_CONSTANT.TYPE.LOG)
    public LocalDateTime getSessionStartTime(Session session, OperationLog log) throws JsonProcessingException {
        System.out.println("logRespond = " + log.getRespond());
        Session createdSession = jsonUtils.fromJSONString(log.getRespond(), Session.class);
        System.out.println("createdSession = " + createdSession);
        return createdSession.getStartTime();
    }

    @InterpolationInterface(value="bundleName", displayName = "套餐名", type = INTERFACE_CONSTANT.TYPE.LOG)
    public String getBundleName(Session session, OperationLog log) throws JsonProcessingException {
        Bundle bundle = jsonUtils.fromJSONString(log.getRespond(), Bundle.class);
        return bundle.getName();
    }

    @InterpolationInterface(value="getUsername", displayName = "坐席用户名", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public String getUsername(Session session) {
        return getUserName(session);
    }

    @InterpolationInterface(value="userGender", displayName = "用户性别", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public String getUserGender(Session session) {
        return getCustomerGender(session);
    }

    @InterpolationInterface(value="customerBundleCount", displayName = "客户套餐数量", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public Integer getCustomerBundleCountSession(Session session) {
        return getCustomerBundleCount(session);
    }

    @InterpolationInterface(value="customerStatus", displayName = "客户状态", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public String getCustomerStatusSession(Session session) {
        Customer customer = customerService.getById(session.getCustomerId());
        return customer.getStatus();
    }

    @InterpolationInterface(value="bundleStatus", displayName = "套餐状态", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public String getBundleStatus(Session session) {
        CustomerDetailVO customer = customerService.getCustomerDetail(session.getCustomerId());
        if (customer.getBundleList() != null && !customer.getBundleList().isEmpty()) {
            return customer.getBundleList().get(0).getState();
        }
        return "无套餐";
    }

    @InterpolationInterface(value="bundleStartDate", displayName = "套餐开始日期", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public LocalDateTime getBundleStartDate(Session session, OperationLog log) throws JsonProcessingException {
        CustomerBundle cb = jsonUtils.fromJSONString(log.getRespond(), CustomerBundle.class);
        return cb.getCreatedAt();
    }

    @InterpolationInterface(value="bundleEndDate", displayName = "套餐结束日期", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public LocalDateTime getBundleEndDate(Session session, OperationLog log) throws JsonProcessingException {
        CustomerBundle cb = jsonUtils.fromJSONString(log.getRespond(), CustomerBundle.class);
        return cb.getEndDate().atStartOfDay();
    }

    @InterpolationInterface(value="createRepairmentSuccess", displayName = "创建维修单是否成功", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public Boolean getCreateRepairmentSuccess(Session session, OperationLog log) {
        return RandomUtils.nextBoolean();
    }

    @InterpolationInterface(value="repairmentExist", displayName = "维修单是否存在", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public Boolean getRepairmentExist(Session session) {
        LambdaQueryWrapper<Repairment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Repairment::getCustomerId, session.getCustomerId());
        return repairmentService.count(queryWrapper) > 0;
    }

    @InterpolationInterface(value="repairmentCount", displayName = "维修单数量", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public Long getRepairmentCountSession(Session session) {
        return getRepairCount(session);
    }

    @InterpolationInterface(value="costAmount", displayName = "费用金额", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public BigDecimal getCostAmount(Session session, OperationLog log) throws JsonProcessingException {
        Cost cost = jsonUtils.fromJSONString(log.getRespond(), Cost.class);
        return cost != null ? cost.getAmount() : BigDecimal.ZERO;
    }

    @InterpolationInterface(value="costType", displayName = "费用类型", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public String getCostType(Session session, OperationLog log) throws JsonProcessingException {
        Cost cost = jsonUtils.fromJSONString(log.getRespond(), Cost.class);
        return cost != null ? cost.getCostType() : "未知";
    }

    @InterpolationInterface(value="costTime", displayName = "费用时间", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public LocalDateTime getCostTime(Session session, OperationLog log) throws JsonProcessingException {
        Cost cost = jsonUtils.fromJSONString(log.getRespond(), Cost.class);
        return cost != null ? cost.getCreatedAt() : LocalDateTime.now();
    }

    @InterpolationInterface(value="name", displayName = "客户姓名", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public String getName(Session session) {
        return getCustomerName(session);
    }

    @InterpolationInterface(value="phone", displayName = "客户电话", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public String getPhone(Session session) {
        return getCustomerPhone(session);
    }

    @InterpolationInterface(value="status", displayName = "客户状态", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public String getStatusSession(Session session) {
        Customer customer = customerService.getById(session.getCustomerId());
        return customer.getStatus();
    }

    @InterpolationInterface(value="customerType", displayName = "客户类型", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public String getCustomerTypeSession(Session session) {
        return getCustomerType(session);
    }

    @InterpolationInterface(value="balance", displayName = "账户余额", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public BigDecimal getBalance(Session session) {
        return getCustomerBalance(session);
    }

    @InterpolationInterface(value="currentMonthCost", displayName = "本月消费", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public BigDecimal getCurrentMonthCost(Session session) {
        return getMonthlyConsumption(session);
    }

    @InterpolationInterface(value="unpaidBillAmount", displayName = "未付账单金额", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public BigDecimal getUnpaidBillAmount(Session session) {
        LambdaQueryWrapper<Cost> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Cost::getCustomerId, session.getCustomerId())
                .eq(Cost::getStatus, "UNPAID");
        BigDecimal total = BigDecimal.ZERO;
        for (Cost cost : costService.list(queryWrapper)) {
            total = total.add(cost.getAmount());
        }
        return total;
    }

    @InterpolationInterface(value="totalBundleCount", displayName = "套餐总数", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public Integer getTotalBundleCount(Session session) {
        return getBundleCount(session);
    }

    @InterpolationInterface(value="totalBundleCost", displayName = "套餐总费用", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public BigDecimal getTotalBundleCost(Session session) {
        CustomerDetailVO customer = customerService.getCustomerDetail(session.getCustomerId());
        BigDecimal total = BigDecimal.ZERO;
        for (BundleVO bundle : customer.getBundleList()) {
            total = total.add(bundle.getPrice());
        }
        return total;
    }

    @InterpolationInterface(value="totalCallCount", displayName = "总通话时长", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public Double getTotalCallCount(Session session) {
        return getCallingMinutes(session);
    }

    @InterpolationInterface(value="totalSmsCount", displayName = "总短信条数", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public Integer getTotalSmsCount(Session session) {
        CustomerDetailVO customer = customerService.getCustomerDetail(session.getCustomerId());
        int totalSms = 0;
        for (BundleVO bundle : customer.getBundleList()) {
            totalSms += bundle.getSmsCount();
        }
        return totalSms;
    }

    @InterpolationInterface(value="totalDataCount", displayName = "总流量", type = INTERFACE_CONSTANT.TYPE.SESSION)
    public Double getTotalDataCount(Session session) {
        return getDataAmount(session);
    }

    @InterpolationInterface(value="repairmentTitle", displayName = "维修标题", type = INTERFACE_CONSTANT.TYPE.LOG)
    public String getRepairmentTitle(Session session, OperationLog log) throws JsonProcessingException {
        Repairment repairment = jsonUtils.fromJSONString(log.getRespond(), Repairment.class);
        return repairment.getTitle();
    }

    @InterpolationInterface(value="appointmentTime", displayName = "预约时间", type = INTERFACE_CONSTANT.TYPE.LOG)
    public Date getAppointmentTime(Session session, OperationLog log) throws JsonProcessingException {
        Repairment repairment = jsonUtils.fromJSONString(log.getRespond(), Repairment.class);
        return repairment.getAppointmentTime();
    }

    @InterpolationInterface(value="repairmentStatus", displayName = "维修状态", type = INTERFACE_CONSTANT.TYPE.LOG)
    public String getRepairmentStatus(Session session, OperationLog log) throws JsonProcessingException {
        Repairment repairment = jsonUtils.fromJSONString(log.getRespond(), Repairment.class);
        return repairment.getStatus();
    }

    @InterpolationInterface(value="repairmentResult", displayName = "维修结果", type = INTERFACE_CONSTANT.TYPE.LOG)
    public String getRepairmentResult(Session session, OperationLog log) throws JsonProcessingException {
        Repairment repairment = jsonUtils.fromJSONString(log.getRespond(), Repairment.class);
        return repairment.getResult() != null ? repairment.getResult() : "暂无结果";
    }

    @InterpolationInterface(value="costCount", displayName = "费用数量", type = INTERFACE_CONSTANT.TYPE.LOG)
    public Integer getCostCount(Session session, OperationLog log) throws JsonProcessingException {
        List<Cost> costList = jsonUtils.fromJSONString(log.getRespond(), List.class);
        return costList != null ? costList.size() : 0;
    }

    @InterpolationInterface(value="refundAmount", displayName = "退款金额", type = INTERFACE_CONSTANT.TYPE.LOG)
    public BigDecimal getRefundAmount(Session session, OperationLog log) throws JsonProcessingException {
        Cost cost = jsonUtils.fromJSONString(log.getRespond(), Cost.class);
        return cost != null ? cost.getAmount() : BigDecimal.ZERO;
    }

    // 其他需要的接口方法
    @InterpolationInterface(value="bundleDescription", displayName = "套餐描述", type = INTERFACE_CONSTANT.TYPE.LOG)
    public String getBundleDescription(Session session, OperationLog log) throws JsonProcessingException {
        Bundle bundle = jsonUtils.fromJSONString(log.getRespond(), Bundle.class);
        return bundle.getDescription();
    }

    @InterpolationInterface(value="monthlyPrice", displayName = "月费", type = INTERFACE_CONSTANT.TYPE.LOG)
    public BigDecimal getMonthlyPrice(Session session, OperationLog log) throws JsonProcessingException {
        Bundle bundle = jsonUtils.fromJSONString(log.getRespond(), Bundle.class);
        return bundle.getPrice();
    }

    @InterpolationInterface(value="dataLimitGB", displayName = "流量限制(GB)", type = INTERFACE_CONSTANT.TYPE.LOG)
    public Double getDataLimitGB(Session session, OperationLog log) throws JsonProcessingException {
        Bundle bundle = jsonUtils.fromJSONString(log.getRespond(), Bundle.class);
        return bundle.getDataLimit().doubleValue();
    }

    @InterpolationInterface(value="callMinutes", displayName = "通话分钟数", type = INTERFACE_CONSTANT.TYPE.LOG)
    public Integer getCallMinutes(Session session, OperationLog log) throws JsonProcessingException {
        Bundle bundle = jsonUtils.fromJSONString(log.getRespond(), Bundle.class);
        return bundle.getCallMinutes();
    }

    @InterpolationInterface(value="smsCount", displayName = "短信条数", type = INTERFACE_CONSTANT.TYPE.LOG)
    public Integer getSmsCount(Session session, OperationLog log) throws JsonProcessingException {
        Bundle bundle = jsonUtils.fromJSONString(log.getRespond(), Bundle.class);
        return bundle.getSmsCount();
    }

    @InterpolationInterface(value="validityDays", displayName = "有效期天数", type = INTERFACE_CONSTANT.TYPE.LOG)
    public Integer getValidityDays(Session session, OperationLog log) throws JsonProcessingException {
        Bundle bundle = jsonUtils.fromJSONString(log.getRespond(), Bundle.class);
        return bundle.getValidityDays();
    }

    @InterpolationInterface(value="bundleState", displayName = "套餐状态", type = INTERFACE_CONSTANT.TYPE.LOG)
    public String getBundleState(Session session, OperationLog log) throws JsonProcessingException {
        Bundle bundle = jsonUtils.fromJSONString(log.getRespond(), Bundle.class);
        return bundle.getState();
    }



    @Override
    public Object call(String interfaceName, Object ...arg) {
        Function<Object[], Object> function = InterfaceRegistry.getInterface(interfaceName);
        if (function == null) {
            throw new RuntimeException("未找到注册的接口: " + interfaceName);
        }

        try {
            return function.apply(arg);
        } catch (Exception e) {
            throw new RuntimeException("执行接口调用失败: " + interfaceName, e);
        }
    }


    /**
     * 解析并替换字符串中的占位符
     *
     * @param template 模板字符串，如 "你好, {getCustomerName}先生。"
     * @param arg 抽象入参
     * @return 替换后的字符串
     */
    @Override
    public String resolve(String template, Object ...arg) {
        if (template == null || template.trim().isEmpty()) {
            return template;
        }

        String escaped = escapeDoubleBraces(template);

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(escaped);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String interfaceName = matcher.group(1).trim();
            String replacement;

            try {
                Object resultObj = call(interfaceName, arg);
                replacement = resultObj != null ? resultObj.toString() : "";
            } catch (Exception e) {
                log.error("调用接口 {} 失败: {}", interfaceName, e.getMessage(), e);
                replacement = matcher.group(0);
            }

            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(result);
        return restoreDoubleBraces(result.toString());
    }

    /**
     * 解析并替换字符串中的占位符
     *
     * @param template 模板字符串，如 "你好, {getCustomerName}先生。"
     * @return 替换后的字符串
     */
    @Override
    public String check(String template) {
        if (template == null || template.trim().isEmpty()) {
            return template;
        }

        String escaped = escapeDoubleBraces(template);

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(escaped);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String interfaceName = matcher.group(1).trim();
            if(!InterfaceRegistry.isRegistered(interfaceName)){
                result.append(String.format("接口 {%s} 无效\n",  interfaceName));
            }
        }

        return result.toString();
    }
}