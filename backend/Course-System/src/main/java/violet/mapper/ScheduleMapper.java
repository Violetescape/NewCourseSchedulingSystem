package violet.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import violet.pojo.Schedule;
import violet.pojo.ScheduleRawDTO;
import violet.pojo.ScheduleVO;

import java.util.List;

/**
 * 排课总表 Mapper（按周查询课表、冲突初始化、插入）。
 * <p>
 * 多表联查使用 XML 动态 SQL。
 * </p>
 */
@Mapper
public interface ScheduleMapper {

    /**
     * 按周查询课表列表（支持按班级、教师或教室过滤）。
     *
     * @param week        周次（对应 Schedule_Week，null 表示不过滤，返回全量）
     * @param classId     班级 ID（可选）
     * @param teacherId   教师 ID（可选）
     * @param classroomId 教室 ID（可选）
     * @return 课表列表
     */
    List<ScheduleVO> findScheduleList(@Param("week") Integer week,
                                      @Param("classId") Integer classId,
                                      @Param("teacherId") Integer teacherId,
                                      @Param("classroomId") Integer classroomId);

    /**
     * 查询所有排课原始记录（用于冲突检测模型初始化）。
     * 关联 course 表获取 courseSingleHour。
     *
     * @return 排课原始记录列表
     */
    List<ScheduleRawDTO> findAllRawForConflict();

    /**
     * 插入一条排课记录。
     *
     * @param schedule 排课记录
     */
    @Insert("insert into schedule(Schedule_Class_ID, Schedule_Teacher_ID, Schedule_Course_ID, " +
            "Schedule_Classroom_ID, Schedule_Weekday, Schedule_Section, Schedule_Week) " +
            "values(#{scheduleClassId}, #{scheduleTeacherId}, #{scheduleCourseId}, " +
            "#{scheduleClassroomId}, #{scheduleWeekday}, #{scheduleSection}, #{scheduleWeek})")
    void insert(Schedule schedule);
}

