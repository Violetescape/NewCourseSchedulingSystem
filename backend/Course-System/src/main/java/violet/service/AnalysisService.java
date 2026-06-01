package violet.service;

import violet.pojo.AnalysisSliceDTO;
import violet.pojo.ClassHealthDTO;
import violet.pojo.ClassroomEfficiencyDTO;
import violet.pojo.HeatmapNodeDTO;

import java.util.List;
import java.util.Map;

/**
 * 排课效能分析服务。
 */
public interface AnalysisService {

    /**
     * 教室双指标效能评估（散点图）。
     */
    List<ClassroomEfficiencyDTO> getClassroomEfficiency();

    /**
     * 班级课表健康度（1-20周时序折线图）。
     */
    List<ClassHealthDTO> getClassHealth();

    /**
     * 全校排课热力图（weekday/section占用频次）。
     */
    Map<Integer, List<HeatmapNodeDTO>> getHeatmapNodes();

    /**
     * 教室类型占比（按教室资源类型计数）。
     */
    List<AnalysisSliceDTO> getClassroomTypeDistribution();

    /**
     * 周一至周五全校「课时量」合计（每条排课记录的连堂节数之和，用于拥挤度趋势）。
     */
    List<AnalysisSliceDTO> getWeekdayLessonTrend();

    /**
     * 按课程类型统计学时占比（累加排课记录上的单次课时）。
     */
    List<AnalysisSliceDTO> getCourseTypeHourDistribution();

    /**
     * 教室容量区间数量分布。
     */
    List<AnalysisSliceDTO> getClassroomCapacityDistribution();
}

