package rulai.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import rulai.dto.BundleCreateDTO;
import rulai.dto.BundleModifyDTO;
import rulai.dto.BundleQueryDTO;
import rulai.dto.CostModifyDTO;
import rulai.entity.Bundle;
import com.baomidou.mybatisplus.extension.service.IService;
import rulai.vo.BundleVO;
import rulai.vo.table.BundleTableVO;

import java.util.List;

/**
 * <p>
 * 存储电信套餐信息 服务类
 * </p>
 *
 * @author Mistry
 * @since 2025-12-26
 */
public interface IBundleService extends IService<Bundle> {
    /**
     * 更新
     * @param dto
     */
    Boolean updateBundle(BundleModifyDTO dto);

    /**
     * 创建
     * @param dto
     * @return
     */
    Bundle createBundle(BundleCreateDTO dto);




    /**
     * 删除
     * @param bundleId
     */
    Boolean deleteBundle(String bundleId);

    IPage<BundleTableVO> pageBundles(BundleQueryDTO queryDTO);



    /**
     * 获取BundleList
     * @param queryWrapper
     * @return
     */
    List<BundleVO> getBundleList(QueryWrapper<Bundle> queryWrapper);
}
