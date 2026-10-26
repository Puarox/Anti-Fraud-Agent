package rulai.convertor;

import org.mapstruct.Mapper;
import rulai.dto.AttachmentModifyDTO;
import rulai.entity.Attachment;
import rulai.vo.table.AttachmentTableVO;

import java.util.List;

@Mapper(componentModel = "spring", implementationName = "AttachmentConverterImpl")
public interface IAttachmentConverter {

    AttachmentTableVO toVO(Attachment attachment);

    List<AttachmentTableVO> toVO(List<Attachment> attachments);

    Attachment fromDTO(AttachmentModifyDTO attachmentModifyDTO);
}