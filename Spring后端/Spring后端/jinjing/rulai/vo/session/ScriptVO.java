package rulai.vo.session;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * <p>
 * 话术内容VO
 * </p>
 *
 * @author Mistry
 * @since 2025-12-30
 */
@Getter
@Setter
@ToString
public class ScriptVO implements Serializable {

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
     * 话术筛选结果
     */
    private Boolean enabled;

    /**
     * 一个浮点数，表示AI给出的推荐度，越接近1表示越推荐
     */
    private Float score;
}
