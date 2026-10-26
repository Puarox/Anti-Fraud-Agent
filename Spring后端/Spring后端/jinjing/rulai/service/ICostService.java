package rulai.service;

import rulai.dto.CostCreateDTO;
import rulai.dto.CostModifyDTO;
import rulai.dto.CostQueryDTO;
import rulai.dto.CostRefundDTO;
import rulai.entity.Cost;
import com.baomidou.mybatisplus.extension.service.IService;
import rulai.vo.table.CostTableVO;

import java.util.List;

/**
 * <p>
 * 客户费用账单记录 服务类
 * </p>
 *
 * @author Mistry
 * @since 2025-12-26
 */
public interface ICostService extends IService<Cost> {
    Cost createCost(CostCreateDTO dto);

    Boolean updateCost(CostModifyDTO dto);

    /**
     * 删除订单
     * @param costId
     */
    void deleteCost(String costId);

    List<CostTableVO> listCosts(CostQueryDTO queryDTO);

    /**
     * 退款
     * @param refundDTO
     */
    void refundCost(CostRefundDTO refundDTO);
}
