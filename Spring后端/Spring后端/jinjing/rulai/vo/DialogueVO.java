package rulai.vo;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class DialogueVO {
    /**
     * 说话人角色
     */
    private String role;

    /**
     * 说话产生时间
     */
    private String time;

    /**
     * 说话内容
     */
    private String content;
}
