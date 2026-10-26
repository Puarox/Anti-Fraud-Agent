package rulai.vo;

import lombok.Data;

import java.util.Date;

@Data
public class HistoryVO {
    /**
     * 用户ID
     */
    private String userId;

    /**
     * 分析任务ID
     */
    private String taskId;

    /**
     * 用户上传聊天记录
     */
    private String inputText;

    /**
     * 图片URL
     */
    private Object imageUrls;

    /**
     * 视频URL
     */
    private Object videoUrls;

    /**
     * AI分析结果
     */
    private String analysisResult;

    /**
     *
     */
    private Object riskLevel;

    /**
     * 匹配案例
     */
    private Object relatedCaseIds;

    /**
     *
     */
    private Date createdAt;
}
