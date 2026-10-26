package rulai.dto;

import lombok.Data;

@Data
public class PageQueryParam {
    private Integer current = 1;
    private Integer size = 10;
    private String sortField;
    private String sortOrder = "desc";

    // 通用搜索字段
    private String keyword;
}