package rulai.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import rulai.dto.CustomerCreateDTO;
import rulai.dto.CustomerModifyDTO;
import rulai.dto.CustomerQueryDTO;
import rulai.entity.Customer;
import com.baomidou.mybatisplus.extension.service.IService;
import rulai.vo.CustomerDetailVO;
import rulai.vo.CustomerVO;
import rulai.vo.table.CustomerTableVO;

import java.util.List;

/**
 * <p>
 * 存储电信客户信息 服务类
 * </p>
 *
 * @author Mistry
 * @since 2025-12-26
 */
public interface ICustomerService extends IService<Customer> {

    IPage<CustomerTableVO> pageCustomers(CustomerQueryDTO queryDTO);

    /**
     * 得到客户信息
     * @param id
     * @return
     */
    CustomerDetailVO getCustomerDetail(String id);

    /**
     * 创建
     * @param dto
     * @return
     */
    Customer createCustomer(CustomerCreateDTO dto);

    /**
     * 删除
     * @param customerId
     */
    void deleteCustomer(String customerId);

    /**
     * 更新
     * @param dto
     */
    void updateCustomer(CustomerModifyDTO dto);

    /**
     * 查询列表
     * @param queryDTO
     * @return
     */
    List<CustomerTableVO> customerList(CustomerQueryDTO queryDTO);

    /**
     * 判断客户是否正常
     * @param customerId
     * @return
     */
    boolean isNormalCustomer(String customerId);
}
