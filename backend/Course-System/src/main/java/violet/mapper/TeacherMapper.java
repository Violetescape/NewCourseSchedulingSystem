package violet.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import violet.pojo.Teacher;

import java.util.List;

/**
 * 教师 Mapper。
 * <p>
 * 基础增删改使用注解；分页条件查询使用 XML 动态 SQL。
 * </p>
 */
@Mapper
public interface TeacherMapper {

    /**
     * 条件分页查询教师列表（姓名模糊、院系精确）。
     *
     * @param offset            偏移量（从 0 开始）
     * @param pageSize          每页大小
     * @param teacherName       教师姓名（模糊）
     * @param teacherDepartment 所属院系/部门（精确）
     * @return 当前页数据
     */
    List<Teacher> findByCondition(@Param("offset") Integer offset,
                                  @Param("pageSize") Integer pageSize,
                                  @Param("teacherName") String teacherName,
                                  @Param("teacherDepartment") String teacherDepartment);

    /**
     * 条件查询总记录数。
     *
     * @param teacherName       教师姓名（模糊）
     * @param teacherDepartment 所属院系/部门（精确）
     * @return 总记录数
     */
    Long countByCondition(@Param("teacherName") String teacherName,
                          @Param("teacherDepartment") String teacherDepartment);

    /**
     * 新增教师（手动传入 teacherId）。
     */
    @Insert("insert into teacher(Teacher_ID, Teacher_Name, Teacher_Department) " +
            "values(#{teacherId}, #{teacherName}, #{teacherDepartment})")
    void add(Teacher teacher);

    /**
     * 修改教师信息。
     */
    @Update("update teacher set Teacher_Name = #{teacherName}, Teacher_Department = #{teacherDepartment} " +
            "where Teacher_ID = #{teacherId}")
    void update(Teacher teacher);

    /**
     * 根据 ID 删除教师。
     */
    @Delete("delete from teacher where Teacher_ID = #{teacherId}")
    void deleteById(Integer teacherId);

    @Select("select Teacher_ID from teacher where Teacher_Name = #{teacherName} limit 1")
    Integer findIdByName(String teacherName);
}

