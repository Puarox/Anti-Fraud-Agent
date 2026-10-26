package rulai.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class FraudCaseModifyDTO {
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

}
