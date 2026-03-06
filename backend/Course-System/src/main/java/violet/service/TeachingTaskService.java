package violet.service;

import violet.pojo.PageResult;
import violet.pojo.TeachingTask;
import violet.pojo.TeachingTaskVO;

/**
 * 教学任务业务接口。
 */
public interface TeachingTaskService {

    /**
     * 分页查询教学任务（关联教师、课程、班级信息）。
     *
     * @param pageNum   页码（从 1 开始）
     * @param pageSize  每页大小
     * @param teacherId 授课教师 ID（精确）
     * @param courseId  授课课程 ID（精确）
     * @param classId   上课班级 ID（精确）
     * @param taskState 任务状态（未排课 / 已排课）
     * @return 分页结果（TeachingTaskVO）
     */
    PageResult<TeachingTaskVO> page(Integer pageNum,
                                    Integer pageSize,
                                    Integer teacherId,
                                    Integer courseId,
                                    Integer classId,
                                    String taskState);

    /**
     * 新增教学任务。
     */
    void add(TeachingTask teachingTask);

    /**
     * 修改教学任务。
     */
    void update(TeachingTask teachingTask);

    /**
     * 根据 ID 删除教学任务。
     */
    void deleteById(Integer taskId);
}

