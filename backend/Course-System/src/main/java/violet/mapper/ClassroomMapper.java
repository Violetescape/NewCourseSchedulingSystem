package violet.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import violet.pojo.Classroom;

import java.util.List;

/**
 * 教室 Mapper。
 * <p>
 * 复杂/动态查询使用 XML，基础增删改使用注解，保持与项目现有风格一致。
 * </p>
 */
@Mapper
public interface ClassroomMapper {

    /**
     * 条件分页查询数据列表（名称模糊、类型过滤）。
     *
     * @param offset        偏移量（从 0 开始）
     * @param pageSize      每页大小
     * @param classroomName 教室名称（模糊）
     * @param classroomType 教室类型（精确）
     * @return 当前页数据
     */
    List<Classroom> findByCondition(@Param("offset") Integer offset,
                                    @Param("pageSize") Integer pageSize,
                                    @Param("classroomName") String classroomName,
                                    @Param("classroomType") String classroomType);

    /**
     * 条件查询总记录数。
     *
     * @param classroomName 教室名称（模糊）
     * @param classroomType 教室类型（精确）
     * @return 总记录数
     */
    Long countByCondition(@Param("classroomName") String classroomName,
                          @Param("classroomType") String classroomType);

    /**
     * 新增教室（手动传入 classroomId）。
     *
     * @param classroom 教室信息
     */
    @Insert("insert into classroom(Classroom_ID, Classroom_Type, Classroom_Cap, Classroom_State, Classroom_Name) " +
            "values(#{classroomId}, #{classroomType}, #{classroomCap}, #{classroomState}, #{classroomName})")
    void add(Classroom classroom);

    /**
     * 修改教室信息（包含状态更新）。
     *
     * @param classroom 教室信息
     */
    @Update("update classroom set Classroom_Type = #{classroomType}, Classroom_Cap = #{classroomCap}, " +
            "Classroom_State = #{classroomState}, Classroom_Name = #{classroomName} " +
            "where Classroom_ID = #{classroomId}")
    void update(Classroom classroom);

    /**
     * 根据 ID 删除教室。
     *
     * @param classroomId 教室编号
     */
    @Delete("delete from classroom where Classroom_ID = #{classroomId}")
    void deleteById(Integer classroomId);
}

