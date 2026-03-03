package violet.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import violet.mapper.ClazzMapper;
import violet.pojo.Clazz;
import violet.pojo.PageResult;
import violet.service.ClazzService;

import java.util.List;

@Service
public class ClazzServiceImpl implements ClazzService {

    @Autowired
    private ClazzMapper clazzMapper;


    @Override
    public List<Clazz> findAll() {
        return clazzMapper.findAll();
    }

    @Override
    public void deleteById(Integer classId) {
        clazzMapper.deleteById(classId);
    }

    @Override
    public void add(Clazz clazzEntity) {
        clazzMapper.add(clazzEntity);
    }

    @Override
    public PageResult<Clazz> page(Integer pageNum, Integer pageSize,
                                  Integer classGrade,
                                  String classMajor,
                                  String classDepartment) {
        // 1. 计算分页起始位置（MySQL 的 limit 从 0 开始）
        int offset = (pageNum - 1) * pageSize;

        // 2. 查询当前页数据
        List<Clazz> rows = clazzMapper.findByCondition(offset, pageSize, classGrade, classMajor, classDepartment);

        // 3. 查询总记录数
        Long total = clazzMapper.countByCondition(classGrade, classMajor, classDepartment);

        // 4. 封装为分页结果返回
        return new PageResult<>(total, rows);
    }
}
