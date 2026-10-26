package rulai.vo.table;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 文件管理 VO
 * </p>
 *
 * @author Mistry
 * @since 2025-06-26
 */

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class AttachmentTableVO implements Serializable {

    /**
     * 附件唯一ID
     */
    @TableId
    private String id;

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
}