package violet.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import violet.mapper.CourseMapper;
import violet.pojo.Course;
import violet.pojo.PageResult;
import violet.service.CourseService;

import java.util.List;

/**
 * 课程业务实现。
 */
@Service
public class CourseServiceImpl implements CourseService {

    @Autowired
    private CourseMapper courseMapper;

    @Override
    public PageResult<Course> page(Integer pageNum, Integer pageSize, String courseName, Integer courseType) {
        // 1. 计算分页起始位置（MySQL 的 limit 从 0 开始）
        int offset = (pageNum - 1) * pageSize;

        // 2. 查询当前页数据
        List<Course> rows = courseMapper.findByCondition(offset, pageSize, courseName, courseType);

        // 3. 查询总记录数
        Long total = courseMapper.countByCondition(courseName, courseType);

        // 4. 封装为分页结果返回
        return new PageResult<>(total, rows);
    }

    @Override
    public void add(Course course) {
        courseMapper.add(course);
    }

    @Override
    public void update(Course course) {
        courseMapper.update(course);
    }

    @Override
    public void deleteById(Integer courseId) {
        courseMapper.deleteById(courseId);
    }
}

