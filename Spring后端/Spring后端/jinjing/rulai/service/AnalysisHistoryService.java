package rulai.service;

import rulai.dto.HistoryCreateDTO;
import rulai.entity.AnalysisHistory;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 22076
* @description 针对表【analysis_history】的数据库操作Service
* @createDate 2026-03-16 11:49:21
*/
public interface AnalysisHistoryService extends IService<AnalysisHistory> {
    /**
     * 创建一个历史
     * @param history
     */
    void createHistory(HistoryCreateDTO history);

    AnalysisHistory getHistory(String id);
}
