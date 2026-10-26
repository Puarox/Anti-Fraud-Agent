package rulai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tencentcloudapi.hunyuan.v20230901.models.History;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import rulai.dto.HistoryCreateDTO;
import rulai.entity.AnalysisHistory;
import rulai.service.AnalysisHistoryService;
import rulai.mapper.AnalysisHistoryMapper;
import org.springframework.stereotype.Service;

/**
* @author 22076
* @description 针对表【analysis_history】的数据库操作Service实现
* @createDate 2026-03-16 11:49:21
*/
@Service
public class AnalysisHistoryServiceImpl extends ServiceImpl<AnalysisHistoryMapper, AnalysisHistory>
    implements AnalysisHistoryService{

    @Autowired
    private AnalysisHistoryMapper analysisHistoryMapper;
    @Override
    public void createHistory(HistoryCreateDTO history) {
        AnalysisHistory analysisHistory = new AnalysisHistory();
        BeanUtils.copyProperties(history, analysisHistory);
        analysisHistoryMapper.insert(analysisHistory);
    }

    @Override
    public AnalysisHistory getHistory(String id) {
        AnalysisHistory analysisHistory = analysisHistoryMapper.selectById(id);

        return analysisHistory;
    }


}




