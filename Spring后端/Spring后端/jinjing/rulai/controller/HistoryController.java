package rulai.controller;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import rulai.annotation.AllowAnonymous;
import rulai.common.APIResponse;
import rulai.dto.HistoryCreateDTO;
import rulai.entity.AnalysisHistory;
import rulai.httpstatus.exceptions.ResourceNotFoundException;
import rulai.service.impl.AnalysisHistoryServiceImpl;
import rulai.vo.HistoryVO;
import rulai.weblogger.WebLog;



@Controller
@RequestMapping("/history")
public class HistoryController {
    @Autowired
    private AnalysisHistoryServiceImpl analysisHistoryService;

    @AllowAnonymous
    @PostMapping("/create")
    @WebLog
    @ResponseBody
    public APIResponse CreateHistory(HistoryCreateDTO history) {
        analysisHistoryService.createHistory(history);
        return APIResponse.success();
    }

    @WebLog
    @AllowAnonymous
    @GetMapping("/get")
    @ResponseBody
    public APIResponse<HistoryVO> getHistory(@PathVariable String id) {
        AnalysisHistory history = analysisHistoryService.getHistory(id);
        if (history != null) {
            HistoryVO historyVO = new HistoryVO();
            BeanUtils.copyProperties(history, historyVO);
            return APIResponse.success(historyVO);
        }
        else {
            throw new ResourceNotFoundException("没有找到对应的历史");
        }
    }



}
