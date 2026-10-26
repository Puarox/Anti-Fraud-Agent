package rulai.service;

import rulai.entity.Record;
import com.baomidou.mybatisplus.extension.service.IService;
import rulai.vo.RecordDetailVO;

/**
 * <p>
 * 坐席与客户的通话记录和通话过程操作轨迹 服务类
 * </p>
 *
 * @author Mistry
 * @since 2025-12-26
 */
public interface IRecordService extends IService<Record> {
    public RecordDetailVO getRecord(String id);
}
