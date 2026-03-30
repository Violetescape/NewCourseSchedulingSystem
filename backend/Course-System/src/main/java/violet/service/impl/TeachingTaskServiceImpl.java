package violet.service.impl;

import com.alibaba.excel.EasyExcel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import violet.mapper.ClazzMapper;
import violet.mapper.CourseMapper;
import violet.mapper.TeacherMapper;
import violet.mapper.TeachingTaskMapper;
import violet.pojo.PageResult;
import violet.pojo.TeachingTask;
import violet.pojo.TeachingTaskExcelDTO;
import violet.pojo.TeachingTaskVO;
import violet.service.TeachingTaskService;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 教学任务业务实现。
 */
@Service
public class TeachingTaskServiceImpl implements TeachingTaskService {

    @Autowired
    private TeachingTaskMapper teachingTaskMapper;
    @Autowired
    private TeacherMapper teacherMapper;
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private ClazzMapper clazzMapper;

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

    @Override
    public void importFromExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }

        List<TeachingTaskExcelDTO> rows;
        try (InputStream inputStream = file.getInputStream()) {
            rows = EasyExcel.read(inputStream)
                    .head(TeachingTaskExcelDTO.class)
                    .sheet()
                    .doReadSync();
        } catch (IOException e) {
            throw new RuntimeException("读取 Excel 失败");
        }

        if (rows == null || rows.isEmpty()) {
            return;
        }

        List<TeachingTask> tasks = new ArrayList<>();
        for (TeachingTaskExcelDTO row : rows) {
            if (row == null) {
                continue;
            }

            String teacherName = trim(row.getTeacherName());
            String courseName = trim(row.getCourseName());
            String className = trim(row.getClassName());
            if (teacherName == null && courseName == null && className == null) {
                continue;
            }

            Integer teacherId = teacherMapper.findIdByName(teacherName);
            if (teacherId == null) {
                throw new RuntimeException("教师数据不存在: " + teacherName);
            }
            Integer courseId = courseMapper.findIdByName(courseName);
            if (courseId == null) {
                throw new RuntimeException("课程数据不存在: " + courseName);
            }
            Integer classId = clazzMapper.findIdByName(className);
            if (classId == null) {
                throw new RuntimeException("班级数据不存在: " + className);
            }

            TeachingTask teachingTask = new TeachingTask();
            teachingTask.setTeacherId(teacherId);
            teachingTask.setCourseId(courseId);
            teachingTask.setClassId(classId);
            teachingTask.setTaskState("未排课");
            tasks.add(teachingTask);
        }

        if (!tasks.isEmpty()) {
            teachingTaskMapper.insertBatch(tasks);
        }
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String v = value.trim();
        return v.isEmpty() ? null : v;
    }
}

