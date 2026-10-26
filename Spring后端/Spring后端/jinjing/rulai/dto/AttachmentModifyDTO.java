package rulai.dto;

import jakarta.validation.constraints.Null;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import rulai.annotation.NotBlankIfPresent;

import java.io.Serializable;

/**
 * <p>
 * 文件管理 DTO
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
public class AttachmentModifyDTO implements Serializable {
    /**
     * 附件唯一ID
     */
    @Null(message = "Body中禁止ID传参，请在URL中指定ID")
    private String attachmentId;

    /**
     * 附件名
     */
    @NotBlankIfPresent(message = "附件名不得为空")
    @Length(max = 255, message = "附件名长度不超过255字符")
    private String attachmentName;

    @NotBlankIfPresent(message = "关联对象id不得为空")
    private String associateId ;
    /**
     * 附件类型
     */
    @Length(max = 10, message = "附件后缀名长度不超过10字符")
    private String attachmentType;
}