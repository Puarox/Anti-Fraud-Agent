package rulai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import rulai.entity.Node;

/**
 * <p>
 * 服务流程节点表 Mapper 接口
 * </p>
 *
 * @author Mistry
 * @since 2025-12-29
 */
@Mapper
public interface NodeMapper extends BaseMapper<Node> {

}

