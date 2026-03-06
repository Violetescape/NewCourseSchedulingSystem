package violet.service;

import violet.pojo.Course;
import violet.pojo.PageResult;

/**
 * 课程业务接口。
 */
public interface CourseService {

    /**
     * 分页查询（支持按名称模糊搜索、按类型过滤）。
     *
     * @param pageNum    页码（从 1 开始）
     * @param pageSize   每页大小
     * @param courseName 课程名称（模糊）
     * @param courseType 课程类型（精确）
     * @return 分页结果
     */
    PageResult<Course> page(Integer pageNum, Integer pageSize, String courseName, Integer courseType);

    /**
     * 新增课程（ID 手动传入）。
     */
    void add(Course course);

    /**
     * 修改课程信息。
     */
    void update(Course course);

    /**
     * 根据 ID 删除课程。
     */
    void deleteById(Integer courseId);
}

