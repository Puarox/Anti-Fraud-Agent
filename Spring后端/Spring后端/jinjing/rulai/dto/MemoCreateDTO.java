package rulai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MemoCreateDTO {

    /**
     * 便签标题
     */
    @NotBlank(message = "便签标题不能为空")
    private String memoName;

    /**
     * 便签内容
     */
    private String content;
}
