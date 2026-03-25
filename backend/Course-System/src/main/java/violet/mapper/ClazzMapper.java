package violet.mapper;



import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import violet.pojo.Clazz;

import java.util.List;


@Mapper
public interface ClazzMapper {

    // 查询全部的班级信息（别名必须与 Clazz 属性名一致：classId、className 等）
    @Select("select Class_ID as classId, Class_Name as className, Class_Major as classMajor, " +
            "Class_Grade as classGrade, CLass_num as classNum, Class_Department as classDepartment " +
            "from class order by Class_ID")
    List<Clazz> findAll();

    /**
     * 根据 ID 查询班级。
     */
    @Select("select Class_ID as classId, Class_Name as className, Class_Major as classMajor, " +
            "Class_Grade as classGrade, Class_Num as classNum, Class_Department as classDepartment " +
            "from class where Class_ID = #{classId}")
    Clazz findById(Integer classId);


    //  根据Id删除班级
    @Delete("delete from class where Class_ID = #{classId}")
    void deleteById(Integer classid);

    // 手动传入 classId 插入
    @Insert("insert into class(Class_ID, Class_Name, Class_Major, Class_Grade, Class_Num, Class_Department) " +
            "values(#{classId}, #{className}, #{classMajor}, #{classGrade}, #{classNum}, #{classDepartment})")
    void add(Clazz clazzEntity);

    // 条件分页查询数据列表
    List<Clazz> findByCondition(@Param("offset") Integer offset,
                                @Param("pageSize") Integer pageSize,
                                @Param("classGrade") Integer classGrade,
                                @Param("classMajor") String classMajor,
                                @Param("classDepartment") String classDepartment);

    // 条件分页查询总记录数
    Long countByCondition(@Param("classGrade") Integer classGrade,
                          @Param("classMajor") String classMajor,
                          @Param("classDepartment") String classDepartment);
}
