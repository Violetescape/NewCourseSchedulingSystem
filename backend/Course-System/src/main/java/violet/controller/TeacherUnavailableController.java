package violet.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import violet.pojo.PageResult;
import violet.pojo.Result;
import violet.pojo.TeacherUnavailable;
import violet.pojo.TeacherUnavailableVO;
import violet.service.TeacherUnavailableService;

/**
 * 教师不可排课时间相关接口（RESTful）。
 * <p>
 * - GET    /teacher-unavailable        分页查询（支持按教师、周几过滤，返回 VO）
 * - POST   /teacher-unavailable        新增记录
 * - PUT    /teacher-unavailable        修改记录
 * - DELETE /teacher-unavailable/{id}   根据 ID 删除记录
 * </p>
 */
@RestController
public class TeacherUnavailableController {

    @Autowired
    private TeacherUnavailableService teacherUnavailableService;

    /**
     * 分页查询教师不可排课记录。
     */
    @GetMapping("/teacher-unavailable")
    public Result page(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) Integer teacherId,
                       @RequestParam(required = false) Integer unWeekday) {
        System.out.println("分页查询教师不可排课记录 pageNum=" + pageNum
                + ", pageSize=" + pageSize
                + ", teacherId=" + teacherId
                + ", unWeekday=" + unWeekday);

        PageResult<TeacherUnavailableVO> pageResult =
                teacherUnavailableService.page(pageNum, pageSize, teacherId, unWeekday);
        return Result.success(pageResult);
    }

    /**
     * 新增教师不可排课记录。
     */
    @PostMapping("/teacher-unavailable")
    public Result add(@RequestBody TeacherUnavailable teacherUnavailable) {
        System.out.println("新增教师不可排课记录：" + teacherUnavailable);
        teacherUnavailableService.add(teacherUnavailable);
        return Result.success();
    }

    /**
     * 修改教师不可排课记录。
     */
    @PutMapping("/teacher-unavailable")
    public Result update(@RequestBody TeacherUnavailable teacherUnavailable) {
        System.out.println("修改教师不可排课记录：" + teacherUnavailable);
        teacherUnavailableService.update(teacherUnavailable);
        return Result.success();
    }

    /**
     * 根据 ID 删除教师不可排课记录。
     */
    @DeleteMapping("/teacher-unavailable/{id}")
    public Result delete(@PathVariable("id") Integer id) {
        System.out.println("删除教师不可排课记录 id=" + id);
        teacherUnavailableService.deleteById(id);
        return Result.success();
    }
}

