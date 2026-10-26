package rulai.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import rulai.annotation.IsValidJSON;
import rulai.constant.NODE_CONSTANT;

import java.io.Serializable;

/**
 * <p>
 * 服务流程节点表
 * </p>
 *
 * @author Mistry
 * @since 2025-12-29
 */
@Getter
@Setter
@ToString
public class NodeCreateDTO implements Serializable {

    /**
     * 节点唯一ID
     */
    @Null(message = "Body中禁止ID传参，请在URL中指定ID")
    private String id;

    /**
     * 节点类型
     */
    @NotNull(message = "节点类型不得为空")
    private String type= NODE_CONSTANT.DEFAULT_TYPE;

    /**
     * 节点名
     */
    @NotNull(message = "节点名不得为空")
    private String name=NODE_CONSTANT.DEFAULT_NAME;
    /**
     * 节点X坐标
     */
    private Double x=1.0;

    /**
     * 节点Y坐标
     */
    private Double y=1.0;

    /**
     * 绘图所用的其他数据，以JSON储存
     */
    @IsValidJSON(message = "绘图参数必须是有效JSON")
    private String graphical;

    /**
     * 节点描述
     */
    private String description;

    /**
     * 节点支持的操作配置，以JSON储存
     */
    @IsValidJSON(message = "节点操作必须是有效JSON")
    private String operations;
}

