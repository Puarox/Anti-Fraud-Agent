package rulai.convertor;

import org.mapstruct.Mapper;
import rulai.dto.BundleCreateDTO;
import rulai.dto.BundleModifyDTO;
import rulai.entity.Bundle;
import rulai.entity.Cost;

@Mapper(componentModel = "spring", implementationName = "BundleConverterImpl")
public interface IBundleConverter {

    Bundle fromCreateDTO(BundleCreateDTO bundleDTO);

    Bundle fromModifyDTO(BundleModifyDTO dto);
}
