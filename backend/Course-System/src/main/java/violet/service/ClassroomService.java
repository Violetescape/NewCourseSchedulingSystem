package violet.service;

import violet.pojo.Classroom;
import violet.pojo.PageResult;

/**
 * 教室业务接口。
 */
public interface ClassroomService {

    /**
     * 分页查询（支持按名称模糊搜索，按类型过滤）。
     *
     * @param pageNum       页码（从 1 开始）
     * @param pageSize      每页大小
     * @param classroomName 教室名称（模糊）
     * @param classroomType 教室类型（精确）
     * @return 分页结果
     */
    PageResult<Classroom> page(Integer pageNum, Integer pageSize, String classroomName, String classroomType);

    /**
     * 新增教室。
     *
     * @param classroom 教室信息
     */
    void add(Classroom classroom);

    /**
     * 修改教室信息（包含状态更新）。
     *
     * @param classroom 教室信息
     */
    void update(Classroom classroom);

    /**
     * 根据 ID 删除教室。
     *
     * @param classroomId 教室编号
     */
    void deleteById(Integer classroomId);
}

