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
import violet.pojo.Teacher;
import violet.service.TeacherService;

/**
 * 教师相关接口（RESTful）。
 * <p>
 * - GET    /teachers        分页查询（支持按姓名模糊、按院系过滤）
 * - POST   /teachers        新增教师（ID 手动传入）
 * - PUT    /teachers        修改教师
 * - DELETE /teachers/{id}   根据 ID 删除教师
 * </p>
 */
@RestController
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    @GetMapping("/teachers")
    public Result page(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) String teacherName,
                       @RequestParam(required = false) String teacherDepartment) {
        System.out.println("分页查询教师信息 pageNum=" + pageNum
                + ", pageSize=" + pageSize
                + ", teacherName=" + teacherName
                + ", teacherDepartment=" + teacherDepartment);

        PageResult<Teacher> pageResult = teacherService.page(pageNum, pageSize, teacherName, teacherDepartment);
        return Result.success(pageResult);
    }

    @PostMapping("/teachers")
    public Result add(@RequestBody Teacher teacher) {
        System.out.println("新增教师：" + teacher);
        teacherService.add(teacher);
        return Result.success();
    }

    @PutMapping("/teachers")
    public Result update(@RequestBody Teacher teacher) {
        System.out.println("修改教师：" + teacher);
        teacherService.update(teacher);
        return Result.success();
    }

    @DeleteMapping("/teachers/{id}")
    public Result delete(@PathVariable("id") Integer id) {
        System.out.println("删除教师 id=" + id);
        teacherService.deleteById(id);
        return Result.success();
    }
}

