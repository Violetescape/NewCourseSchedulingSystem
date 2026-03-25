package violet.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import violet.pojo.Result;
import violet.pojo.ScheduleVO;
import violet.service.ScheduleService;

import java.util.List;

/**
 * 课表查询接口（按周）。
 * <p>
 * - GET /schedules/class/{classId}?week={week}
 * - GET /schedules/teacher/{teacherId}?week={week}
 * </p>
 */
@RestController
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    /**
     * 按班级查询课表（按周过滤）。
     */
    @GetMapping("/schedules/class/{classId}")
    public Result listByClass(@PathVariable("classId") Integer classId,
                              @RequestParam(required = false) Integer week) {
        System.out.println("按班级查询课表 classId=" + classId + ", week=" + week);
        List<ScheduleVO> list = scheduleService.listByClass(classId, week);
        return Result.success(list);
    }

    /**
     * 按教师查询课表（按周过滤）。
     */
    @GetMapping("/schedules/teacher/{teacherId}")
    public Result listByTeacher(@PathVariable("teacherId") Integer teacherId,
                                @RequestParam(required = false) Integer week) {
        List<ScheduleVO> list = scheduleService.listByTeacher(teacherId, week);
        return Result.success(list);
    }

    /**
     * 按教室查询课表（week 不传或为 null 则返回全量，用于全局聚合课表）。
     */
    @GetMapping("/schedules/classroom/{classroomId}")
    public Result listByClassroom(@PathVariable("classroomId") Integer classroomId,
                                  @RequestParam(required = false) Integer week) {
        List<ScheduleVO> list = scheduleService.listByClassroom(classroomId, week);
        return Result.success(list);
    }
}

