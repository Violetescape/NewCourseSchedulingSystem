package violet.service;

import violet.pojo.Clazz;
import violet.pojo.PageResult;

import java.util.List;

public interface ClazzService {

    // 查询全部的班级信息
    List<Clazz> findAll();

    // 根据Id删除班级
    void deleteById(Integer classId);

    // 添加班级
    void add(Clazz clazzEntity);

    // 条件分页查询：可以按年级、专业、学院筛选
    PageResult<Clazz> page(Integer pageNum, Integer pageSize,
                           Integer classGrade,
                           String classMajor,
                           String classDepartment);

    // 修改班级（预留）
}
