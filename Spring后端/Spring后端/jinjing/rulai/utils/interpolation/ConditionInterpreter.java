package rulai.utils.interpolation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rulai.constant.INTERPRETER_CONSTANT;
import rulai.service.IInterfaceService;

import java.util.*;
import java.util.regex.*;

/**
 * <p>
 * Condition 解释器主类
 * </p>
 *
 * @author Mistry
 * @since 2025-1-5
 */

@Component
public class ConditionInterpreter {

    @Autowired
    IInterfaceService interpreterService;

    /**
     * 解析条件表达式数组
     * @param conditions 条件表达式数组
     * @return 是否满足所有条件
     */
    public boolean evaluate(List<Map<String, Object>> conditions, Object ...arg) {
        if (conditions == null || conditions.isEmpty()) {
            return true;
        }

        for (Map<String, Object> condition : conditions) {
            if (!evaluateExpression(condition, arg)) {
                return false;
            }
        }
        return true;
    }


    /**
     * 计算AND条件
     */
    private boolean evaluateAnd(Map<String, Object> expr, Object ...arg) {
        List<Map<String, Object>> subConditions = (List<Map<String, Object>>) expr.get("sub");
        for (Map<String, Object> condition : subConditions) {
            if (!evaluateExpression(condition, arg)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 计算OR条件
     */
    private boolean evaluateOr(Map<String, Object> expr, Object ...arg) {
        List<Map<String, Object>> subConditions = (List<Map<String, Object>>) expr.get("sub");
        for (Map<String, Object> condition : subConditions) {
            if (evaluateExpression(condition, arg)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 计算NOT条件
     */
    private boolean evaluateNot(Map<String, Object> expr, Object ...arg) {
        Object subObj = expr.get("sub");

        if (subObj instanceof List) {
            List<Map<String, Object>> subList = (List<Map<String, Object>>) subObj;
            if (subList.size() != 1) {
                throw new RuntimeException("NOT条件只能有一个子条件");
            }
            return !evaluateExpression(subList.get(0), arg);
        } else {
            Map<String, Object> subCondition = (Map<String, Object>) subObj;
            return !evaluateExpression(subCondition, arg);
        }
    }

    /**
     * 递归计算表达式
     */
    private boolean evaluateExpression(Map<String, Object> expr, Object ...arg) {
        String type = (String) expr.get("type");
        INTERPRETER_CONSTANT.EXPR_TYPE exprType = INTERPRETER_CONSTANT.EXPR_TYPE.valueOf(type.toUpperCase());

        switch (exprType) {
            case ATOMIC:
                return evaluateAtomic(expr, arg);
            case AND:
                return evaluateAnd(expr, arg);
            case OR:
                return evaluateOr(expr, arg);
            case NOT:
                return evaluateNot(expr, arg);
            default:
                throw new IllegalArgumentException("不支持的表达式类型: " + type);
        }
    }

    /**
     * 解析原子条件
     */
    private boolean evaluateAtomic(Map<String, Object> atomicExpr, Object ...arg) {
        Object lhsObj = atomicExpr.get("lhs");
        Object rhsObj = atomicExpr.get("rhs");
        String operation = (String) atomicExpr.get("op");
        if(operation == null || operation.isEmpty()) {
            operation = (String) atomicExpr.get("operation");
        }

        if(operation == null || operation.isBlank()){
            return true;
        }

        Object leftValue = parseOperand(lhsObj, arg);
        Object rightValue = parseOperand(rhsObj, arg);

        INTERPRETER_CONSTANT.OPERATION op = INTERPRETER_CONSTANT.OPERATION.valueOf(operation.toUpperCase());

        return compareValues(leftValue, rightValue, op);
    }

    /**
     * 解析操作数
     */
    private Object parseOperand(Object operand, Object ...arg) {
        if (operand == null) {
            return null;
        }

        if (operand instanceof String) {
            String strValue = (String) operand;

            Pattern pattern = Pattern.compile("^\\{(.*)\\}$");
            Matcher matcher = pattern.matcher(strValue);

            if (matcher.matches()) {
                String interfaceName = matcher.group(1);
                return interpreterService.call(interfaceName, arg);
            }
            return strValue;
        }

        if (operand instanceof List) {
            return operand;
        }

        return operand;
    }

    /**
     * 比较两个值
     */
    private boolean compareValues(Object left, Object right, INTERPRETER_CONSTANT.OPERATION op) {
        if (left == null || right == null) {
            return compareNullValues(left, right, op);
        }

        switch (op) {
            case EQ:
                return Objects.equals(left, right);
            case NE:
                return !Objects.equals(left, right);
            case GT:
            case GE:
            case LT:
            case LE:
                return compareNumbers(left, right, op);
            case IN:
                return checkInOperation(left, right);
            case CONTAINS:
                return checkContainsOperation(left, right);
            case STARTSWITH:
                return checkStartsWith(left, right);
            case ENDSWITH:
                return checkEndsWith(left, right);
            default:
                throw new IllegalArgumentException("不支持的操作符: " + op);
        }
    }

    /**
     * 处理null值的比较
     */
    private boolean compareNullValues(Object left, Object right, INTERPRETER_CONSTANT.OPERATION op) {
        switch (op) {
            case EQ:
                return left == null && right == null;
            case NE:
                return left != null || right != null;
            default:
                return false;
        }
    }

    /**
     * 数字比较
     */
    private boolean compareNumbers(Object left, Object right, INTERPRETER_CONSTANT.OPERATION op) {
        if (left instanceof Integer && right instanceof Integer) {}
        double leftNum = toNumber(left);
        double rightNum = toNumber(right);

        switch (op) {
            case GT:
                return leftNum > rightNum;
            case GE:
                return leftNum >= rightNum;
            case LT:
                return leftNum < rightNum;
            case LE:
                return leftNum <= rightNum;
            default:
                return false;
        }
    }

    /**
     * 转换为数字
     */
    private double toNumber(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        } else if (obj instanceof String) {
            try {
                return Double.parseDouble((String) obj);
            } catch (NumberFormatException e) {
                throw new RuntimeException("无法转换为数字: " + obj);
            }
        } else if (obj instanceof Boolean) {
            return ((Boolean) obj) ? 1.0 : 0.0;
        } else {
            throw new RuntimeException("不支持的数字类型: " + obj.getClass());
        }
    }

    /**
     * 检查是否包含于
     */
    private boolean checkInOperation(Object left, Object right) {
        if (!(right instanceof List)) {
            throw new RuntimeException("IN操作的右操作数必须是数组");
        }

        List<?> rightList = (List<?>) right;
        return rightList.contains(left);
    }

    /**
     * 检查是否包含
     */
    private boolean checkContainsOperation(Object left, Object right) {
        if (!(left instanceof List)) {
            throw new RuntimeException("CONTAINS操作的左操作数必须是数组");
        }

        List<?> leftList = (List<?>) left;
        return leftList.contains(right);
    }

    /**
     * 检查是否以指定字符串开头
     */
    private boolean checkStartsWith(Object left, Object right) {
        String leftStr = left.toString();
        String rightStr = right.toString();
        return leftStr.startsWith(rightStr);
    }

    /**
     * 检查是否以指定字符串结尾
     */
    private boolean checkEndsWith(Object left, Object right) {
        String leftStr = left.toString();
        String rightStr = right.toString();
        return leftStr.endsWith(rightStr);
    }

    public Map<String, List<String>> check(List<Map<String, Object>> expr) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        String path = "root";
        int i = 0;
        for (Map<String, Object> map : expr) {
            checkExpression(map, errors, warnings, path + String.format("[%d]", i++));
        }
        Map<String, List<String>> result = new HashMap<>();
        result.put("errors", errors);
        result.put("warnings", warnings);
        return result;
    }

    /**
     * 检查单个表达式
     */
    private void checkExpression(Map<String, Object> expr, List<String> errors, List<String> warnings,
                                 String path) {
        if (expr == null) {
            errors.add(path + ": 表达式不能为null");
            return;
        }

        String type = (String) expr.get("type");
        if (type == null) {
            errors.add(path + ": 缺少表达式类型(type)字段");
            return;
        }

        try {
            INTERPRETER_CONSTANT.EXPR_TYPE exprType = INTERPRETER_CONSTANT.EXPR_TYPE.valueOf(type.toUpperCase());

            switch (exprType) {
                case ATOMIC:
                    checkAtomicExpression(expr, errors, warnings, path);
                    break;
                case AND:
                case OR:
                    checkLogicalExpression(expr, errors, warnings, path, exprType);
                    break;
                case NOT:
                    checkNotExpression(expr, errors, warnings, path);
                    break;
                default:
                    errors.add(path + ": 不支持的表达式类型: " + type);
            }
        } catch (IllegalArgumentException e) {
            errors.add(path + ": 无效的表达式类型: " + type +
                    "，有效类型: " + Arrays.toString(INTERPRETER_CONSTANT.EXPR_TYPE.values()));
        }
    }

    /**
     * 检查原子表达式
     */
    private void checkAtomicExpression(Map<String, Object> atomicExpr, List<String> errors,
                                       List<String> warnings, String path) {
        // 检查操作符
        String operation = (String) atomicExpr.get("op");
        if(operation == null || operation.isEmpty()) {
            operation = (String) atomicExpr.get("operation");
        }

        if (operation == null || operation.isBlank()) {
            warnings.add(path + ": 操作符为空，将始终返回true");
            return;
        }

        try {
            INTERPRETER_CONSTANT.OPERATION op = INTERPRETER_CONSTANT.OPERATION.valueOf(operation.toUpperCase());

            Object lhs = atomicExpr.get("lhs");
            checkOperand(lhs, errors, warnings, path + ".lhs", op, true);

            Object rhs = atomicExpr.get("rhs");
            checkOperand(rhs, errors, warnings, path + ".rhs", op, false);

            if (lhs != null && rhs != null) {
                checkOperandCompatibility(lhs, rhs, op, errors, path);
            }

        } catch (IllegalArgumentException e) {
            errors.add(path + ": 无效的操作符: " + operation +
                    "，有效操作符: " + Arrays.toString(INTERPRETER_CONSTANT.OPERATION.values()));
        }
    }

    /**
     * 检查操作数
     */
    private void checkOperand(Object operand, List<String> errors, List<String> warnings,
                              String path, INTERPRETER_CONSTANT.OPERATION op, boolean isLeft) {
        if (operand == null) {
            // 对于某些操作符，null是允许的
            if (op == INTERPRETER_CONSTANT.OPERATION.EQ || op == INTERPRETER_CONSTANT.OPERATION.NE) {
                warnings.add(path + ": 操作数为null");
            } else {
                errors.add(path + ": 操作数不能为null（操作符" + op + "不支持null操作数）");
            }
            return;
        }

        if (operand instanceof String) {
            String strValue = (String) operand;
            Pattern pattern = Pattern.compile("^\\{(.*)\\}$");
            Matcher matcher = pattern.matcher(strValue);

            if (matcher.matches()) {
                String interfaceName = matcher.group(1);

                if(!InterfaceRegistry.isRegistered(interfaceName)){
                    errors.add(path + ": 占位符{" + interfaceName + "}无效");
                }
            }
        } else if (operand instanceof List) {
            if (isLeft && op != INTERPRETER_CONSTANT.OPERATION.CONTAINS) {
                errors.add(path + ": 左操作数为列表，但操作符" + op + "不支持列表左操作数（应使用CONTAINS）");
            } else if (!isLeft && op != INTERPRETER_CONSTANT.OPERATION.IN) {
                errors.add(path + ": 右操作数为列表，但操作符" + op + "不支持列表右操作数（应使用IN）");
            }
        } else if (!(operand instanceof Number || operand instanceof Boolean)) {
            warnings.add(path + ": 操作数类型为" + operand.getClass().getSimpleName() +
                    "，可能不支持某些操作");
        }
    }

    /**
     * 检查操作数兼容性
     */
    private void checkOperandCompatibility(Object left, Object right, INTERPRETER_CONSTANT.OPERATION op,
                                           List<String> errors, String path) {
        if (op == INTERPRETER_CONSTANT.OPERATION.GT || op == INTERPRETER_CONSTANT.OPERATION.GE ||
                op == INTERPRETER_CONSTANT.OPERATION.LT || op == INTERPRETER_CONSTANT.OPERATION.LE) {

            boolean leftIsNumber = left instanceof Number ||
                    (left instanceof String && isNumeric((String) left));
            boolean rightIsNumber = right instanceof Number ||
                    (right instanceof String && isNumeric((String) right));

            if (!leftIsNumber) {
                errors.add(path + ": 左操作数'" + left + "'无法转换为数字，但操作符" + op + "要求数字类型");
            }
            if (!rightIsNumber) {
                errors.add(path + ": 右操作数'" + right + "'无法转换为数字，但操作符" + op + "要求数字类型");
            }
        }
    }

    /**
     * 检查逻辑表达式
     */
    private void checkLogicalExpression(Map<String, Object> expr, List<String> errors,
                                        List<String> warnings,
                                        String path, INTERPRETER_CONSTANT.EXPR_TYPE type) {
        Object subObj = expr.get("sub");
        if (subObj == null) {
            errors.add(path + ": " + type + "表达式缺少sub字段");
            return;
        }

        if (!(subObj instanceof List)) {
            errors.add(path + ": " + type + "表达式的sub字段必须是数组");
            return;
        }

        List<Map<String, Object>> subConditions = (List<Map<String, Object>>) subObj;
        if (subConditions.isEmpty()) {
            warnings.add(path + ": " + type + "表达式的子条件列表为空");
            return;
        }

        if (type == INTERPRETER_CONSTANT.EXPR_TYPE.OR && subConditions.size() == 1) {
            warnings.add(path + ": OR表达式只有一个子条件，建议直接使用该子条件");
        }

        for (int i = 0; i < subConditions.size(); i++) {
            checkExpression(subConditions.get(i), errors, warnings,
                    path + ".sub[" + i + "]");
        }
    }

    /**
     * 检查NOT表达式
     */
    private void checkNotExpression(Map<String, Object> expr, List<String> errors,
                                    List<String> warnings, String path) {
        Object subObj = expr.get("sub");
        if (subObj == null) {
            errors.add(path + ": NOT表达式缺少sub字段");
            return;
        }

        if (subObj instanceof List) {
            List<Map<String, Object>> subList = (List<Map<String, Object>>) subObj;
            if (subList.size() != 1) {
                errors.add(path + ": NOT表达式只能有一个子条件，当前有" + subList.size() + "个");
                return;
            }
            checkExpression(subList.get(0), errors, warnings, path + ".sub[0]");
        } else if (subObj instanceof Map) {
            checkExpression((Map<String, Object>) subObj, errors, warnings, path + ".sub");
        } else {
            errors.add(path + ": NOT表达式的sub字段必须是单个条件对象或包含单个条件的数组");
        }
    }

    /**
     * 判断字符串是否为数字
     */
    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}