package rulai.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import rulai.dto.CustomerBundleQueryDTO;
import rulai.entity.CustomerBundle;
import rulai.vo.table.CustomerBundleTableVO;

import java.util.List;

/**
 * <p>
 * 客户与套餐的订阅关系 Mapper 接口
 * </p>
 *
 * @author Mistry
 * @since 2025-12-29
 */
@Mapper
public interface CustomerBundleMapper extends BaseMapper<CustomerBundle> {
    /**
     * 分页查询客户套餐关系
     */
    IPage<CustomerBundleTableVO> selectCustomerBundlePage(
            IPage<CustomerBundleTableVO> page,
            @Param("ew") Wrapper<CustomerBundleTableVO> wrapper
    );

    /**
     * 查询客户的套餐列表
     */
    List<CustomerBundleTableVO> selectCustomerBundles(@Param("ew") Wrapper<CustomerBundleTableVO> wrapper);

    /**
     * 获取最受欢迎的套餐名称（按关联数量排序）
     * @return 套餐名称
     */
    @Select("SELECT b.name FROM bundle b " +
            "JOIN (SELECT bundle_id, COUNT(*) as cnt FROM customer_bundle " +
            "      WHERE status = 'ACTIVE' " +  // 可选的过滤条件
            "      GROUP BY bundle_id " +
            "      ORDER BY cnt DESC " +
            "      LIMIT 1) cb_stats ON b.id = cb_stats.bundle_id")
    String getMostPopularBundleName();
}

