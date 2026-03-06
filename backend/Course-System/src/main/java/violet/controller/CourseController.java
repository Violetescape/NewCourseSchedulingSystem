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
import violet.pojo.Course;
import violet.pojo.PageResult;
import violet.pojo.Result;
import violet.service.CourseService;

/**
 * 课程相关接口（RESTful）。
 * <p>
 * - GET    /courses        分页查询（支持按名称模糊、按类型过滤）
 * - POST   /courses        新增课程（ID 手动传入）
 * - PUT    /courses        修改课程
 * - DELETE /courses/{id}   根据 ID 删除课程
 * </p>
 */
@RestController
public class CourseController {

    @Autowired
    private CourseService courseService;

    /**
     * 分页查询课程。
     */
    @GetMapping("/courses")
    public Result page(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) String courseName,
                       @RequestParam(required = false) Integer courseType) {
        System.out.println("分页查询课程信息 pageNum=" + pageNum
                + ", pageSize=" + pageSize
                + ", courseName=" + courseName
                + ", courseType=" + courseType);

        PageResult<Course> pageResult = courseService.page(pageNum, pageSize, courseName, courseType);
        return Result.success(pageResult);
    }

    /**
     * 新增课程（ID 手动传入）。
     */
    @PostMapping("/courses")
    public Result add(@RequestBody Course course) {
        System.out.println("新增课程：" + course);
        courseService.add(course);
        return Result.success();
    }

    /**
     * 修改课程信息。
     */
    @PutMapping("/courses")
    public Result update(@RequestBody Course course) {
        System.out.println("修改课程：" + course);
        courseService.update(course);
        return Result.success();
    }

    /**
     * 根据 ID 删除课程。
     */
    @DeleteMapping("/courses/{id}")
    public Result delete(@PathVariable("id") Integer id) {
        System.out.println("删除课程 id=" + id);
        courseService.deleteById(id);
        return Result.success();
    }
}

