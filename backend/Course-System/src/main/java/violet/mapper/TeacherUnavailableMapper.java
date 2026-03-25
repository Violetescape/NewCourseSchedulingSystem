package violet.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import violet.pojo.TeacherUnavailable;
import violet.pojo.TeacherUnavailableVO;

import java.util.List;

/**
 * 教师不可排课时间 Mapper。
 * <p>
 * - 分页条件查询（带 teacher 信息）使用 XML 动态 SQL；
 * - 新增、修改、删除使用注解形式，仅操作 teacher_unavailable 主表。
 * </p>
 */
@Mapper
public interface TeacherUnavailableMapper {

    /**
     * 条件分页查询教师不可排课时间列表。
     *
     * @param offset    偏移量（从 0 开始）
     * @param pageSize  每页大小
     * @param teacherId 教师编号（精确）
     * @param unWeekday 周几（精确）
     * @return 当前页数据（TeacherUnavailableVO）
     */
    List<TeacherUnavailableVO> findByCondition(@Param("offset") Integer offset,
                                               @Param("pageSize") Integer pageSize,
                                               @Param("teacherId") Integer teacherId,
                                               @Param("unWeekday") Integer unWeekday);

    /**
     * 条件查询总记录数。
     */
    Long countByCondition(@Param("teacherId") Integer teacherId,
                          @Param("unWeekday") Integer unWeekday);

    /**
     * 新增教师不可排课记录（Un_ID 自增）。
     */
    @Insert("insert into teacher_unavailable(Teacher_ID, Un_Week, Un_Weekday, Un_Section, Un_Remark) " +
            "values(#{teacherId}, #{unWeek}, #{unWeekday}, #{unSection}, #{unRemark})")
    void add(TeacherUnavailable teacherUnavailable);

    /**
     * 修改教师不可排课记录。
     */
    @Update("update teacher_unavailable " +
            "set Teacher_ID = #{teacherId}, " +
            "    Un_Week = #{unWeek}, " +
            "    Un_Weekday = #{unWeekday}, " +
            "    Un_Section = #{unSection}, " +
            "    Un_Remark = #{unRemark} " +
            "where Un_ID = #{unId}")
    void update(TeacherUnavailable teacherUnavailable);

    /**
     * 根据 ID 删除教师不可排课记录。
     */
    @Delete("delete from teacher_unavailable where Un_ID = #{unId}")
    void deleteById(Integer unId);

    /**
     * 查询所有教师不可排课记录（用于冲突检测模型初始化）。
     *
     * @return 教师不可排课记录列表
     */
    List<TeacherUnavailable> findAll();
}

