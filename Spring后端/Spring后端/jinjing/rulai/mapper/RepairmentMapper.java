package rulai.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import rulai.entity.Repairment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import rulai.vo.table.CostTableVO;
import rulai.vo.table.RepairmentTableVO;

import java.util.List;

/**
* @author 22076
* @description 针对表【repairment(维修订单信息)】的数据库操作Mapper
* @createDate 2026-01-05 10:12:45
* @Entity rulai.rulai.Repairment
*/
@Mapper
public interface RepairmentMapper extends BaseMapper<Repairment> {
    IPage<RepairmentTableVO> selectRepairmentPage( IPage<RepairmentTableVO> page,
                                                   @Param("ew") Wrapper<RepairmentTableVO> wrapper);
    List<RepairmentTableVO> selectRepairmentList(@Param("ew") Wrapper<RepairmentTableVO> wrapper);
}




