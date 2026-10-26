package rulai.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import rulai.httpstatus.exceptions.BadRequestException;
import rulai.mapper.ForeignKeyCheckerMapper;
import rulai.service.IForeignKeyCheckerService;

import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * 系统附件信息 服务实现类
 * </p>
 *
 * @author Mistry
 * @since 2025-06-23
 */
@Service
@ConfigurationProperties(prefix = "foreign-key-check")
public class ForeignKeyCheckerServiceImpl implements IForeignKeyCheckerService {

    @Autowired
    ForeignKeyCheckerMapper foreignKeyCheckerMapper;

    private Set<String> allowedTables = new HashSet<>();

    public void setAllowedTables(Set<String> allowedTables) {
        this.allowedTables = new HashSet<>(allowedTables);
    }

    @Override
    public boolean checkForeignKey(String tableName, String id) {
        if(tableName == null || tableName.strip() == "") {
            return true;
        }
        System.out.println("tableName = " + tableName + ", id = " + id);
        if(foreignKeyCheckerMapper.checkForeignKeyExists(tableName, id)) {
            return true;
        } else {
            throw new BadRequestException(String.format("关联表 %s 中找不到对象 %s = %s", tableName, tableName+"_id", id));
        }
    }

    @Override
    public boolean checkForeignKey(String tableName, String pk, String id) {
        if(tableName == null || tableName.strip() == "") {
            return true;
        }
//        System.out.println("tableName = " + tableName + ", pk = " + pk + ", id = " + id);
        if (!allowedTables.contains(tableName)) {
            throw new BadRequestException("未授权的表访问: " + tableName);
        } else if(foreignKeyCheckerMapper.checkForeignKeyExistsWithPK(tableName, pk, id)) {
            return true;
        } else {
            throw new BadRequestException(String.format("关联表 %s 中找不到对象 %s = %s", tableName, pk, id));
        }
    }

    @Override
    public boolean hasPredecessors(String tableName, String id) {
        return foreignKeyCheckerMapper.checkForeignKeyExistsWithPK(tableName, "associateId", id);
    }

    @Override
    public boolean hasPredecessors(String tableName, String fk, String id) {
        return foreignKeyCheckerMapper.checkForeignKeyExistsWithPK(tableName, fk, id);
    }
}
