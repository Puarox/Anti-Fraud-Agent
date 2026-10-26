package rulai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import rulai.entity.UploadedFile;

/**
 * <p>
 * 文件信息 Mapper 接口
 * </p>
 *
 * @author Mistry
 * @since 2025-12-29
 */
@Mapper
public interface UploadedFileMapper extends BaseMapper<UploadedFile> {

}

