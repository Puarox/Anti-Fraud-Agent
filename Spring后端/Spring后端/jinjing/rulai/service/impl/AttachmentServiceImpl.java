package rulai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import rulai.entity.Attachment;
import rulai.entity.UploadedFile;
import rulai.httpstatus.exceptions.InternalServeErrorException;
import rulai.httpstatus.exceptions.ResourceNotFoundException;
import rulai.mapper.AttachmentMapper;
import rulai.service.IAttachmentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import rulai.service.IFileService;
import rulai.service.IForeignKeyCheckerService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * <p>
 * 系统附件信息 服务实现类
 * </p>
 *
 * @author Mistry
 * @since 2025-12-26
 */
@Service
public class AttachmentServiceImpl extends ServiceImpl<AttachmentMapper, Attachment> implements IAttachmentService {

    @Autowired
    private IFileService fileService;

    @Autowired
    private IForeignKeyCheckerService foreignKeyCheckerService;

    @Override
    public Attachment uploadAttachment(Attachment attachment, MultipartFile file) {
        if(attachment.getAssociateTable() != null || attachment.getAssociateId() != null) {
            foreignKeyCheckerService.checkForeignKey(attachment.getAssociateTable(), attachment.getAssociateId());
        }

        UploadedFile uploadedFile;
        try {
            uploadedFile = fileService.storeFile(file);
        } catch (IOException ex) {
            throw new InternalServeErrorException("文件上传失败");
        }

        attachment.setId(UUID.randomUUID().toString());
        attachment.setFileId(uploadedFile.getId());
        attachment.setUploadTime(LocalDateTime.now());
        save(attachment);
        return attachment;
    }

    @Override
    public Attachment getAttachment(String attachmentId) {
        Attachment attachment = this.getById(attachmentId);
        if (attachment == null) {
            throw new ResourceNotFoundException("附件不存在");
        }
        return attachment;
    }

    @Override
    public void deleteAttachment(String attachmentId) {
        Attachment attachment = getById(attachmentId);
        if(attachment == null) {
            throw new ResourceNotFoundException("附件不存在");
        }

        fileService.deleteFile(attachment.getId());
        removeById(attachmentId);
    }

    @Override
    public List<Attachment> getPredecessors(String id) {
        LambdaQueryWrapper<Attachment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Attachment::getAssociateId, id);
        return this.list(queryWrapper);
    }

    @Override
    public boolean hasPredecessors(String id) {
        LambdaQueryWrapper<Attachment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Attachment::getAssociateId, id);
        return this.exists(queryWrapper);
    }
}
