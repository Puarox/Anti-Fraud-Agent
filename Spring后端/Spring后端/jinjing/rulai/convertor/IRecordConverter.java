package rulai.convertor;

import org.mapstruct.Mapper;
import rulai.entity.Record;
import rulai.vo.RecordDetailVO;

@Mapper(componentModel = "spring", implementationName = "RecordConverterImpl")
public interface IRecordConverter {
    RecordDetailVO toVO(Record edge);
}