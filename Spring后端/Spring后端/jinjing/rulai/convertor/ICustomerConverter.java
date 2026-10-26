package rulai.convertor;

import org.mapstruct.Mapper;
import rulai.dto.CustomerCreateDTO;
import rulai.dto.CustomerModifyDTO;
import rulai.entity.Customer;
import rulai.vo.CustomerDetailVO;
import rulai.vo.CustomerVO;

@Mapper(componentModel = "spring", implementationName = "CustomerConverterImpl")
public interface ICustomerConverter {
    
    Customer fromCreateDTO(CustomerCreateDTO dto);

    Customer fromModifyDTO(CustomerModifyDTO dto);

    CustomerDetailVO toDetailVO(Customer customer);
}
