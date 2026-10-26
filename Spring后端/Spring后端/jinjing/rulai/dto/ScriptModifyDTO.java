package rulai.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Null;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import rulai.annotation.EnumValid;
import rulai.annotation.IsValidJSON;
import rulai.annotation.NotBlankIfPresent;
import rulai.common.JsonDataDeserializer;
import rulai.common.JsonDataSerializer;
import rulai.constant.SCRIPT_CONSTANT;

import java.io.Serializable;

/**
 * <p>
 * 话术内容表
 * </p>
 *
 * @author Mistry
 * @since 2025-12-30
 */
@Getter
@Setter
@ToString
public class ScriptModifyDTO implements Serializable {

    /**
     * 话术ID
     */
    @Null(message = "Body中禁止ID传参，请在URL中指定ID")
    private String id;

    /**
     * 话术名
     */
    @NotBlankIfPresent(message = "节点名不得为空")
    private String name = SCRIPT_CONSTANT.DEFAULT_NAME;

    /**
     * 话术摘要
     */
    @Length(min = 1, max = 128, message = "话术摘要长度需要在1~128之间")
    private String summary;

    /**
     * 话术内容
     */
    @NotBlankIfPresent(message = "话术内容不得为空")
    private String content;

    /**
     * 话术类型:节点话术/边话术/通用话术
     */
    @NotBlankIfPresent(message = "话术内容不得为空")
    private String type;

    /**
     * 关联ID，若为通用话术，为 NULL
     */
    @EnumValid(target = SCRIPT_CONSTANT.class, message = "请求类型为枚举类，输入无效")
    private String associateId;

    /**
     * 话术标签
     */
    private String tag;

    /**
     * 优先级，1-100，数字越小优先级越高
     */
    @Min(value = 1, message = "优先级必须为 1~100 之间的整数")
    @Max(value = 100, message = "优先级必须为 1~100 之间的整数")
    private Byte priority;

    /**
     * 话术筛选条件JSON
     */
    @JsonDeserialize(using = JsonDataDeserializer.class)
    @JsonSerialize(using = JsonDataSerializer.class)
    @IsValidJSON(message = "话术触发条件必须是有效JSON")
    private String conditions;
}
