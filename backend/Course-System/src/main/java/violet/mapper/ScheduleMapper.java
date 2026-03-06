package violet.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import violet.pojo.ScheduleVO;

import java.util.List;

/**
 * 排课总表 Mapper（按周查询课表）。
 * <p>
 * 多表联查使用 XML 动态 SQL。
 * </p>
 */
@Mapper
public interface ScheduleMapper {

    /**
     * 按周查询课表列表（支持按班级或教师过滤）。
     *
     * @param week      周次（对应 Schedule_Week）
     * @param classId   班级 ID（可选）
     * @param teacherId 教师 ID（可选）
     * @return 课表列表
     */
    List<ScheduleVO> findScheduleList(@Param("week") Integer week,
                                      @Param("classId") Integer classId,
                                      @Param("teacherId") Integer teacherId);
}

