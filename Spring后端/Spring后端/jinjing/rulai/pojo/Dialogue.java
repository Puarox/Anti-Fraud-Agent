package rulai.pojo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * <p>
 * 服务对话类
 * </p>
 *
 * @author Mistry
 * @since 2025-12-31
 */

@Getter
@Setter
@ToString
public class Dialogue {
    /**
     * 说话人角色
     */
    private String role;

    /**
     * 说话产生时间
     */
    private LocalDateTime time;

    /**
     * 说话内容
     */
    private String content;
}
