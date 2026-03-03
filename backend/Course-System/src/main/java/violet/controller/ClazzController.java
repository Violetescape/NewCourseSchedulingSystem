package violet.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import violet.pojo.Clazz;
import violet.pojo.PageResult;
import violet.pojo.Result;
import violet.service.ClazzService;

import java.util.List;

@RestController
public class ClazzController {

    @Autowired
    private ClazzService clazzService;


    //用于排课时的下拉列表
    //@RequestMapping("/class/all")
    @GetMapping("/class/all")
    public Result list(){
        System.out.println("查询全部的班级信息");
        List<Clazz> clazzList = clazzService.findAll();
        return Result.success(clazzList);
    }

    //删除指定班级
    @DeleteMapping("/class/{id}")
    public Result delete(@PathVariable("id") Integer classId){
        System.out.println("删除班级"+classId);
        clazzService.deleteById(classId);
        return Result.success();
    }

    //新增指定班级
    @PostMapping("/class")
    public Result add(@RequestBody Clazz clazzEntity){
        System.out.println("新增班级"+clazzEntity);
        clazzService.add(clazzEntity);
        return Result.success();
    }

    // 条件分页查询：支持按年级、专业、学院筛选
    @GetMapping("/class/page")
    public Result page(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) Integer classGrade,
                       @RequestParam(required = false) String classMajor,
                       @RequestParam(required = false) String classDepartment) {
        System.out.println("条件分页查询班级信息 pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                ", classGrade=" + classGrade +
                ", classMajor=" + classMajor +
                ", classDepartment=" + classDepartment);

        PageResult<Clazz> pageResult = clazzService.page(pageNum, pageSize, classGrade, classMajor, classDepartment);
        return Result.success(pageResult);
    }
}
