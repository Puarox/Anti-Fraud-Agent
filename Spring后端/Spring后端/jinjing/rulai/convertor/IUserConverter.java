package rulai.convertor;

import org.mapstruct.Mapper;
import rulai.dto.UserCreateDTO;
import rulai.dto.UserDetailModifyDTO;
import rulai.dto.UserModifyDTO;
import rulai.entity.User;
import rulai.vo.UserVO;

@Mapper(componentModel = "spring", implementationName = "UserConverterImpl")
public interface IUserConverter {
    User fromUserCreateDTOToEntity(UserCreateDTO dto);
    User fromUserModifyDTOToEntity(UserDetailModifyDTO dto);
    UserVO toUserVO(User user);
    User fromUserModifyDTOToEntityManage(UserModifyDTO dto);

}