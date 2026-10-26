package rulai.convertor;

import org.mapstruct.Mapper;
import rulai.dto.NodeCreateDTO;
import rulai.dto.NodeModifyDTO;
import rulai.entity.Node;
import rulai.vo.graph.NodeManageVO;
import rulai.vo.graph.NodeVO;
import rulai.vo.session.NodeScriptVO;

@Mapper(componentModel = "spring", implementationName = "NodeConverterImpl")
public interface INodeConverter {
    Node fromDTO(NodeCreateDTO nodeModifyDTO);
    Node fromDTO(NodeModifyDTO nodeModifyDTO);
    NodeScriptVO toNodeScriptVO(Node node);
    NodeManageVO toNodeManageVO(Node node);
    NodeVO toNodeVO(Node node);
}