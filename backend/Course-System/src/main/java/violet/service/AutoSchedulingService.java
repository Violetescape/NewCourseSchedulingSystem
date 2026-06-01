package violet.service;

import violet.pojo.AutoSchedulingResult;
import violet.pojo.ManualScheduleDTO;
import violet.pojo.ManualScheduleValidateResult;

/**
 * 自动排课服务接口。
 * <p>
 * 基于贪心算法 + 启发式排序 + 纯内存冲突校验实现一键自动排课。
 * </p>
 */
public interface AutoSchedulingService {

    /**
     * 执行一键自动排课。
     * <p>
     * 将 teaching_task 表中 Task_State = '未排课' 的任务，按启发式策略排序后，
     * 遍历时间与教室空间，利用三大冲突检测模型快速校验，批量落库并更新内存。
     * </p>
     *
     * @return 排课结果摘要（成功数、失败数等，可按需扩展）
     */
    AutoSchedulingResult executeAutoScheduling();

    /**
     * 手动排课前校验：复用与自动排课相同的冲突检测矩阵（教师 / 教室 / 班级）。
     */
    ManualScheduleValidateResult validateManualSchedule(ManualScheduleDTO dto);

    /**
     * 手动排课落库：校验通过后写入 schedule，并将任务状态更新为「已排课」。
     *
     * @throws IllegalArgumentException 参数或业务规则不通过时抛出（消息为中文提示）
     */
    void manualSchedule(ManualScheduleDTO dto);

    /**
     * 清空全校排课记录，并将所有教学任务状态重置为「未排课」。
     * <p>
     * 先删除 schedule 全表数据，再批量更新 teaching_task，两步在同一事务中执行。
     * </p>
     */
    void clearAndResetSchedules();
}
