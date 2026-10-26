package rulai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import rulai.entity.Attachment;
import rulai.entity.UploadedFile;

import java.io.IOException;

/**
 * <p>
 * 文件信息 服务类
 * </p>
 *
 * @author Mistry
 * @since 2025-06-23
 */
public interface IFileService extends IService<UploadedFile> {
    /**
     * 存储文件到系统
     * @param file 上传的文件
     * @return 文件信息实体
     */
    UploadedFile storeFile(MultipartFile file) throws IOException;

    /**
     * 加载文件资源
     * @param fileId 文件ID
     * @return 文件资源
     */
    Resource loadFileAsResource(String fileId);

    /**
     * 创建附件记录
     * @param uploadedFile 文件信息
     * @param associateId 关联对象ID
     * @param attachmentName 附件名称
     * @return 附件记录
     */
    Attachment createAttachmentRecord(UploadedFile uploadedFile, String associateId, String attachmentName);

    void deleteFile(String fileId);
}
