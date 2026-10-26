package rulai.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;




@Data
public class AnalysisResultVO {


    public enum FraudStatus {
        YES, NO, SUSPICIOUS
    }

    public enum RiskLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    /**
     * 是否为欺诈
     */
    @NotNull(message = "欺诈状态不能为空")
    private FraudStatus isFraud;

    /**
     * 欺诈类型
     */
    @Size(max = 50, message = "欺诈类型长度不能超过50")
    private String fraudType;

    /**
     * 风险等级
     */
    @NotNull(message = "风险等级不能为空")
    private RiskLevel riskLevel;

    /**
     * 原因说明
     */
    @Size(max = 500, message = "原因说明长度不能超过500")
    private String reason;

    /**
     * 处理建议
     */
    @Size(max = 500, message = "处理建议长度不能超过500")
    private String advice;

    /**
     * 案件ID
     */
    @NotBlank(message = "案件ID不能为空")
    @Size(min = 1, max = 50, message = "案件ID长度需在1-50之间")
    private String caseId;
}
