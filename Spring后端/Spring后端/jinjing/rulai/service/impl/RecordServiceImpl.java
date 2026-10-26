package rulai.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import rulai.convertor.IRecordConverter;
import rulai.entity.Record;
import rulai.mapper.RecordMapper;
import rulai.service.IOperationLogService;
import rulai.service.IRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import rulai.vo.RecordDetailVO;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * <p>
 * 坐席与客户的通话记录和通话过程操作轨迹 服务实现类
 * </p>
 *
 * @author Mistry
 * @since 2025-12-26
 */
@Service
public class RecordServiceImpl extends ServiceImpl<RecordMapper, Record> implements IRecordService {

    @Autowired
    IRecordConverter recordConverter;

    @Autowired
    IOperationLogService operationLogService;

    @Override
    public RecordDetailVO getRecord(String id) {
        Record record = getById(id);
        RecordDetailVO recordDetailVO = recordConverter.toVO(record);
        if(record.getOperations()!=null){
            recordDetailVO.setOperationLogs(record.getOperations().stream().map(
                    operationId -> operationLogService.getById(operationId)
            ).collect(Collectors.toList()));
        } else {
            recordDetailVO.setOperationLogs(new ArrayList<>());
        }
        return recordDetailVO;
    }
}
