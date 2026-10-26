package rulai.convertor;

import org.mapstruct.Mapper;
import rulai.dto.CustomerBundleCreateDTO;
import rulai.dto.CustomerBundleModifyDTO;
import rulai.entity.CustomerBundle;
import rulai.vo.CustomerBundleVO;

@Mapper(componentModel = "spring", implementationName = "CustomerBundleConverterImpl")
public interface ICustomerBundleConverter {
    CustomerBundle fromCreateDTO(CustomerBundleCreateDTO createDTO);
    CustomerBundle fromModfiyDTO(CustomerBundleModifyDTO modifyDTO);
    CustomerBundleVO toTO(CustomerBundle bundle);
}
