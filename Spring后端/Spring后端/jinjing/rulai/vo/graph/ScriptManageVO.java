package rulai.vo.graph;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import rulai.common.JsonDataDeserializer;
import rulai.common.JsonDataSerializer;

import java.io.Serializable;

/**
 * <p>
 * 话术内容表
 * </p>
 *
 * @author Mistry
 * @since 2026-01-05
 */
@Getter
@Setter
@ToString
public class ScriptManageVO implements Serializable {

    /**
     * 话术ID
     */
    private String id;

    /**
     * 话术名
     */
    private String name;

    /**
     * 话术摘要
     */
    private String summary;

    /**
     * 话术内容
     */
    private String content;

    /**
     * 话术类型:节点话术/反馈话术/通用话术
     */
    private String type;

    /**
     * 关联ID，若为通用话术，为 NULL
     */
    private String associateId;

    /**
     * 话术标签
     */
    private String tag;

    /**
     * 优先级，1-100，数字越小优先级越高
     */
    private Byte priority;

    /**
     * 话术筛选条件JSON
     */
    @JsonDeserialize(using = JsonDataDeserializer.class)
    @JsonSerialize(using = JsonDataSerializer.class)
    private String conditions;
}
