package rulai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 反诈案例表
 * @TableName fraud_cases
 */
@TableName(value ="fraud_cases")
public class FraudCases {
    /**
     * 案例ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 案例编号
     */
    private String caseId;

    /**
     * 案例标题
     */
    private String title;

    /**
     * 目标群体（老人/学生/未成年人/通用）
     */
    private String targetGroup;

    /**
     * 诈骗类型（投资诈骗/刷单诈骗等）
     */
    private String fraudType;

    /**
     * 诈骗场景（社交平台/电话/兼职平台等）
     */
    private String scenario;

    /**
     * 诈骗经过描述
     */
    private String process;

    /**
     * 关键特征关键词(JSON数组)
     */
    private String keyFeatures;

    /**
     * 风险等级
     */
    private Object riskLevel;

    /**
     * 涉案金额
     */
    private BigDecimal amountInvolved;

    /**
     * 发生地区
     */
    private String region;

    /**
     * 案例年份
     */
    private Integer caseYear;

    /**
     * 案例来源（公安部/法院等）
     */
    private String caseSource;

    /**
     * 案例来源链接
     */
    private String sourceUrl;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 用于向量化的拼接文本
     */
    private String ragText;

    /**
     * 案例ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 案例ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 案例编号
     */
    public String getCaseId() {
        return caseId;
    }

    /**
     * 案例编号
     */
    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    /**
     * 案例标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 案例标题
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 目标群体（老人/学生/未成年人/通用）
     */
    public String getTargetGroup() {
        return targetGroup;
    }

    /**
     * 目标群体（老人/学生/未成年人/通用）
     */
    public void setTargetGroup(String targetGroup) {
        this.targetGroup = targetGroup;
    }

    /**
     * 诈骗类型（投资诈骗/刷单诈骗等）
     */
    public String getFraudType() {
        return fraudType;
    }

    /**
     * 诈骗类型（投资诈骗/刷单诈骗等）
     */
    public void setFraudType(String fraudType) {
        this.fraudType = fraudType;
    }

    /**
     * 诈骗场景（社交平台/电话/兼职平台等）
     */
    public String getScenario() {
        return scenario;
    }

    /**
     * 诈骗场景（社交平台/电话/兼职平台等）
     */
    public void setScenario(String scenario) {
        this.scenario = scenario;
    }

    /**
     * 诈骗经过描述
     */
    public String getProcess() {
        return process;
    }

    /**
     * 诈骗经过描述
     */
    public void setProcess(String process) {
        this.process = process;
    }

    /**
     * 关键特征关键词(JSON数组)
     */
    public String getKeyFeatures() {
        return keyFeatures;
    }

    /**
     * 关键特征关键词(JSON数组)
     */
    public void setKeyFeatures(String keyFeatures) {
        this.keyFeatures = keyFeatures;
    }

    /**
     * 风险等级
     */
    public Object getRiskLevel() {
        return riskLevel;
    }

    /**
     * 风险等级
     */
    public void setRiskLevel(Object riskLevel) {
        this.riskLevel = riskLevel;
    }

    /**
     * 涉案金额
     */
    public BigDecimal getAmountInvolved() {
        return amountInvolved;
    }

    /**
     * 涉案金额
     */
    public void setAmountInvolved(BigDecimal amountInvolved) {
        this.amountInvolved = amountInvolved;
    }

    /**
     * 发生地区
     */
    public String getRegion() {
        return region;
    }

    /**
     * 发生地区
     */
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * 案例年份
     */
    public Integer getCaseYear() {
        return caseYear;
    }

    /**
     * 案例年份
     */
    public void setCaseYear(Integer caseYear) {
        this.caseYear = caseYear;
    }

    /**
     * 案例来源（公安部/法院等）
     */
    public String getCaseSource() {
        return caseSource;
    }

    /**
     * 案例来源（公安部/法院等）
     */
    public void setCaseSource(String caseSource) {
        this.caseSource = caseSource;
    }

    /**
     * 案例来源链接
     */
    public String getSourceUrl() {
        return sourceUrl;
    }

    /**
     * 案例来源链接
     */
    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    /**
     * 创建时间
     */
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * 创建时间
     */
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 更新时间
     */
    public Date getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 更新时间
     */
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * 用于向量化的拼接文本
     */
    public String getRagText() {
        return ragText;
    }

