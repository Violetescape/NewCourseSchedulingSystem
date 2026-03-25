package violet.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import violet.pojo.AutoSchedulingResult;
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
}
