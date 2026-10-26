package rulai.utils;

import java.util.HashMap;
import java.util.Map;

public class PermissionUtils {
    // 权限掩码映射
    public static final Map<Integer, String> PERMISSION_MAP = new HashMap<>();

    static {


        PERMISSION_MAP.put(1<<0, "USER_DRIVER");
        PERMISSION_MAP.put(1<<1, "ADMIN_VEHICLE");
        PERMISSION_MAP.put(1<<2, "USER_MAINTAIN");
        PERMISSION_MAP.put(1<<3, "PASSENGER");
        PERMISSION_MAP.put(1<<4, "ADMIN_SUPER");
    }

    /**
     * 检查权限值是否包含特定权限
     * @param permissionValue 权限值
     * @param permissionName 权限名称
     * @return 是否包含该权限
     */
    public static boolean hasPermission(int permissionValue, String permissionName) {
        for (Map.Entry<Integer, String> entry : PERMISSION_MAP.entrySet()) {
            if (entry.getValue().equals(permissionName)) {
                return (permissionValue & entry.getKey()) != 0;
            }
        }
        return false;
    }

    /**
     * 检查权限值是否包含特定权限掩码
     * @param permissionValue 权限值
     * @param permissionMask 权限掩码
     * @return 是否包含该权限
     */
    public static boolean hasPermission(int permissionValue, int permissionMask) {
        return (permissionValue & permissionMask) != 0;
    }

    /**
     * 获取权限值中包含的所有权限名称
     * @param permissionValue 权限值
     * @return 包含的权限名称列表
     */
    public static String[] getPermissionNames(int permissionValue) {
        return PERMISSION_MAP.entrySet().stream()
                .filter(entry -> (permissionValue & entry.getKey()) != 0)
                .map(Map.Entry::getValue)
                .toArray(String[]::new);
    }

    /**
     * 获取权限值中包含的所有权限掩码
     * @param permissionValue 权限值
     * @return 包含的权限掩码列表
     */
    public static int[] getPermissionMasks(int permissionValue) {
        return PERMISSION_MAP.keySet().stream()
                .filter(mask -> (permissionValue & mask) != 0)
                .mapToInt(Integer::intValue)
                .toArray();
    }

    /**
     * 添加权限到权限值
     * @param permissionValue 原始权限值
     * @param permissionName 要添加的权限名称
     * @return 新的权限值
     */
    public static int addPermission(int permissionValue, String permissionName) {
        for (Map.Entry<Integer, String> entry : PERMISSION_MAP.entrySet()) {
            if (entry.getValue().equals(permissionName)) {
                return permissionValue | entry.getKey();
            }
        }
        return permissionValue;
    }

    /**
     * 从权限值中移除权限
     * @param permissionValue 原始权限值
     * @param permissionName 要移除的权限名称
     * @return 新的权限值
     */
    public static int removePermission(int permissionValue, String permissionName) {
        for (Map.Entry<Integer, String> entry : PERMISSION_MAP.entrySet()) {
            if (entry.getValue().equals(permissionName)) {
                return permissionValue & ~entry.getKey();
            }
        }
        return permissionValue;
    }
}