    /**
     * 用于向量化的拼接文本
     */
    public void setRagText(String ragText) {
        this.ragText = ragText;
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
        FraudCases other = (FraudCases) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getCaseId() == null ? other.getCaseId() == null : this.getCaseId().equals(other.getCaseId()))
            && (this.getTitle() == null ? other.getTitle() == null : this.getTitle().equals(other.getTitle()))
            && (this.getTargetGroup() == null ? other.getTargetGroup() == null : this.getTargetGroup().equals(other.getTargetGroup()))
            && (this.getFraudType() == null ? other.getFraudType() == null : this.getFraudType().equals(other.getFraudType()))
            && (this.getScenario() == null ? other.getScenario() == null : this.getScenario().equals(other.getScenario()))
            && (this.getProcess() == null ? other.getProcess() == null : this.getProcess().equals(other.getProcess()))
            && (this.getKeyFeatures() == null ? other.getKeyFeatures() == null : this.getKeyFeatures().equals(other.getKeyFeatures()))
            && (this.getRiskLevel() == null ? other.getRiskLevel() == null : this.getRiskLevel().equals(other.getRiskLevel()))
            && (this.getAmountInvolved() == null ? other.getAmountInvolved() == null : this.getAmountInvolved().equals(other.getAmountInvolved()))
            && (this.getRegion() == null ? other.getRegion() == null : this.getRegion().equals(other.getRegion()))
            && (this.getCaseYear() == null ? other.getCaseYear() == null : this.getCaseYear().equals(other.getCaseYear()))
            && (this.getCaseSource() == null ? other.getCaseSource() == null : this.getCaseSource().equals(other.getCaseSource()))
            && (this.getSourceUrl() == null ? other.getSourceUrl() == null : this.getSourceUrl().equals(other.getSourceUrl()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()))
            && (this.getRagText() == null ? other.getRagText() == null : this.getRagText().equals(other.getRagText()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getCaseId() == null) ? 0 : getCaseId().hashCode());
        result = prime * result + ((getTitle() == null) ? 0 : getTitle().hashCode());
        result = prime * result + ((getTargetGroup() == null) ? 0 : getTargetGroup().hashCode());
        result = prime * result + ((getFraudType() == null) ? 0 : getFraudType().hashCode());
        result = prime * result + ((getScenario() == null) ? 0 : getScenario().hashCode());
        result = prime * result + ((getProcess() == null) ? 0 : getProcess().hashCode());
        result = prime * result + ((getKeyFeatures() == null) ? 0 : getKeyFeatures().hashCode());
        result = prime * result + ((getRiskLevel() == null) ? 0 : getRiskLevel().hashCode());
        result = prime * result + ((getAmountInvolved() == null) ? 0 : getAmountInvolved().hashCode());
        result = prime * result + ((getRegion() == null) ? 0 : getRegion().hashCode());
        result = prime * result + ((getCaseYear() == null) ? 0 : getCaseYear().hashCode());
        result = prime * result + ((getCaseSource() == null) ? 0 : getCaseSource().hashCode());
        result = prime * result + ((getSourceUrl() == null) ? 0 : getSourceUrl().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        result = prime * result + ((getUpdatedAt() == null) ? 0 : getUpdatedAt().hashCode());
        result = prime * result + ((getRagText() == null) ? 0 : getRagText().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", caseId=").append(caseId);
        sb.append(", title=").append(title);
        sb.append(", targetGroup=").append(targetGroup);
        sb.append(", fraudType=").append(fraudType);
        sb.append(", scenario=").append(scenario);
        sb.append(", process=").append(process);
        sb.append(", keyFeatures=").append(keyFeatures);
        sb.append(", riskLevel=").append(riskLevel);
        sb.append(", amountInvolved=").append(amountInvolved);
        sb.append(", region=").append(region);
        sb.append(", caseYear=").append(caseYear);
        sb.append(", caseSource=").append(caseSource);
        sb.append(", sourceUrl=").append(sourceUrl);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", ragText=").append(ragText);
        sb.append("]");
        return sb.toString();
    }
}