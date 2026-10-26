package rulai.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import rulai.entity.Cost;
import rulai.vo.table.CostTableVO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 客户费用账单记录 Mapper 接口
 * </p>
 *
 * @author Mistry
 * @since 2025-12-29
 */
@Mapper
public interface CostMapper extends BaseMapper<Cost> {
    /**
     * 分页查询费用记录（带客户信息）
     */
    IPage<CostTableVO> selectCostPage(
            IPage<CostTableVO> page,
            @Param("ew") Wrapper<CostTableVO> wrapper
    );

    /**
     * 查询费用列表（不分页）
     */
    List<CostTableVO> selectCostList(
            @Param("ew") Wrapper<CostTableVO> wrapper
    );

    /**
     * 根据ID查询费用详情
     */
    CostTableVO selectCostDetail(String costId);

    /**
     * 查询客户费用统计
     */



}

