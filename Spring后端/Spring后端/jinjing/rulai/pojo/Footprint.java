package rulai.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * <p>
 * 服务轨迹单步实体
 * </p>
 *
 * @author Mistry
 * @since 2025-12-31
 */

@Getter
@Setter
@ToString
public class Footprint {
    /**
     * 到达点ID
     */
    private String nodeId;

    /**
     * 经过边ID
     */
    private String edgeId;

    /**
     * 痕迹类型
     */
    private String type;

    /**
     * 痕迹产生时间
     */
    private LocalDateTime time;
}
