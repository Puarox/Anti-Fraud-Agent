package rulai.service.impl;

import rulai.entity.UploadedFile;
import rulai.mapper.UploadedFileMapper;
import rulai.service.IUploadedFileService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 文件信息 服务实现类
 * </p>
 *
 * @author Mistry
 * @since 2025-12-26
 */
@Service
public class UploadedFileServiceImpl extends ServiceImpl<UploadedFileMapper, UploadedFile> implements IUploadedFileService {

}
