package rulai.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * <p>
 * API返回工厂类
 * </p>
 *
 * @author Mistry
 * @since 2025-06-23
 */


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class APIResponse<T> {
    private Boolean success;
    private String message;
    private T data;
    private Pagination meta;

    public APIResponse() {
    }

    private APIResponse(Boolean success, String message, T data, Pagination meta) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.meta = meta;
    }

    // 分页
    @Data
    public static class Pagination {
        private Long total;
        private Integer page;
        private Integer size;

        public Pagination(Long total, Integer page, Integer size) {
            this.total = total; // 总记录数
            this.page = page; // 当前页
            this.size = size; // 页大小
        }
    }

    /* ========== 成功响应 ========== */
    public static <T> APIResponse<T> success() {
        return new APIResponse<>(true, "Operation succeeded", null, null);
    }

    public static <T> APIResponse<T> success(T data) {
        return new APIResponse<>(true, "Operation succeeded", data, null);
    }

    public static <T> APIResponse<T> paginated(Long total, Integer page, Integer size, T data) {
        return new APIResponse<>(true, "Data retrieved",
                data, new Pagination(total, page, size));
    }

    /* ========== 错误响应 ========== */
    public static APIResponse<Void> error(String message) {
        return new APIResponse<>(false, message, null, null);
    }
}
