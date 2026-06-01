package violet.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import violet.pojo.TeachingTask;
import violet.pojo.TeachingTaskVO;

import java.util.List;

/**
 * 教学任务 Mapper。
 * <p>
 * - 分页条件查询（带多表关联）使用 XML 动态 SQL；
 * - 新增、修改、删除使用注解形式，仅操作 teaching_task 主表。
 * </p>
 */
@Mapper
public interface TeachingTaskMapper {

    /**
     * 条件分页查询教学任务列表（关联教师、课程、班级信息）。
     *
     * @param offset    偏移量（从 0 开始）
     * @param pageSize  每页大小
     * @param teacherId 授课教师 ID（精确）
     * @param courseId  授课课程 ID（精确）
     * @param classId   上课班级 ID（精确）
     * @param taskState 任务状态：未排课 / 已排课
     * @return 当前页数据（TeachingTaskVO）
     */
    List<TeachingTaskVO> findByCondition(@Param("offset") Integer offset,
                                         @Param("pageSize") Integer pageSize,
                                         @Param("teacherId") Integer teacherId,
                                         @Param("courseId") Integer courseId,
                                         @Param("classId") Integer classId,
                                         @Param("taskState") String taskState);

    /**
     * 按任务主键查询单条 VO（关联教师、课程、班级）。
     */
    TeachingTaskVO findVoByTaskId(@Param("taskId") Integer taskId);

    /**
     * 条件查询总记录数。
     */
    Long countByCondition(@Param("teacherId") Integer teacherId,
                          @Param("courseId") Integer courseId,
                          @Param("classId") Integer classId,
                          @Param("taskState") String taskState);

    /**
     * 新增教学任务（Task_ID 自增，故不插入）。
     */
    @Insert("insert into teaching_task(Teacher_ID, Course_ID, Class_Ids, Task_State) " +
            "values(#{teacherId}, #{courseId}, #{classIds}, #{taskState})")
    void add(TeachingTask teachingTask);

    void insertBatch(@Param("list") List<TeachingTask> tasks);

    /**
     * 修改教学任务。
     */
    @Update("update teaching_task " +
            "set Teacher_ID = #{teacherId}, " +
            "    Course_ID = #{courseId}, " +
            "    Class_Ids = #{classIds}, " +
            "    Task_State = #{taskState} " +
            "where Task_ID = #{taskId}")
    void update(TeachingTask teachingTask);

    /**
     * 根据 ID 删除教学任务。
     */
    @Delete("delete from teaching_task where Task_ID = #{taskId}")
    void deleteById(Integer taskId);

    /**
     * 将全部教学任务状态重置为「未排课」。
     */
    @Update("update teaching_task set Task_State = '未排课'")
    void resetAllTaskStates();
}

