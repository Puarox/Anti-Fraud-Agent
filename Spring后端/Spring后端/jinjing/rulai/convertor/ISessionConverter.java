package rulai.convertor;

import org.mapstruct.Mapper;
import rulai.entity.Record;
import rulai.pojo.Session;
import rulai.vo.session.SessionStateVO;

@Mapper(componentModel = "spring", implementationName = "SessionConverterImpl")
public interface ISessionConverter {
    Record toRecord(Session session);
    SessionStateVO toSessionStateVO(Session session);
}