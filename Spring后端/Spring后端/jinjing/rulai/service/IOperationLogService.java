package rulai.service;

import rulai.entity.OperationLog;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 存储系统操作日志，用于追踪用户操作历史 服务类
 * </p>
 *
 * @author Mistry
 * @since 2026-01-06
 */
public interface IOperationLogService extends IService<OperationLog> {
    public void saveAsync(OperationLog operationLog);
}
