package rulai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import rulai.entity.OperationLog;

/**
 * <p>
 * 存储系统操作日志，用于追踪用户操作历史 Mapper 接口
 * </p>
 *
 * @author Mistry
 * @since 2026-01-06
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {

}

