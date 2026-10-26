package rulai.convertor;

import org.mapstruct.Mapper;
import rulai.dto.RepairmentCreateDTO;
import rulai.entity.Repairment;
import rulai.vo.RepairmentVO;

@Mapper(componentModel = "spring", implementationName = "RepairmentConverterImpl")
public interface IRpairmentConverter {
    Repairment fromCreateDTO(RepairmentCreateDTO dto);

    RepairmentVO toVO(Repairment repairment);
}
