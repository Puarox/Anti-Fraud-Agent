package rulai.convertor;

import org.mapstruct.Mapper;
import rulai.dto.MemoCreateDTO;
import rulai.dto.MemoModifyDTO;
import rulai.pojo.Memo;

@Mapper(componentModel = "spring", implementationName = "MemoConverterImpl")
public interface IMemoConverter {
    Memo fromDTO(MemoCreateDTO memoCreateDTO);
    Memo fromDTO(MemoModifyDTO memoModifyDTO);
}