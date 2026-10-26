package rulai.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import rulai.annotation.AllowAnonymous;
import rulai.common.APIResponse;
import rulai.dto.FraudCaseCreateDTO;
import rulai.entity.FraudCases;
import rulai.httpstatus.exceptions.ResourceNotFoundException;
import rulai.service.impl.FraudCasesServiceImpl;
import rulai.vo.CaseVO;
import rulai.weblogger.WebLog;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/fraud-case")
public class FraudCaseController {

    @Autowired
    private FraudCasesServiceImpl fraudCasesService;

    @WebLog
    @PostMapping("/create")
    @ResponseBody
    @AllowAnonymous
    public APIResponse create(@RequestBody FraudCaseCreateDTO fraudCaseDTO) {
        fraudCasesService.createFraudCase(fraudCaseDTO);
        return APIResponse.success();
    }

    @WebLog
    @GetMapping("/{id}")
    @ResponseBody
    public APIResponse<CaseVO> getFraudCase(@PathVariable String id) {
        FraudCases fraudCase = fraudCasesService.getFraudCaseById(id);
        if (fraudCase != null) {
            CaseVO CaseVO = new CaseVO();
            org.springframework.beans.BeanUtils.copyProperties(fraudCase, CaseVO);
            return APIResponse.success(CaseVO);
        } else {
            throw new ResourceNotFoundException("没有找到对应的诈骗案例");
        }
    }

    @WebLog
    @GetMapping("/case/{caseId}")
    @ResponseBody
    public APIResponse<CaseVO> getFraudCaseByCaseId(@PathVariable String caseId) {
        FraudCases fraudCase = fraudCasesService.getFraudCaseByCaseId(caseId);
        if (fraudCase != null) {
            CaseVO CaseVO = new CaseVO();
            org.springframework.beans.BeanUtils.copyProperties(fraudCase, CaseVO);
            return APIResponse.success(CaseVO);
        } else {
            throw new ResourceNotFoundException("没有找到对应的诈骗案例");
        }
    }

    @WebLog
    @GetMapping("/list")
    @ResponseBody
    public APIResponse<List<CaseVO>> listFraudCases() {
        List<FraudCases> fraudCases = fraudCasesService.getAllFraudCases();
        List<CaseVO> CaseVOs = fraudCases.stream().map(fraudCase -> {
            CaseVO vo = new CaseVO();
            org.springframework.beans.BeanUtils.copyProperties(fraudCase, vo);
            return vo;
        }).collect(Collectors.toList());
        return APIResponse.success(CaseVOs);
    }

    @WebLog
    @PutMapping("/update/{id}")
    @ResponseBody
    public APIResponse updateFraudCase(@PathVariable String id, @RequestBody FraudCaseCreateDTO fraudCaseDTO) {
        fraudCasesService.updateFraudCase(id, fraudCaseDTO);
        return APIResponse.success();
    }

    @WebLog
    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public APIResponse deleteFraudCase(@PathVariable String id) {
        fraudCasesService.deleteFraudCase(id);
        return APIResponse.success();
    }
}