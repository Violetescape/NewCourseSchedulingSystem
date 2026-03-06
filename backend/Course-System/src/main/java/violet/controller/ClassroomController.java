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
import violet.pojo.Classroom;
import violet.pojo.PageResult;
import violet.pojo.Result;
import violet.service.ClassroomService;

/**
 * 教室相关接口。
 * <p>
 * RESTful 风格：
 * - GET    /classrooms        分页查询（支持按名称模糊搜索、按类型过滤）
 * - POST   /classrooms        新增教室
 * - PUT    /classrooms        修改教室信息（包括状态更新）
 * - DELETE /classrooms/{id}   根据 ID 删除教室
 * </p>
 */
@RestController
public class ClassroomController {

    @Autowired
    private ClassroomService classroomService;

    /**
     * 分页查询教室。
     *
     * @param pageNum       页码
     * @param pageSize      每页大小
     * @param classroomName 教室名称（模糊）
     * @param classroomType 教室类型（精确）
     * @return 分页结果
     */
    @GetMapping("/classrooms")
    public Result page(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) String classroomName,
                       @RequestParam(required = false) String classroomType) {
        System.out.println("分页查询教室信息 pageNum=" + pageNum
                + ", pageSize=" + pageSize
                + ", classroomName=" + classroomName
                + ", classroomType=" + classroomType);

        PageResult<Classroom> pageResult = classroomService.page(pageNum, pageSize, classroomName, classroomType);
        return Result.success(pageResult);
    }

    /**
     * 新增教室。
     *
     * @param classroom 教室信息
     * @return 统一结果
     */
    @PostMapping("/classrooms")
    public Result add(@RequestBody Classroom classroom) {
        System.out.println("新增教室：" + classroom);
        classroomService.add(classroom);
        return Result.success();
    }

    /**
     * 修改教室信息（包含状态更新）。
     *
     * @param classroom 教室信息
     * @return 统一结果
     */
    @PutMapping("/classrooms")
    public Result update(@RequestBody Classroom classroom) {
        System.out.println("修改教室：" + classroom);
        classroomService.update(classroom);
        return Result.success();
    }

    /**
     * 根据 ID 删除教室。
     *
     * @param id 教室编号
     * @return 统一结果
     */
    @DeleteMapping("/classrooms/{id}")
    public Result delete(@PathVariable("id") Integer id) {
        System.out.println("删除教室 id=" + id);
        classroomService.deleteById(id);
        return Result.success();
    }
}

