package rulai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tencentcloudapi.dbs.v20211108.models.BackupEndpoint;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import rulai.dto.FraudCaseCreateDTO;
import rulai.entity.FraudCases;
import rulai.httpstatus.exceptions.ResourceNotFoundException;
import rulai.service.FraudCasesService;
import rulai.mapper.FraudCasesMapper;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
* @author 22076
* @description 针对表【fraud_cases(反诈案例表)】的数据库操作Service实现
* @createDate 2026-03-16 11:51:00
*/
@Service
public class FraudCasesServiceImpl extends ServiceImpl<FraudCasesMapper, FraudCases>
    implements FraudCasesService{


    @Autowired
    private FraudCasesMapper fraudcasesMapper;
    @Override
    public void createFraudCase(FraudCaseCreateDTO fraudCaseDTO) {
        FraudCases fraudCases = new FraudCases();
        BeanUtils.copyProperties(fraudCaseDTO,fraudCases);
        fraudcasesMapper.insert(fraudCases);
    }

    @Override
    public List<FraudCases> getAllFraudCases() {
        List<FraudCases> fraudCasesList = fraudcasesMapper.selectList(null);

        return fraudCasesList;
    }

    @Override
    public void updateFraudCase(String id, FraudCaseCreateDTO fraudCaseDTO) {
        // 1. 根据ID查询现有诈骗案例
        FraudCases existingCase = fraudcasesMapper.selectById(id);
        if (existingCase == null) {
            throw new ResourceNotFoundException("没有找到ID为" + id + "的诈骗案例");
        }

        // 2. 使用DTO中的新值更新实体
        FraudCases updatedCase = new FraudCases();
        BeanUtils.copyProperties(existingCase, updatedCase);


        // 4. 执行数据库更新
        int result = fraudcasesMapper.updateById(updatedCase);
        if (result == 0) {
            throw new RuntimeException("更新诈骗案例失败，受影响行数为0");
        }
    }

    @Override
    public void deleteFraudCase(String id) {

    }


    public FraudCases getFraudCaseById(String id) {
        FraudCases fraudCases = fraudcasesMapper.selectById(id);
        return fraudCases;

    }

    public FraudCases getFraudCaseByCaseId(String caseId) {

        QueryWrapper<FraudCases> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("case_id", caseId);

        return fraudcasesMapper.selectOne(queryWrapper);
    }
}




