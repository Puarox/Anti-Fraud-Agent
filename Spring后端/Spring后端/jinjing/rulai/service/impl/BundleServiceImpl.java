package rulai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import rulai.convertor.IBundleConverter;

import rulai.dto.*;
import rulai.entity.Bundle;
import rulai.entity.CustomerBundle;
import rulai.httpstatus.exceptions.BadRequestException;
import rulai.httpstatus.exceptions.ResourceNotFoundException;
import rulai.mapper.BundleMapper;
import rulai.mapper.UserMapper;
import rulai.service.IBundleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import rulai.utils.ReflectionUtils;
import rulai.vo.BundleVO;
import rulai.vo.table.BundleTableVO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * <p>
 * 存储电信套餐信息 服务实现类
 * </p>
 *
 * @author Mistry
 * @since 2025-12-26
 */
@Service
@Slf4j
public class BundleServiceImpl extends ServiceImpl<BundleMapper, Bundle> implements IBundleService {

    @Autowired
    private BundleMapper bundleMapper;

    @Autowired
    private CustomerBundleServiceImpl  customerBundleService;

    @Autowired
    private IBundleConverter bundleConverter;
    @Autowired
    private UserMapper userMapper;

    /**
     * Bundle 转换为 BundleVO
     */
    public BundleVO convertToBundleVO(Bundle bundle, CustomerBundle customerBundle) {
        BundleVO vo = new BundleVO();
        vo.setId(bundle.getId());
        vo.setName(bundle.getName());
        vo.setDescription(bundle.getDescription());
        vo.setPrice(bundle.getPrice());
        vo.setDataLimit(bundle.getDataLimit());
        vo.setCallMinutes(bundle.getCallMinutes());
        vo.setSmsCount(bundle.getSmsCount());
        vo.setValidityDays(bundle.getValidityDays());
        vo.setState(bundle.getState());
        return vo;
    }



    @Override
    public Bundle createBundle(BundleCreateDTO dto) {
        Bundle bundle = bundleConverter.fromCreateDTO(dto);
        LocalDateTime dateTime = LocalDateTime.now();
        bundle.setCreatedAt(dateTime);
        bundle.setId(UUID.randomUUID().toString());
        if(save(bundle)) {
            return getById(bundle.getId());
        } else {
            throw new ResourceNotFoundException("新建失败");
        }
    }
    @Override
    public Boolean updateBundle(BundleModifyDTO dto) {
        Bundle modified = bundleConverter.fromModifyDTO(dto);
        Bundle bundle = getById(modified.getId());

        if(bundle == null) {
            throw new ResourceNotFoundException("目标费用不存在");
        } else {
            ReflectionUtils.update(bundle, modified);
        }
        updateById(bundle);
        return true;
    }

    @Override
    public Boolean deleteBundle(String bundleId) {
        //1 获取验证这个bundle
        Bundle old = bundleMapper.selectById(bundleId);
        if(old == null) {throw new ResourceNotFoundException("找不到该套餐");}
        //2 检验这个bundle是否还有外键关联
        Boolean isAssociated = customerBundleService.checkBundleForeignKey(bundleId);
        if(isAssociated) { throw new BadRequestException("有用户订阅改套餐，不能删除");}
        //3 删除这个bundle
        bundleMapper.deleteById(old);
        return true;
    }

    @Override
    public List<BundleVO> getBundleList(QueryWrapper<Bundle> queryWrapper) {
        List<Bundle> list = baseMapper.selectList(queryWrapper);
        List<BundleVO> voList = new ArrayList<>();
        for (Bundle bundle : list) {
            voList.add(convertToBundleVO(bundle,null));
        }
        return voList;
    }

    private QueryWrapper<BundleTableVO> buildQueryWrapper(BundleQueryDTO queryDTO) {
        QueryWrapper<BundleTableVO> wrapper = new QueryWrapper<>();

        // 1. 精确查询条件
        if (StringUtils.isNotBlank(queryDTO.getId())) {
            wrapper.eq("bundle.id", queryDTO.getId());
        }

        if (StringUtils.isNotBlank(queryDTO.getName())) {
            wrapper.like("bundle.name", queryDTO.getName());
        }

        if (StringUtils.isNotBlank(queryDTO.getState())) {
            wrapper.eq("bundle.state", queryDTO.getState());
        }

        // 7. 有效期天数查询
        if (queryDTO.getValidityDays() != null) {
            wrapper.eq("bundle.validity_days", queryDTO.getValidityDays());
        }
        // 确保至少有 WHERE 条件，避免只有 ORDER BY
        if (wrapper.isEmptyOfWhere()) {
            wrapper.apply("1=1");
        }
        // 9. 排序逻辑
        applySorting(wrapper, queryDTO);

        return wrapper;
    }

    public List<BundleTableVO> listBundles(BundleQueryDTO queryDTO) {
        QueryWrapper<BundleTableVO> wrapper = buildQueryWrapper(queryDTO);
        return bundleMapper.selectBundleList(wrapper);
    }
    @Override
    public IPage<BundleTableVO> pageBundles(BundleQueryDTO queryDTO) {
        Page<BundleTableVO> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        QueryWrapper<BundleTableVO> wrapper = buildQueryWrapper(queryDTO);
        return bundleMapper.selectBundlePage(page, wrapper);
    }

    public List<BundleTableVO> listActiveBundles(BundleQueryDTO queryDTO) {
        QueryWrapper<BundleTableVO> wrapper = buildQueryWrapper(queryDTO);
        return bundleMapper.selectActiveBundleList(wrapper);
    }

    public BundleTableVO getBundleById(String id) {
        return bundleMapper.selectBundleById(id);
    }

    public Integer countBundles(BundleQueryDTO queryDTO) {
        QueryWrapper<BundleTableVO> wrapper = buildQueryWrapper(queryDTO);
        return bundleMapper.countBundles(wrapper);
    }

    private void applySorting(QueryWrapper<BundleTableVO> wrapper, BundleQueryDTO queryDTO) {
        if (StringUtils.isNotBlank(queryDTO.getSortField())) {
            String order = StringUtils.isNotBlank(queryDTO.getSortOrder()) ? queryDTO.getSortOrder() : "asc";
            wrapper.orderBy(true, "asc".equalsIgnoreCase(order),
                    "bundle." + StringUtils.camelToUnderline(queryDTO.getSortField()));
        }
        // 如果没有排序字段，就不添加 ORDER BY
    }

}
