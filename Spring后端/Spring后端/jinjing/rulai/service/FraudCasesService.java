package rulai.service;

import rulai.dto.FraudCaseCreateDTO;
import rulai.entity.FraudCases;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 22076
* @description 针对表【fraud_cases(反诈案例表)】的数据库操作Service
* @createDate 2026-03-16 11:51:00
*/
public interface FraudCasesService extends IService<FraudCases> {

      void createFraudCase(FraudCaseCreateDTO fraudCaseDTO);

    List<FraudCases> getAllFraudCases();

    void updateFraudCase(String id, FraudCaseCreateDTO fraudCaseDTO);

    void deleteFraudCase(String id);
}
