package violet.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import violet.pojo.AutoSchedulingResult;
import violet.pojo.ManualScheduleDTO;
import violet.pojo.ManualScheduleValidateResult;
import violet.pojo.Result;
import violet.service.AutoSchedulingService;

/**
 * 自动排课控制器。
 */
@RestController
public class AutoSchedulingController {

    @Autowired
    private AutoSchedulingService autoSchedulingService;

    /**
     * 一键自动排课。
     */
    @PostMapping("/auto-schedule")
    public Result executeAutoScheduling() {
        AutoSchedulingResult result = autoSchedulingService.executeAutoScheduling();
        return Result.success(result);
    }

    /**
     * 手动排课：实时冲突校验（教师 / 教室 / 班级矩阵）。
     */
    @PostMapping("/auto-schedule/manual/validate")
    public Result validateManualSchedule(@RequestBody ManualScheduleDTO dto) {
        ManualScheduleValidateResult r = autoSchedulingService.validateManualSchedule(dto);
        return Result.success(r);
    }

    /**
     * 手动排课落库。
     */
    @PostMapping("/auto-schedule/manual")
    public Result manualSchedule(@RequestBody ManualScheduleDTO dto) {
        try {
            autoSchedulingService.manualSchedule(dto);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 一键重置排课：清空全校课表并将任务恢复为未排课。
     */
    @PostMapping("/auto-scheduling/reset")
    public Result clearAndResetSchedules() {
        autoSchedulingService.clearAndResetSchedules();
        return Result.success();
    }
}
