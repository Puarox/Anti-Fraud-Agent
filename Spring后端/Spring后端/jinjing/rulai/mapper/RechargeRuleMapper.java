package rulai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import rulai.entity.RechargeRule;

/**
 * <p>
 * 存储电信充值规则 Mapper 接口
 * </p>
 *
 * @author Mistry
 * @since 2025-12-29
 */
@Mapper
public interface RechargeRuleMapper extends BaseMapper<RechargeRule> {

}

