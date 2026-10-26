package rulai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rulai.entity.Attachment;
import rulai.entity.UploadedFile;
import rulai.httpstatus.exceptions.GoneException;
import rulai.httpstatus.exceptions.ResourceNotFoundException;
import rulai.mapper.UploadedFileMapper;
import rulai.service.IFileService;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * <p>
 * 文件信息 服务实现类
 * </p>
 *
 * @author Mistry
 * @since 2025-06-23
 */
@Service
public class FileServiceImpl extends ServiceImpl<UploadedFileMapper, UploadedFile> implements IFileService {
    private static Path storageLocation;

    @Value("${file.storage.location}")
    public void setStorageLocation(String location) {
        FileServiceImpl.storageLocation = Path.of(location);
    }


    @Override
    public UploadedFile storeFile(MultipartFile file) throws IOException {
        // 计算文件SHA256
        InputStream inputStream = file.getInputStream();
        String fileId = DigestUtils.sha256Hex(inputStream);
        inputStream.close();

        // 检查文件是否已存在
        UploadedFile existingFile = getById(fileId);
        if (existingFile != null) {
            // 更新引用计数
            existingFile.setRefCount(existingFile.getRefCount() + 1);
            updateById(existingFile);
            return existingFile;
        } else {
            // 存储文件，此处分按照计算的SHA-256分两级存储
            Path targetLocation = storageLocation.resolve(fileId.substring(0, 2)).resolve(fileId.substring(2, 4));
            Files.createDirectories(targetLocation);
            Files.copy(file.getInputStream(), targetLocation.resolve(fileId), StandardCopyOption.REPLACE_EXISTING);

            // 创建文件记录
            UploadedFile newFile = new UploadedFile();
            newFile.setId(fileId);
            newFile.setRefCount(1);
            newFile.setStoragePath(targetLocation.toString());

            save(newFile);
            return newFile;
        }
    }

    @Override
    public Resource loadFileAsResource(String fileId) throws ResourceNotFoundException, GoneException, InternalError{
        UploadedFile fileInfo = getById(fileId);
        if (fileInfo == null) {
            throw new ResourceNotFoundException("文件不存在");
        }

        try {
            Path filePath = Paths.get(fileInfo.getStoragePath()).resolve(fileId).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new GoneException("文件丢失");
            }
        } catch (MalformedURLException ex) {
            throw new InternalError("文件路径错误");
        }
    }

    @Override
    public Attachment createAttachmentRecord(UploadedFile uploadedFile, String associateId, String attachmentName) {
        return null;
    }

    @Override
    public void deleteFile(String fileId){
        UploadedFile fileInfo = getById(fileId);
        if (fileInfo == null) {
            throw new ResourceNotFoundException("文件不存在");
        }

        int refCount = fileInfo.getRefCount();
        if(refCount == 1) {
//            Path filePath = Paths.get(fileInfo.getStoragePath()).normalize();
            removeById(fileId);
//            try {
//                Files.delete(filePath.resolve(fileId));
//            } catch (IOException ex) {
//                throw new GoneException("文件已丢失，相关记录已移除");
//            }
        } else {
            fileInfo.setRefCount(refCount-1);
            updateById(fileInfo);
        }
    }
}
