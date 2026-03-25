package violet.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import violet.mapper.*;
import violet.pojo.*;
import violet.service.AutoSchedulingService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 自动排课服务实现类。
 * <p>
 * 基于贪心算法 + 启发式排序 + 纯内存冲突校验实现一键自动排课。
 * 核心架构：三大冲突检测模型（教师冲突、教室冲突、班级冲突）+ 最佳适应排序（Best-Fit）。
 * </p>
 */
@Service
public class AutoSchedulingServiceImpl implements AutoSchedulingService {

    private static final Logger log = LoggerFactory.getLogger(AutoSchedulingServiceImpl.class);

    /** 未排课状态 */
    private static final String TASK_STATE_UNSCHEDULED = "未排课";
    /** 已排课状态 */
    private static final String TASK_STATE_SCHEDULED = "已排课";
    /** 排课失败状态 */
    private static final String TASK_STATE_FAILED = "排课失败";

    /** 周次范围：1-20 */
    private static final int MAX_WEEK = 20;
    /** 星期范围：1-5（周一至周五） */
    private static final int MIN_WEEKDAY = 1;
    private static final int MAX_WEEKDAY = 5;
    /** 节次范围：1-12 */
    private static final int MIN_SECTION = 1;
    private static final int MAX_SECTION = 12;

    @Autowired
    private TeachingTaskMapper teachingTaskMapper;
    @Autowired
    private ScheduleMapper scheduleMapper;
    @Autowired
    private ClassroomMapper classroomMapper;
    @Autowired
    private TeacherUnavailableMapper teacherUnavailableMapper;
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private ClazzMapper clazzMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AutoSchedulingResult executeAutoScheduling() {
        // ==================== 一、数据预热 ====================
        List<TeachingTaskVO> unscheduledTasks = loadAndSortTasks();
        List<Classroom> classroomsAsc = loadAndSortClassrooms();

        Set<String> teacherSet = new HashSet<>();
        Map<Integer, Set<String>> classroomMap = new HashMap<>();
        Map<Integer, boolean[][][]> classMatrixMap = new HashMap<>();

        buildConflictModels(teacherSet, classroomMap, classMatrixMap);

        int successCount = 0;
        int failCount = 0;

        // ==================== 二、贪心排课循环 ====================
        for (TeachingTaskVO task : unscheduledTasks) {
            if (scheduleOneTask(task, classroomsAsc, teacherSet, classroomMap, classMatrixMap)) {
                successCount++;
            } else {
                failCount++;
                markTaskAsFailed(task);
                log.warn("自动排课失败：任务 taskId={}, 教师={}, 课程={}, 班级={}",
                        task.getTaskId(), task.getTeacherName(), task.getCourseName(), task.getClassName());
            }
        }

        return AutoSchedulingResult.builder()
                .successCount(successCount)
                .failCount(failCount)
                .totalCount(unscheduledTasks.size())
                .build();
    }

