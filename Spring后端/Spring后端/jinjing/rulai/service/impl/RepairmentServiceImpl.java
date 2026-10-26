package rulai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import rulai.convertor.RepairmentConverterImpl;
import rulai.dto.CostQueryDTO;
import rulai.dto.RepairmentCreateDTO;
import rulai.dto.RepairmentQueryDTO;
import rulai.entity.Repairment;
import rulai.httpstatus.exceptions.ResourceNotFoundException;
import rulai.service.IRepairmentService;
import rulai.mapper.RepairmentMapper;
import org.springframework.stereotype.Service;
import rulai.vo.table.CostTableVO;
import rulai.vo.table.RepairmentTableVO;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
* @author 22076
* @description 针对表【repairment(维修订单信息)】的数据库操作Service实现
* @createDate 2026-01-05 10:12:45
*/
@Service
public class RepairmentServiceImpl extends ServiceImpl<RepairmentMapper, Repairment>
    implements IRepairmentService{

    @Autowired
    private RepairmentMapper repairmentMapper;
    @Autowired
    private RepairmentConverterImpl repairmentConverterImpl;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Repairment createRepairment(RepairmentCreateDTO dto) {

        Repairment repairment = repairmentConverterImpl.fromCreateDTO(dto);
        LocalDateTime dateTime = LocalDateTime.now();
        repairment.setCreatedAt(dateTime);
        repairment.setId(UUID.randomUUID().toString());
        if(save(repairment)){
            return repairment;
        }
        else{
            throw new ResourceNotFoundException("创建维修工单失败");
        }

    }

    @Override
    public List<RepairmentTableVO> listRepairments(RepairmentQueryDTO queryDTO) {
        QueryWrapper<RepairmentTableVO> wrapper = buildQueryWrapper(queryDTO);
        return repairmentMapper.selectRepairmentList(wrapper);
    }

    @Override
    public IPage<RepairmentTableVO> pageRepairment(RepairmentQueryDTO queryDTO) {
        Page<RepairmentTableVO> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        QueryWrapper<RepairmentTableVO> wrapper = buildQueryWrapper(queryDTO);
        return repairmentMapper.selectRepairmentPage(page, wrapper);
    }

    /**
     * 构建查询条件
     */
    private QueryWrapper<RepairmentTableVO> buildQueryWrapper(RepairmentQueryDTO queryDTO) {
        QueryWrapper<RepairmentTableVO> wrapper = new QueryWrapper<>();

        // 1. 精确查询条件
        if (StringUtils.isNotBlank(queryDTO.getId())) {
            wrapper.eq("r.id", queryDTO.getId());
        }

        if (StringUtils.isNotBlank(queryDTO.getCustomerId())) {
            wrapper.eq("r.customer_id", queryDTO.getCustomerId());
        }

        if (StringUtils.isNotBlank(queryDTO.getUserId())) {
            wrapper.eq("r.user_id", queryDTO.getUserId());
        }

        if (StringUtils.isNotBlank(queryDTO.getStatus())) {
            wrapper.eq("r.status", queryDTO.getStatus());
        }

        if (StringUtils.isNotBlank(queryDTO.getPriority())) {
            wrapper.eq("r.priority", queryDTO.getPriority());
        }

        // 2. 模糊查询条件
        if (StringUtils.isNotBlank(queryDTO.getTitle())) {
            wrapper.like("r.title", queryDTO.getTitle());
        }

        if (StringUtils.isNotBlank(queryDTO.getDescription())) {
            wrapper.like("r.description", queryDTO.getDescription());
        }

        if (StringUtils.isNotBlank(queryDTO.getAddress())) {
            wrapper.like("r.address", queryDTO.getAddress());
        }

        if (StringUtils.isNotBlank(queryDTO.getContactPhone())) {
            wrapper.like("r.contact_phone", queryDTO.getContactPhone());
        }

        if (StringUtils.isNotBlank(queryDTO.getResult())) {
            wrapper.like("r.result", queryDTO.getResult());
        }

        // 3. 单个时间查询条件（你的 DTO 中没有时间范围字段）
        if (queryDTO.getAppointmentTime() != null) {
            wrapper.apply("DATE(r.appointment_time) = DATE({0})", queryDTO.getAppointmentTime());
        }

        if (queryDTO.getCompletedAt() != null) {
            wrapper.apply("DATE(r.completed_at) = DATE({0})", queryDTO.getCompletedAt());
        }

        if (queryDTO.getCreatedAt() != null) {
            wrapper.apply("DATE(r.created_at) = DATE({0})", queryDTO.getCreatedAt());
        }

        if (queryDTO.getUpdatedAt() != null) {
            wrapper.apply("DATE(r.updated_at) = DATE({0})", queryDTO.getUpdatedAt());
        }
        // 确保至少有 WHERE 条件，避免只有 ORDER BY
        if (wrapper.isEmptyOfWhere()) {
            wrapper.apply("1=1");
        }
        // 9. 排序逻辑
        applySorting(wrapper, queryDTO);

        return wrapper;
    }

    /**
     * 应用排序规则
     */
    private void applySorting(QueryWrapper<RepairmentTableVO> wrapper, RepairmentQueryDTO queryDTO) {
        if (StringUtils.isNotBlank(queryDTO.getSortField())) {
            boolean isAsc = "asc".equalsIgnoreCase(queryDTO.getSortOrder());
            wrapper.orderBy(true, isAsc, queryDTO.getSortField());
        }
    }
}




