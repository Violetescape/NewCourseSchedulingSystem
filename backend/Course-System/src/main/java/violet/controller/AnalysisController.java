package violet.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import violet.pojo.Result;
import violet.service.AnalysisService;

/**
 * 排课效能分析控制器。
 */
@RestController
public class AnalysisController {

    @Autowired
    private AnalysisService analysisService;

    /**
     * 教室双指标效能评估（散点图数据）。
     */
    @GetMapping("/analysis/classroom-efficiency")
    public Result classroomEfficiency() {
        return Result.success(analysisService.getClassroomEfficiency());
    }

    /**
     * 班级课表健康度（时序折线图数据）。
     */
    @GetMapping("/analysis/class-health")
    public Result classHealth() {
        return Result.success(analysisService.getClassHealth());
    }

    /**
     * 全校排课热力图数据。
     */
    @GetMapping("/analysis/heatmap")
    public Result heatmap() {
        return Result.success(analysisService.getHeatmapNodes());
    }

    /**
     * 教室类型资源占比（饼图 / 环形图）。
     */
    @GetMapping("/analysis/classroom-type-distribution")
    public Result classroomTypeDistribution() {
        return Result.success(analysisService.getClassroomTypeDistribution());
    }

    /**
     * 周一至周五课时负荷趋势（折线图）。
     */
    @GetMapping("/analysis/weekday-lesson-trend")
    public Result weekdayLessonTrend() {
        return Result.success(analysisService.getWeekdayLessonTrend());
    }

    /**
     * 课程类型学时占比（扇形图）。
     */
    @GetMapping("/analysis/course-type-hours")
    public Result courseTypeHours() {
        return Result.success(analysisService.getCourseTypeHourDistribution());
    }

    /**
     * 教室容量区间分布（柱状图）。
     */
    @GetMapping("/analysis/classroom-capacity-distribution")
    public Result classroomCapacityDistribution() {
        return Result.success(analysisService.getClassroomCapacityDistribution());
    }
}

