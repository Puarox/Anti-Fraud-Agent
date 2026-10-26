package rulai.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
public class Script implements Serializable {

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
    private String conditions;

    /**
     * 逻辑删除标识（0-未删除，1-已删除）
     */
    @TableLogic
    private Integer isDeleted;
}
