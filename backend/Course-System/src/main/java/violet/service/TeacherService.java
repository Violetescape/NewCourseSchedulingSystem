package violet.service;

import violet.pojo.PageResult;
import violet.pojo.Teacher;

/**
 * 教师业务接口。
 */
public interface TeacherService {

    /**
     * 分页查询（支持按姓名模糊搜索、按院系精确过滤）。
     *
     * @param pageNum            页码（从 1 开始）
     * @param pageSize           每页大小
     * @param teacherName        教师姓名（模糊）
     * @param teacherDepartment  所属院系/部门（精确）
     * @return 分页结果
     */
    PageResult<Teacher> page(Integer pageNum, Integer pageSize, String teacherName, String teacherDepartment);

    /**
     * 新增教师（ID 手动传入）。
     */
    void add(Teacher teacher);

    /**
     * 修改教师信息。
     */
    void update(Teacher teacher);

    /**
     * 根据 ID 删除教师。
     */
    void deleteById(Integer teacherId);
}

