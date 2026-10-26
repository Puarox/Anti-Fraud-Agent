package rulai.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
/**
 * <p>
 * 文件信息
 * </p>
 *
 * @author Mistry
 * @since 2025-12-29
 */
@Getter
@Setter
@ToString
@TableName("uploaded_file")
public class UploadedFile implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文件唯一ID（SHA256）
     */
    private String id;

    /**
     * 引用计数
     */
    private Integer refCount;

    /**
     * 文件存储路径
     */
    private String storagePath;

    /**
     * 逻辑删除标识（0-未删除，1-已删除）
     */
    @TableLogic
    private Integer isDeleted;
}
