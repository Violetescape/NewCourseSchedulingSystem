package violet.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import violet.mapper.ClassroomMapper;
import violet.pojo.Classroom;
import violet.pojo.PageResult;
import violet.service.ClassroomService;

import java.util.List;

/**
 * 教室业务实现。
 */
@Service
public class ClassroomServiceImpl implements ClassroomService {

    @Autowired
    private ClassroomMapper classroomMapper;

    @Override
    public PageResult<Classroom> page(Integer pageNum, Integer pageSize, String classroomName, String classroomType) {
        // 1. 计算分页起始位置（MySQL 的 limit 从 0 开始）
        int offset = (pageNum - 1) * pageSize;

        // 2. 查询当前页数据
        List<Classroom> rows = classroomMapper.findByCondition(offset, pageSize, classroomName, classroomType);

        // 3. 查询总记录数
        Long total = classroomMapper.countByCondition(classroomName, classroomType);

        // 4. 封装为分页结果返回
        return new PageResult<>(total, rows);
    }

    @Override
    public void add(Classroom classroom) {
        classroomMapper.add(classroom);
    }

    @Override
    public void update(Classroom classroom) {
        classroomMapper.update(classroom);
    }

    @Override
    public void deleteById(Integer classroomId) {
        classroomMapper.deleteById(classroomId);
    }
}

