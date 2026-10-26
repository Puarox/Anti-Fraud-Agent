package rulai.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import rulai.httpstatus.exceptions.BadRequestException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import static com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank;
import static rulai.utils.NamingConverter.camelToSnake;

/**
 * <p>
 * 帮助链式书写 Query 的一个帮助类
 * </p>
 *
 * @author Mistry
 * @since 2025-06-23
 */
public class QueryHelper {
    public final static String EQ = "EQ";
    public final static String NE = "NE";
    public final static String LIKE = "LIKE";
    public final static String NOT_LIKE = "NOT_LIKE";
    public final static String GT = "GT";
    public final static String GE = "GE";
    public final static String LT = "LT";
    public final static String LE = "LE";
    public final static String IN = "IN";
    public final static String NOT_IN = "NOT_IN";
    public final static String IS_NULL = "IS_NULL";
    public final static String IS_NOT_NULL = "IS_NOT_NULL";

    public final static String ORDER_KEYS = "ORDER_KEYS";

    public final static String ASCENDING = "ASC";

    public final static String DESCENDING = "DESC";

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void handle(QueryWrapper queryWrapper, HashMap<String, Object> param) {
        if (param == null) {
            return;
        }

        queryWrapper.apply("1=1");

        Object orders = null;
        for (String key : param.keySet()) {
            if (ORDER_KEYS.equalsIgnoreCase(key)) {
                orders = param.get(ORDER_KEYS);
                continue;
            }

            String[] parts = key.split(":");
            String type;
            String field;
            if (parts.length == 1) {
                type = EQ;
                field = camelToSnake(parts[0].strip());
            } else if (parts.length == 2) {
                type = parts[0].strip().toUpperCase();
                field = camelToSnake(parts[1].strip());
            } else {
                throw new BadRequestException(String.format("请求条件%s不符合格式：type:field", key));
            }
            switch (type) {
                case (IS_NULL), (IS_NOT_NULL): {
                    switch (type) {
                        case (IS_NULL) -> queryWrapper.isNull(key);
                        case (IS_NOT_NULL) -> queryWrapper.isNotNull(key);
                    }
                    break;
                }
                default: {
                    if(param.get(key) != null) {
                        switch (type) {
                            case (""), (EQ) -> queryWrapper.eq(field, param.get(key));
                            case (NE) -> queryWrapper.ne(field, param.get(key));
                            case (LIKE) -> queryWrapper.and(
                                    (Consumer<QueryWrapper<?>>) wrapper -> wrapper.eq(field, param.get(key)).or().like(field, param.get(key))
                            );
                            case (NOT_LIKE) -> {
                                queryWrapper.ne(field, param.get(key));
                                queryWrapper.notLike(field, param.get(key));
                            }
                            case (GT) -> queryWrapper.gt(field, param.get(key));
                            case (GE) -> queryWrapper.ge(field, param.get(key));
                            case (LT) -> queryWrapper.lt(field, param.get(key));
                            case (LE) -> queryWrapper.le(field, param.get(key));
                            case (IN) -> queryWrapper.in(field, (List) param.get(key));
                            case (NOT_IN) -> queryWrapper.notIn(field, (List) param.get(key));
                            default -> throw new BadRequestException(String.format("条件类型%s不被支持：type:field", key));
                        }
                    }
                }
            }
        }

        if (orders == null) {
            return;
        }

        ArrayList<String> orderList;
        try {
            orderList = (ArrayList<String>) orders;
        } catch (Exception e) {
            try {
                orderList = new ArrayList();
                orderList.add((String) orders);
            } catch (Exception ex) {
                throw new BadRequestException(String.format("排序条件%s不符合格式", orders));
            }
        }

        for (String order : orderList) {
            if(order!=null) {
                String[] parts = order.split(":");
                if (parts.length == 1) {
                    String field = camelToSnake(parts[0].strip());
                    queryWrapper.orderBy(true, true, field);
                } else {
                    String ascOrDesc = parts[0].strip().toUpperCase();
                    String field = camelToSnake(parts[1].strip());
                    if(!field.isBlank()) {
                        if (ascOrDesc.equals(ASCENDING) || isBlank(ascOrDesc)) {
                            queryWrapper.orderBy(true, true, field);
                        } else if (ascOrDesc.equals(DESCENDING)) {
                            queryWrapper.orderBy(true, false, field);
                        } else {
                            throw new BadRequestException(String.format("升降序格式%s不符合格式", order));
                        }
                    }
                }
            }
        }

    }
}
