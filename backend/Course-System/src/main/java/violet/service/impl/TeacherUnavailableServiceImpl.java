package violet.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import violet.mapper.TeacherUnavailableMapper;
import violet.pojo.PageResult;
import violet.pojo.TeacherUnavailable;
import violet.pojo.TeacherUnavailableVO;
import violet.service.TeacherUnavailableService;

import java.util.List;

/**
 * 教师不可排课时间业务实现。
 */
@Service
public class TeacherUnavailableServiceImpl implements TeacherUnavailableService {

    @Autowired
    private TeacherUnavailableMapper teacherUnavailableMapper;

    @Override
    public PageResult<TeacherUnavailableVO> page(Integer pageNum,
                                                 Integer pageSize,
                                                 Integer teacherId,
                                                 Integer unWeekday) {
        int offset = (pageNum - 1) * pageSize;

        List<TeacherUnavailableVO> rows =
                teacherUnavailableMapper.findByCondition(offset, pageSize, teacherId, unWeekday);

        Long total = teacherUnavailableMapper.countByCondition(teacherId, unWeekday);

        return new PageResult<>(total, rows);
    }

    @Override
    public void add(TeacherUnavailable teacherUnavailable) {
        teacherUnavailableMapper.add(teacherUnavailable);
    }

    @Override
    public void update(TeacherUnavailable teacherUnavailable) {
        teacherUnavailableMapper.update(teacherUnavailable);
    }

    @Override
    public void deleteById(Integer unId) {
        teacherUnavailableMapper.deleteById(unId);
    }
}

