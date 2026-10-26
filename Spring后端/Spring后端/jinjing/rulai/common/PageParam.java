package rulai.common;

import lombok.Data;

import java.util.HashMap;

/**
 * <p>
 * 分页请求参数类
 * </p>
 *
 * @author Mistry
 * @since 2025-06-23
 */

@Data
public class PageParam {
    private static int PAGE_SIZE = 10;
    private static int PAGE_NUM = 1;

    private Integer pageSize=PAGE_SIZE;
    private Integer pageNum=PAGE_NUM;
    private HashMap<String, Object> param=null;
}
