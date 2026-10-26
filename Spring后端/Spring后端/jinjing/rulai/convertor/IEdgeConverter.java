package rulai.convertor;

import org.mapstruct.Mapper;
import rulai.dto.EdgeCreateDTO;
import rulai.dto.EdgeModifyDTO;
import rulai.entity.Edge;
import rulai.vo.graph.EdgeManageVO;
import rulai.vo.graph.EdgeVO;
import rulai.vo.session.EdgeScriptVO;

@Mapper(componentModel = "spring", implementationName = "EdgeConverterImpl")
public interface IEdgeConverter {
    Edge fromDTO(EdgeCreateDTO edgeModifyDTO);
    Edge fromDTO(EdgeModifyDTO edgeModifyDTO);
    EdgeScriptVO toEdgeScriptVO(Edge edge);
    EdgeManageVO toEdgeManageVO(Edge edge);
    EdgeVO toEdgeVO(Edge edge);
}