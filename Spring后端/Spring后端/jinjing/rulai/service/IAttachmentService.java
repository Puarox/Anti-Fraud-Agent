package rulai.service;

import org.springframework.web.multipart.MultipartFile;
import rulai.entity.Attachment;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 系统附件信息 服务类
 * </p>
 *
 * @author Mistry
 * @since 2025-12-26
 */
public interface IAttachmentService extends IService<Attachment> {

    Attachment uploadAttachment(Attachment attachment, MultipartFile file);

    Attachment getAttachment(String attachmentId);

    void deleteAttachment(String attachmentId);

    List<Attachment> getPredecessors(String id);

    boolean hasPredecessors(String id);

}
