package rulai.service;


public interface IForeignKeyCheckerService {

     boolean checkForeignKey(String tableName, String id);

     boolean checkForeignKey(String tableName, String pk, String id);

     boolean hasPredecessors(String tableName, String id);

     boolean hasPredecessors(String tableName, String fk, String id);

}
