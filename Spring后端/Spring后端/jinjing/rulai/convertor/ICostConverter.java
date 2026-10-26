package rulai.convertor;

import org.mapstruct.Mapper;
import rulai.dto.CostCreateDTO;
import rulai.dto.CostModifyDTO;
import rulai.dto.CostRefundDTO;
import rulai.entity.Cost;
import rulai.vo.table.CostTableVO;

import java.util.List;

@Mapper(componentModel = "spring", implementationName = "CostConverterImpl")
public interface ICostConverter {

    CostTableVO toVO(Cost company);

    List<CostTableVO> toVO(List<Cost> costs);

    Cost fromDTO(CostModifyDTO dto);

    Cost fromDTO(CostCreateDTO dto);

    Cost fromRefundDTO(CostRefundDTO dto);
}
