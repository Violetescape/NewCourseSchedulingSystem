package violet.service;

import violet.pojo.PageResult;
import violet.pojo.TeacherUnavailable;
import violet.pojo.TeacherUnavailableVO;

/**
 * 教师不可排课时间业务接口。
 */
public interface TeacherUnavailableService {

    /**
     * 分页查询教师不可排课时间列表。
     *
     * @param pageNum   页码（从 1 开始）
     * @param pageSize  每页大小
     * @param teacherId 教师编号（精确）
     * @param unWeekday 周几（精确）
     * @return 分页结果（TeacherUnavailableVO）
     */
    PageResult<TeacherUnavailableVO> page(Integer pageNum,
                                          Integer pageSize,
                                          Integer teacherId,
                                          Integer unWeekday);

    /**
     * 新增教师不可排课记录。
     */
    void add(TeacherUnavailable teacherUnavailable);

    /**
     * 修改教师不可排课记录。
     */
    void update(TeacherUnavailable teacherUnavailable);

    /**
     * 根据 ID 删除教师不可排课记录。
     */
    void deleteById(Integer unId);
}

