package violet.service;

import violet.pojo.AutoSchedulingResult;

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
}
