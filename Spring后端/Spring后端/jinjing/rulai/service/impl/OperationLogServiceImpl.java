package rulai.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import rulai.entity.OperationLog;
import rulai.mapper.OperationLogMapper;
import rulai.service.IOperationLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 存储系统操作日志，用于追踪用户操作历史 服务实现类
 * </p>
 *
 * @author Mistry
 * @since 2026-01-06
 */
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements IOperationLogService {

    @Autowired
    private OperationLogMapper operationLogMapper;

    /**
     * 异步保存操作日志
     */
    @Async("logExecutor")
    @Override
    public void saveAsync(OperationLog operationLog) {
        save(operationLog);
    }
}
