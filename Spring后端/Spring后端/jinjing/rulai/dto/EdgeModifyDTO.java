package rulai.dto;

import jakarta.validation.constraints.Null;
import lombok.*;
import rulai.annotation.IsValidJSON;
import rulai.annotation.NotBlankIfPresent;
import rulai.constant.EDGE_CONSTANT;

import java.io.Serializable;

/**
 * <p>
 * 文件管理 DTO
 * </p>
 *
 * @author Mistry
 * @since 2025-12-29
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class EdgeModifyDTO implements Serializable {
    /**
     * 边ID
     */
    @Null(message = "Body中禁止ID传参，请在URL中指定ID")
    private String id;

    /**
     * 起点UUID
     */
    @NotBlankIfPresent(message = "边类型不得为空")
    private String type=EDGE_CONSTANT.DEFAULT_TYPE;

    @NotBlankIfPresent(message = "起点不得为空")
    private String sourceId ;
    /**
     * 终点UUID
     */
    @NotBlankIfPresent(message = "终点不得为空")
    private String targetId;

    @NotBlankIfPresent(message = "边名不得为空")
    private String name= EDGE_CONSTANT.DEFAULT_NAME;

    @IsValidJSON(message = "转移条件必须是有效JSON")
    private String conditions;

    /**
     * 绘图所用的其他数据，以JSON储存
     */
    @IsValidJSON(message = "绘图参数必须是有效JSON")
    private String graphical;

    /**
     * 边描述
     */
    private String description;
}
