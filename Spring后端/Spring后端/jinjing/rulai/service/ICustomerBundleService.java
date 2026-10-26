package rulai.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import rulai.dto.CostQueryDTO;
import rulai.dto.CustomerBundleCreateDTO;
import rulai.dto.CustomerBundleModifyDTO;
import rulai.dto.CustomerBundleQueryDTO;
import rulai.entity.Cost;
import rulai.entity.CustomerBundle;
import com.baomidou.mybatisplus.extension.service.IService;
import rulai.vo.CustomerBundleDetailVO;
import rulai.vo.CustomerBundleVO;
import rulai.vo.table.CostTableVO;
import rulai.vo.table.CustomerBundleTableVO;

import java.util.List;

/**
 * <p>
 * 客户与套餐的订阅关系 服务类
 * </p>
 *
 * @author Mistry
 * @since 2025-12-26
 */
public interface ICustomerBundleService extends IService<CustomerBundle> {


    /**
     * 办理套餐
     * @param dto
     * @return
     */
    CustomerBundleVO createCustomerBundle(CustomerBundleCreateDTO dto);

    /**
     * 更新订单信息
     * @param dto
     */
    void updateCost(CustomerBundleModifyDTO dto);

    /**
     * 分页条件查询用户套餐
     * @param queryDTO
     * @return
     */
    IPage<CustomerBundleTableVO> pageCustomerBundle(CustomerBundleQueryDTO queryDTO);

    /**
     * 部分也查询用户套餐
     * @param queryDTO
     * @return
     */
    List<CustomerBundleTableVO> listCustomerBundle(CustomerBundleQueryDTO queryDTO );

    /**
     * 根据客户id获得杂项信息
     * @param customerId
     * @return
     */
    CustomerBundleDetailVO getCustomerBundleMeta(String customerId);

    /**
     * 判断外键
     * @param bundleId
     * @return
     */
    Boolean checkBundleForeignKey(String bundleId);
}
