package rulai.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;
/**
 * <p>
 * 系统附件信息
 * </p>
 *
 * @author Mistry
 * @since 2025-12-29
 */
@Getter
@Setter
@ToString
public class Attachment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 附件唯一ID
     */
    private String id;

    /**
     * 关联对象表名
     */
    private String associateTable;

    /**
     * 关联对象ID
     */
    private String associateId;

    /**
     * 关联类型
     */
    private String associateType;

    /**
     * 附件名
     */
    private String attachmentName;

    /**
     * 附件类型
     */
    private String attachmentType;

    /**
     * 附件大小（字节）
     */
    private Integer attachmentSize;

    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;

    /**
     * 附件关联文件ID
     */
    private String fileId;

    /**
     * 逻辑删除标识（0-未删除，1-已删除）
     */
    @TableLogic
    private Integer isDeleted;
}
