package violet.service;

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
}

