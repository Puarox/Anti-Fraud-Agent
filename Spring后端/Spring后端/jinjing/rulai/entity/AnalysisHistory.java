package rulai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

/**
 * 
 * @TableName analysis_history
 */
@TableName(value ="analysis_history")
public class AnalysisHistory {
    /**
     * 记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

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

    /**
     * 记录ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 记录ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 用户ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * 用户ID
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * 分析任务ID
     */
    public String getTaskId() {
        return taskId;
    }

    /**
     * 分析任务ID
     */
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    /**
     * 用户上传聊天记录
     */
    public String getInputText() {
        return inputText;
    }

    /**
     * 用户上传聊天记录
     */
    public void setInputText(String inputText) {
        this.inputText = inputText;
    }

    /**
     * 图片URL
     */
    public Object getImageUrls() {
        return imageUrls;
    }

    /**
     * 图片URL
     */
    public void setImageUrls(Object imageUrls) {
        this.imageUrls = imageUrls;
    }

    /**
     * 视频URL
     */
    public Object getVideoUrls() {
        return videoUrls;
    }

    /**
     * 视频URL
     */
    public void setVideoUrls(Object videoUrls) {
        this.videoUrls = videoUrls;
    }

    /**
     * AI分析结果
     */
    public String getAnalysisResult() {
        return analysisResult;
    }

    /**
     * AI分析结果
     */
    public void setAnalysisResult(String analysisResult) {
        this.analysisResult = analysisResult;
    }

    /**
     * 
     */
    public Object getRiskLevel() {
        return riskLevel;
    }

    /**
     * 
     */
    public void setRiskLevel(Object riskLevel) {
        this.riskLevel = riskLevel;
    }

    /**
     * 匹配案例
     */
    public Object getRelatedCaseIds() {
        return relatedCaseIds;
    }

    /**
     * 匹配案例
     */
    public void setRelatedCaseIds(Object relatedCaseIds) {
        this.relatedCaseIds = relatedCaseIds;
    }

    /**
     * 
     */
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * 
     */
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        AnalysisHistory other = (AnalysisHistory) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getTaskId() == null ? other.getTaskId() == null : this.getTaskId().equals(other.getTaskId()))
            && (this.getInputText() == null ? other.getInputText() == null : this.getInputText().equals(other.getInputText()))
            && (this.getImageUrls() == null ? other.getImageUrls() == null : this.getImageUrls().equals(other.getImageUrls()))
            && (this.getVideoUrls() == null ? other.getVideoUrls() == null : this.getVideoUrls().equals(other.getVideoUrls()))
            && (this.getAnalysisResult() == null ? other.getAnalysisResult() == null : this.getAnalysisResult().equals(other.getAnalysisResult()))
            && (this.getRiskLevel() == null ? other.getRiskLevel() == null : this.getRiskLevel().equals(other.getRiskLevel()))
            && (this.getRelatedCaseIds() == null ? other.getRelatedCaseIds() == null : this.getRelatedCaseIds().equals(other.getRelatedCaseIds()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getTaskId() == null) ? 0 : getTaskId().hashCode());
        result = prime * result + ((getInputText() == null) ? 0 : getInputText().hashCode());
        result = prime * result + ((getImageUrls() == null) ? 0 : getImageUrls().hashCode());
        result = prime * result + ((getVideoUrls() == null) ? 0 : getVideoUrls().hashCode());
        result = prime * result + ((getAnalysisResult() == null) ? 0 : getAnalysisResult().hashCode());
        result = prime * result + ((getRiskLevel() == null) ? 0 : getRiskLevel().hashCode());
        result = prime * result + ((getRelatedCaseIds() == null) ? 0 : getRelatedCaseIds().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", userId=").append(userId);
        sb.append(", taskId=").append(taskId);
        sb.append(", inputText=").append(inputText);
        sb.append(", imageUrls=").append(imageUrls);
        sb.append(", videoUrls=").append(videoUrls);
        sb.append(", analysisResult=").append(analysisResult);
        sb.append(", riskLevel=").append(riskLevel);
        sb.append(", relatedCaseIds=").append(relatedCaseIds);
        sb.append(", createdAt=").append(createdAt);
        sb.append("]");
        return sb.toString();
    }
}