    /**
     * 双向启发式排序（极速匹配策略）：查询未任务，按上课人数降序排列（大课优先）。
     */
    private List<TeachingTaskVO> loadAndSortTasks() {
        List<TeachingTaskVO> list = teachingTaskMapper.findByCondition(
                0, Integer.MAX_VALUE, null, null, null, TASK_STATE_UNSCHEDULED);
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();                             
        }
        return list.stream()
                .sorted((a, b) -> {
                    int numA = getStudentNum(a.getClassId());
                    int numB = getStudentNum(b.getClassId());
                    return Integer.compare(numB, numA);
                })
                .collect(Collectors.toList());

    }

    /**
     * 最佳适应排序（Best-Fit）：查询所有教室，按容量升序排列（小课配小屋，大课配大屋）。
     */
    private List<Classroom> loadAndSortClassrooms() {
        List<Classroom> list = classroomMapper.findAllOrderByCapacityAsc();
        return list != null ? list : Collections.emptyList();
    }
    /**
     * 构建三大冲突检测内存模型
     *
     * @param teacherSet    教师冲突集合：Key = "TeacherID_Week_Weekday_Section"
     * @param classroomMap  教室冲突映射：Key = 教室ID, Value = 已占用时间槽集合
     * @param classMatrixMap 班级冲突矩阵：Key = 班级ID, Value = [周][星期][节次]
     */
    private void buildConflictModels(Set<String> teacherSet,
                                    Map<Integer, Set<String>> classroomMap,
                                    Map<Integer, boolean[][][]> classMatrixMap) {
        // ----- 1. 教师冲突（定点排雷）：HashSet O(1) 快速判断教师某时间段是否已占用 -----
        List<ScheduleRawDTO> rawSchedules = scheduleMapper.findAllRawForConflict();
        if (rawSchedules != null) {
            for (ScheduleRawDTO r : rawSchedules) {
                if (r.getTeacherId() == null || r.getWeek() == null || r.getWeekday() == null || r.getSection() == null) continue;
                int sh = Math.max(1, r.getCourseSingleHour() != null ? r.getCourseSingleHour() : 1);//防止数据库填了0，保底每门课占1节课
                for (int s = 0; s < sh; s++) {
                    int sec = r.getSection() + s;
                    addTeacherKey(teacherSet, r.getTeacherId(), r.getWeek(), r.getWeekday(), sec);
                }
            }
        }

        List<TeacherUnavailable> unavailList = teacherUnavailableMapper.findAll();
        if (unavailList != null) {
            for (TeacherUnavailable u : unavailList) {
                Integer teacherId = u.getTeacherId();
                Integer unWeekday = u.getUnWeekday();
                Integer unSection = u.getUnSection();
                if (teacherId == null || unWeekday == null || unSection == null) continue;
                int weekday = unWeekday;
                int section = unSection;
                Integer unWeek = u.getUnWeek();
                if (unWeek == null) {
                    for (int w = 1; w <= MAX_WEEK; w++) {
                        addTeacherKey(teacherSet, teacherId, w, weekday, section);
                    }
                } else {
                    addTeacherKey(teacherSet, teacherId, unWeek, weekday, section);
                }
            }
        }

        // ----- 2. 教室冲突（极速寻租）：Map<教室ID, Set<时间槽>> 哈希 O(1) 寻租 -----
        // ----- 3. 班级冲突（网格防线）：Map<班级ID, boolean[21][8][13]> 三维矩阵直接访问 -----
        if (rawSchedules != null) {
            for (ScheduleRawDTO r : rawSchedules) {
                if (r.getClassroomId() == null || r.getClassId() == null || r.getWeek() == null
                        || r.getWeekday() == null || r.getSection() == null) continue;
                int sh = Math.max(1, r.getCourseSingleHour() != null ? r.getCourseSingleHour() : 1);
                for (int s = 0; s < sh; s++) {
                    int sec = r.getSection() + s;
                    String slotKey = buildSlotKey(r.getWeek(), r.getWeekday(), sec);
                    classroomMap.computeIfAbsent(r.getClassroomId(), k -> new HashSet<>()).add(slotKey);
                    setClassMatrixSlot(classMatrixMap, r.getClassId(), r.getWeek(), r.getWeekday(), sec, true);
                }
            }
        }
    }

    private void addTeacherKey(Set<String> teacherSet, Integer teacherId, int week, int weekday, int section) {
        if (teacherId == null) return;
        teacherSet.add(teacherId + "_" + week + "_" + weekday + "_" + section);
    }

    private String buildSlotKey(int week, int weekday, int section) {
        return week + "_" + weekday + "_" + section;
    }

    /**
     * 设置班级矩阵坐标。
     */
    private void setClassMatrixSlot(Map<Integer, boolean[][][]> classMatrixMap,
                                    Integer classId, int week, int weekday, int section, boolean value) {
        if (classId == null) return;
        boolean[][][] matrix = classMatrixMap.computeIfAbsent(classId, k -> new boolean[21][8][13]);
        if (week >= 1 && week <= 21 && weekday >= 0 && weekday <= 7 && section >= 0 && section <= 12) {
            matrix[week - 1][weekday][section] = value;
        }
    }

    /**
     * 校验班级矩阵坐标是否已被占用。
     */
    private boolean isClassMatrixOccupied(Map<Integer, boolean[][][]> classMatrixMap,
                                         Integer classId, int week, int weekday, int section) {
        if (classId == null) return false;
        boolean[][][] matrix = classMatrixMap.get(classId);
        if (matrix == null) return false;
        if (week < 1 || week > 21 || weekday < 0 || weekday > 7 || section < 0 || section > 12) return false;
        return matrix[week - 1][weekday][section];
    }

    /**
     * 尝试为单个任务排课。
     *
     * @return true 成功，false 失败
     */
    private boolean scheduleOneTask(TeachingTaskVO task,
                                   List<Classroom> classroomsAsc,
                                   Set<String> teacherSet,
                                   Map<Integer, Set<String>> classroomMap,
                                   Map<Integer, boolean[][][]> classMatrixMap) {
        Integer teacherId = task.getTeacherId();
        Integer classId = task.getClassId();
        Integer courseId = task.getCourseId();
        if (teacherId == null || classId == null || courseId == null) {
            return false;
        }

        Course course = courseMapper.findById(courseId);
        Clazz clazz = clazzMapper.findById(classId);
        if (course == null || clazz == null) {
            return false;
        }

        //课程数据初始化
        int startWeek = course.getCourseStartWeek() != null ? course.getCourseStartWeek() : 1;
        int endWeek = course.getCourseEndWeek() != null ? course.getCourseEndWeek() : MAX_WEEK;
        int singleHour = course.getCourseSingleHour() != null ? Math.max(1, course.getCourseSingleHour()) : 1;
        int studentNum = clazz.getClassNum() != null ? clazz.getClassNum() : 0;

        // 时间搜索循环
        for (int weekday = MIN_WEEKDAY; weekday <= MAX_WEEKDAY; weekday++) {
            for (int section = MIN_SECTION; section <= MAX_SECTION; section++) {
                int endSection = section + singleHour - 1;

                // 致命边界防守：不跨越午休(5/6)和晚饭(9/10)
                if (!isSectionRangeValid(section, endSection)) {
                    continue;
                }

                // 寻找教室
                for (Classroom room : classroomsAsc) {
                    if (room.getClassroomCap() == null || room.getClassroomCap() < studentNum) {
                        continue;
                    }

                    // 三大冲突联合校验：该时间窗内所有周次、所有节次均无冲突
                    boolean conflict = false;
                    for (int w = startWeek; w <= endWeek && !conflict; w++) {
                        for (int s = 0; s < singleHour && !conflict; s++) {
                            int sec = section + s;
                            String slotKey = buildSlotKey(w, weekday, sec);

                            if (teacherSet.contains(teacherId + "_" + w + "_" + weekday + "_" + sec)) {
                                conflict = true;
                                break;
                            }
                            if (isClassMatrixOccupied(classMatrixMap, classId, w, weekday, sec)) {
                                conflict = true;
                                break;
                            }
                            Set<String> occupied = classroomMap.get(room.getClassroomId());
                            if (occupied != null && occupied.contains(slotKey)) {
                                conflict = true;
                                break;
                            }
                        }
                    }

                    if (conflict) continue;

                    // 找到完全绿灯：光速占座与落库
                    List<Schedule> toInsert = new ArrayList<>();
                    for (int w = startWeek; w <= endWeek; w++) {
                        Schedule schedule = new Schedule();
                        schedule.setScheduleClassId(classId);
                        schedule.setScheduleTeacherId(teacherId);
                        schedule.setScheduleCourseId(courseId);
                        schedule.setScheduleClassroomId(room.getClassroomId());
                        schedule.setScheduleWeekday(weekday);
                        schedule.setScheduleSection(section);
                        schedule.setScheduleWeek(w);
                        toInsert.add(schedule);
                    }

                    for (Schedule s : toInsert) {
                        scheduleMapper.insert(s);
                    }

                    // 同步更新内存
                    for (int w = startWeek; w <= endWeek; w++) {
                        for (int s = 0; s < singleHour; s++) {
                            int sec = section + s;
                            addTeacherKey(teacherSet, teacherId, w, weekday, sec);
                            classroomMap.computeIfAbsent(room.getClassroomId(), k -> new HashSet<>())
                                    .add(buildSlotKey(w, weekday, sec));
                            setClassMatrixSlot(classMatrixMap, classId, w, weekday, sec, true);
                        }
                    }

                    // 更新任务状态
                    TeachingTask t = new TeachingTask();
                    t.setTaskId(task.getTaskId());
                    t.setTeacherId(task.getTeacherId());
                    t.setCourseId(task.getCourseId());
                    t.setClassId(task.getClassId());
                    t.setTaskState(TASK_STATE_SCHEDULED);
                    teachingTaskMapper.update(t);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 校验节次范围是否合法（不跨越午休、晚饭）。
     */
    private boolean isSectionRangeValid(int section, int endSection) {
        return (section <= 5 && endSection <= 5)
                || (section >= 6 && section <= 9 && endSection <= 9)
                || (section >= 10 && endSection <= 12);
    }

    private int getStudentNum(Integer classId) {
        if (classId == null) return 0;
        Clazz c = clazzMapper.findById(classId);
        return c != null && c.getClassNum() != null ? c.getClassNum() : 0;
    }

    /**
     * 将任务标记为「排课失败」。
     */
    private void markTaskAsFailed(TeachingTaskVO task) {
        if (task == null || task.getTaskId() == null) return;
        TeachingTask t = new TeachingTask();
        t.setTaskId(task.getTaskId());
        t.setTeacherId(task.getTeacherId());
        t.setCourseId(task.getCourseId());
        t.setClassId(task.getClassId());
        t.setTaskState(TASK_STATE_FAILED);
        teachingTaskMapper.update(t);
    }
}
