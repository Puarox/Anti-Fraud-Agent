package rulai.dto;

import lombok.Data;
import rulai.annotation.NotBlankIfPresent;

@Data
public class MemoModifyDTO {

    /**
     * 便签标题
     */
    @NotBlankIfPresent(message = "便签标题不能为空")
    private String memoName;

    /**
     * 便签内容
     */
    private String content;
}
