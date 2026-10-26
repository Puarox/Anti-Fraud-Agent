package rulai.convertor;

import org.mapstruct.Mapper;
import rulai.dto.ScriptCreateDTO;
import rulai.dto.ScriptModifyDTO;
import rulai.entity.Script;
import rulai.vo.graph.ScriptManageVO;
import rulai.vo.session.ScriptVO;

@Mapper(componentModel = "spring", implementationName = "ScriptConverterImpl")
public interface IScriptConverter {
    Script fromDTO(ScriptCreateDTO scriptCreateDTO);
    Script fromDTO(ScriptModifyDTO scriptModifyDTO);
    ScriptVO toScriptVO(Script script);
    ScriptManageVO toScriptManageVO(Script script);
}