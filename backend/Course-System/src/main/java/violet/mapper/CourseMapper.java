package violet.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import violet.pojo.Course;

import java.util.List;

/**
 * 课程 Mapper。
 * <p>
 * 基础增删改使用注解；分页条件查询使用 XML 动态 SQL。
 * </p>
 */
@Mapper
public interface CourseMapper {

    /**
     * 条件分页查询课程列表（名称模糊、类型过滤）。
     *
     * @param offset     偏移量（从 0 开始）
     * @param pageSize   每页大小
     * @param courseName 课程名称（模糊）
     * @param courseType 课程类型：1-必修, 2-专业选修, 3-公选（精确）
     * @return 当前页数据
     */
    List<Course> findByCondition(@Param("offset") Integer offset,
                                 @Param("pageSize") Integer pageSize,
                                 @Param("courseName") String courseName,
                                 @Param("courseType") Integer courseType);

    /**
     * 条件查询总记录数。
     *
     * @param courseName 课程名称（模糊）
     * @param courseType 课程类型（精确）
     * @return 总记录数
     */
    Long countByCondition(@Param("courseName") String courseName,
                          @Param("courseType") Integer courseType);

    /**
     * 新增课程（手动传入 courseId）。
     */
    @Insert("insert into course(Course_ID, Course_Name, Course_Type, Course_StartWeek, Course_EndWeek, Course_SingleHour) " +
            "values(#{courseId}, #{courseName}, #{courseType}, #{courseStartWeek}, #{courseEndWeek}, #{courseSingleHour})")
    void add(Course course);

    /**
     * 修改课程信息。
     */
    @Update("update course set Course_Name = #{courseName}, Course_Type = #{courseType}, " +
            "Course_StartWeek = #{courseStartWeek}, Course_EndWeek = #{courseEndWeek}, " +
            "Course_SingleHour = #{courseSingleHour} " +
            "where Course_ID = #{courseId}")
    void update(Course course);

    /**
     * 根据 ID 删除课程。
     */
    @Delete("delete from course where Course_ID = #{courseId}")
    void deleteById(Integer courseId);

    /**
     * 根据 ID 查询课程。
     */
    @Select("select Course_ID as courseId, Course_Name as courseName, Course_Type as courseType, " +
            "Course_StartWeek as courseStartWeek, Course_EndWeek as courseEndWeek, " +
            "Course_SingleHour as courseSingleHour from course where Course_ID = #{courseId}")
    Course findById(Integer courseId);
}

