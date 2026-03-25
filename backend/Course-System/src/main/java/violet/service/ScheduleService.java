package violet.service;

import violet.pojo.ScheduleVO;

import java.util.List;

/**
 * 课表查询业务接口（按周）。
 */
public interface ScheduleService {

    /**
     * 按班级查询课表（按周过滤）。
     */
    List<ScheduleVO> listByClass(Integer classId, Integer week);

    /**
     * 按教师查询课表（按周过滤）。
     */
    List<ScheduleVO> listByTeacher(Integer teacherId, Integer week);

    /**
     * 按教室查询课表（按周过滤，week 为 null 则不过滤周次）。
     */
    List<ScheduleVO> listByClassroom(Integer classroomId, Integer week);
}

