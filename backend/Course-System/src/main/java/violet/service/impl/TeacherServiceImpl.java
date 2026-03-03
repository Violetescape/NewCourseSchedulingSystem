package violet.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import violet.mapper.TeacherMapper;
import violet.pojo.PageResult;
import violet.pojo.Teacher;
import violet.service.TeacherService;

import java.util.List;

/**
 * 教师业务实现。
 */
@Service
public class TeacherServiceImpl implements TeacherService {

    @Autowired
    private TeacherMapper teacherMapper;

    @Override
    public PageResult<Teacher> page(Integer pageNum, Integer pageSize, String teacherName, String teacherDepartment) {
        int offset = (pageNum - 1) * pageSize;

        List<Teacher> rows = teacherMapper.findByCondition(offset, pageSize, teacherName, teacherDepartment);
        Long total = teacherMapper.countByCondition(teacherName, teacherDepartment);

        return new PageResult<>(total, rows);
    }

    @Override
    public void add(Teacher teacher) {
        teacherMapper.add(teacher);
    }

    @Override
    public void update(Teacher teacher) {
        teacherMapper.update(teacher);
    }

    @Override
    public void deleteById(Integer teacherId) {
        teacherMapper.deleteById(teacherId);
    }
}

