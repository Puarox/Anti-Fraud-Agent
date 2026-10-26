package rulai.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 外键检查器，你别看一堆爆红，就是能跑
 * </p>
 *
 * @author Mistry
 * @since 2025-06-27
 */
@Mapper
public interface ForeignKeyCheckerMapper {
    /**
     * 检查外键是否存在
     * @param tableName 表名（需经过白名单校验）
     * @param id 要检查的ID
     * @return 是否存在（true:存在, false:不存在）
     */
    @Select("SELECT EXISTS(SELECT 1 FROM `${tableName}` WHERE id = #{id}) AS exists_flag")
    boolean checkForeignKeyExists(
            @Param("tableName") String tableName,
            @Param("id") String id);
    /**
     * 检查外键是否存在
     * @param tableName 表名（需经过白名单校验）
     * @param pk 要检查的外键名
     * @param id 要检查的ID
     * @return 是否存在（true:存在, false:不存在）
     */
    @Select("SELECT EXISTS(SELECT 1 FROM `${tableName}` WHERE `${pk}` = #{id}) AS exists_flag")
    boolean checkForeignKeyExistsWithPK(
            @Param("tableName") String tableName,
            @Param("pk") String pk,
            @Param("id") String id);
}