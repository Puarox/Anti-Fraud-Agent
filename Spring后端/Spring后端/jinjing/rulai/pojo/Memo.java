package rulai.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Memo {

    /**
     * 便签唯一Id
     */
    private String id;

    /**
     * 便签唯一名称
     */
    private String memoName;

    /**
     * 便签内容
     */
    private String content;

    /**
     * 便签创建时间
     */
    private LocalDateTime createTime;
}
