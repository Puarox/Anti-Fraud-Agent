package rulai.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import rulai.entity.Bundle;
import rulai.vo.table.BundleTableVO;
import rulai.vo.table.CostTableVO;

import java.util.List;

/**
 * <p>
 * 存储电信套餐信息 Mapper 接口
 * </p>
 *
 * @author Mistry
 * @since 2025-12-29
 */
@Mapper
public interface BundleMapper extends BaseMapper<Bundle> {

    List<BundleTableVO> selectBundleList(@Param("ew")QueryWrapper<BundleTableVO> wrapper);

    IPage<BundleTableVO> selectBundlePage(@Param("page")Page<BundleTableVO> page, @Param("ew")QueryWrapper<BundleTableVO> wrapper);

    BundleTableVO selectBundleById(String id);

    List<BundleTableVO> selectActiveBundleList(@Param("ew")QueryWrapper<BundleTableVO> wrapper);

    Integer countBundles(@Param("ew")QueryWrapper<BundleTableVO> wrapper);
}

