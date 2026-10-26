package rulai.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import rulai.entity.Customer;
import rulai.vo.CustomerVO;
import rulai.vo.table.CustomerTableVO;

import java.util.List;

/**
 * <p>
 * 存储电信客户信息 Mapper 接口
 * </p>
 *
 * @author Mistry
 * @since 2025-12-29
 */
@Mapper
public interface CustomerMapper extends BaseMapper<Customer> {

    /**
     * 分页查询客户列表（带统计信息）
     */
    IPage<CustomerTableVO> selectCustomerPage(
            IPage<CustomerTableVO> page,
            @Param("ew") Wrapper<CustomerTableVO> wrapper
    );


    /**
     * 根据ID查询客户详情（带统计信息）
     */
    CustomerTableVO selectCustomerDetail(String customerId);

    List<CustomerTableVO> selectCustomerList(@Param("ew")QueryWrapper<CustomerTableVO> wrapper);
}

