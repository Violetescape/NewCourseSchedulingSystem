package violet.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import violet.mapper.TeachingTaskMapper;
import violet.pojo.PageResult;
import violet.pojo.TeachingTask;
import violet.pojo.TeachingTaskVO;
import violet.service.TeachingTaskService;

import java.util.List;

/**
 * 教学任务业务实现。
 */
@Service
public class TeachingTaskServiceImpl implements TeachingTaskService {

    @Autowired
    private TeachingTaskMapper teachingTaskMapper;

    @Override
    public PageResult<TeachingTaskVO> page(Integer pageNum,
                                           Integer pageSize,
                                           Integer teacherId,
                                           Integer courseId,
                                           Integer classId,
                                           String taskState) {
        // 1. 计算分页起始位置（MySQL 的 limit 从 0 开始）
        int offset = (pageNum - 1) * pageSize;

        // 2. 查询当前页数据（带多表关联，返回 VO）
        List<TeachingTaskVO> rows =
                teachingTaskMapper.findByCondition(offset, pageSize, teacherId, courseId, classId, taskState);

        // 3. 查询总记录数
        Long total = teachingTaskMapper.countByCondition(teacherId, courseId, classId, taskState);

        // 4. 封装分页结果
        return new PageResult<>(total, rows);
    }

    @Override
    public void add(TeachingTask teachingTask) {
        teachingTaskMapper.add(teachingTask);
    }

    @Override
    public void update(TeachingTask teachingTask) {
        teachingTaskMapper.update(teachingTask);
    }

    @Override
    public void deleteById(Integer taskId) {
        teachingTaskMapper.deleteById(taskId);
    }
}

