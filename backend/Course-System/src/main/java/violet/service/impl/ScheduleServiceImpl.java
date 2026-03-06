package violet.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import violet.mapper.ScheduleMapper;
import violet.pojo.ScheduleVO;
import violet.service.ScheduleService;

import java.util.List;

/**
 * 课表查询业务实现（按周）。
 */
@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Override
    public List<ScheduleVO> listByClass(Integer classId, Integer week) {
        return scheduleMapper.findScheduleList(week, classId, null);
    }

    @Override
    public List<ScheduleVO> listByTeacher(Integer teacherId, Integer week) {
        return scheduleMapper.findScheduleList(week, null, teacherId);
    }
}

