package rulai.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import rulai.dto.CostQueryDTO;
import rulai.dto.RepairmentCreateDTO;
import rulai.dto.RepairmentQueryDTO;
import rulai.entity.Repairment;
import com.baomidou.mybatisplus.extension.service.IService;
import rulai.vo.table.CostTableVO;
import rulai.vo.table.RepairmentTableVO;

import java.util.List;

/**
* @author 22076
* @description 针对表【repairment(维修订单信息)】的数据库操作Service
* @createDate 2026-01-05 10:12:45
*/
public interface IRepairmentService extends IService<Repairment> {

    /**
     * 创建维修订单
     * @param dto
     * @return
     */
    Repairment createRepairment(RepairmentCreateDTO dto);

    /**
     * list
     * @param queryDTO
     * @return
     */
    List<RepairmentTableVO> listRepairments(RepairmentQueryDTO queryDTO);

    /**
     * 分页查询
     * @param queryDTO
     * @return
     */
    IPage<RepairmentTableVO> pageRepairment(RepairmentQueryDTO queryDTO);
}
