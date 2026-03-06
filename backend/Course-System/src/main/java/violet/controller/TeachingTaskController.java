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
import violet.pojo.TeachingTask;
import violet.pojo.TeachingTaskVO;
import violet.service.TeachingTaskService;

/**
 * 教学任务相关接口（RESTful）。
 * <p>
 * - GET    /teaching-tasks        分页查询（支持按教师、课程、班级、状态过滤，返回 VO）
 * - POST   /teaching-tasks        新增教学任务
 * - PUT    /teaching-tasks        修改教学任务
 * - DELETE /teaching-tasks/{id}   根据 ID 删除教学任务
 * </p>
 */
@RestController
public class TeachingTaskController {

    @Autowired
    private TeachingTaskService teachingTaskService;

    /**
     * 分页查询教学任务（返回 TeachingTaskVO）。
     */
    @GetMapping("/teaching-tasks")
    public Result page(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) Integer teacherId,
                       @RequestParam(required = false) Integer courseId,
                       @RequestParam(required = false) Integer classId,
                       @RequestParam(required = false) String taskState) {
        System.out.println("分页查询教学任务 pageNum=" + pageNum
                + ", pageSize=" + pageSize
                + ", teacherId=" + teacherId
                + ", courseId=" + courseId
                + ", classId=" + classId
                + ", taskState=" + taskState);

        PageResult<TeachingTaskVO> pageResult =
                teachingTaskService.page(pageNum, pageSize, teacherId, courseId, classId, taskState);
        return Result.success(pageResult);
    }

    /**
     * 新增教学任务。
     */
    @PostMapping("/teaching-tasks")
    public Result add(@RequestBody TeachingTask teachingTask) {
        System.out.println("新增教学任务：" + teachingTask);
        teachingTaskService.add(teachingTask);
        return Result.success();
    }

    /**
     * 修改教学任务。
     */
    @PutMapping("/teaching-tasks")
    public Result update(@RequestBody TeachingTask teachingTask) {
        System.out.println("修改教学任务：" + teachingTask);
        teachingTaskService.update(teachingTask);
        return Result.success();
    }

    /**
     * 根据 ID 删除教学任务。
     */
    @DeleteMapping("/teaching-tasks/{id}")
    public Result delete(@PathVariable("id") Integer id) {
        System.out.println("删除教学任务 id=" + id);
        teachingTaskService.deleteById(id);
        return Result.success();
    }
}

