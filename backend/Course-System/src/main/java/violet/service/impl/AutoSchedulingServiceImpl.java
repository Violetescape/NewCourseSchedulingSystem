package violet.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import violet.mapper.*;
import violet.pojo.AutoSchedulingResult;
import violet.pojo.Classroom;
import violet.pojo.Clazz;
import violet.pojo.Course;
import violet.pojo.ManualScheduleDTO;
import violet.pojo.ManualScheduleValidateResult;
import violet.pojo.Schedule;
import violet.pojo.ScheduleRawDTO;
import violet.pojo.TeacherUnavailable;
import violet.pojo.TeachingTask;
import violet.pojo.TeachingTaskVO;
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
    /** 星期范围：1-5（周一至周五），自动排课搜索用 */
    private static final int MIN_WEEKDAY = 1;
    private static final int MAX_WEEKDAY = 5;
    /** 手动排课允许 1-7，与课表网格一致 */
    private static final int MAX_WEEKDAY_MANUAL = 7;
    /** 节次范围：1-12 */
    private static final int MIN_SECTION = 1;
    private static final int MAX_SECTION = 12;

    /** 记录任务落库后的时空占位（用于一度局部回溯） */
    private static class TaskPlacement {
        int weekday;
        int section;
        int classroomId;
        int startWeek;
        int endWeek;
        int singleHour;
        List<Integer> classIdList;
    }

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

        Map<String, TeachingTaskVO> slotOccupierMap = new HashMap<>();
        Map<Integer, TaskPlacement> taskPlacementMap = new HashMap<>();

        int successCount = 0;
        int failCount = 0;

        // ==================== 二、贪心排课循环 ====================
        for (TeachingTaskVO task : unscheduledTasks) {
            if (scheduleOneTask(task, classroomsAsc, teacherSet, classroomMap, classMatrixMap,
                    slotOccupierMap, taskPlacementMap)) {
                successCount++;
            } else if (tryBacktrackResolution(task, classroomsAsc, teacherSet, classroomMap, classMatrixMap,
                    slotOccupierMap, taskPlacementMap)) {
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
                    int numA = getTotalStudentNum(a.getClassIds());
                    int numB = getTotalStudentNum(b.getClassIds());
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

    private void removeTeacherKey(Set<String> teacherSet, Integer teacherId, int week, int weekday, int section) {
        if (teacherId == null) return;
        teacherSet.remove(teacherId + "_" + week + "_" + weekday + "_" + section);
    }

    private String buildSlotKey(int week, int weekday, int section) {
        return week + "_" + weekday + "_" + section;
    }

    /** 占位追踪键：week_weekday_section_classroomId */
    private String buildOccupierSlotKey(int week, int weekday, int section, int classroomId) {
        return week + "_" + weekday + "_" + section + "_" + classroomId;
    }

    /** 解析逗号分隔的班级 ID 字符串为整数列表 */
    private List<Integer> parseClassIds(String classIds) {
        if (classIds == null || classIds.isBlank()) {
            return Collections.emptyList();
        }
        List<Integer> result = new ArrayList<>();
        for (String part : classIds.split(",")) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            try {
                result.add(Integer.parseInt(trimmed));
            } catch (NumberFormatException ignored) {
                // 跳过非法片段
            }
        }
        return result;
    }

    /** 合班任务：累加所有班级学生人数 */
    private int getTotalStudentNum(String classIds) {
        return parseClassIds(classIds).stream().mapToInt(this::getStudentNum).sum();
    }

    /** 合班冲突：任一班级在该时段已被占用则视为冲突 */
    private boolean isAnyClassMatrixOccupied(Map<Integer, boolean[][][]> classMatrixMap,
                                             List<Integer> classIdList,
                                             int week, int weekday, int section) {
        for (Integer classId : classIdList) {
            if (isClassMatrixOccupied(classMatrixMap, classId, week, weekday, section)) {
                return true;
            }
        }
        return false;
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
                                   Map<Integer, boolean[][][]> classMatrixMap,
                                   Map<String, TeachingTaskVO> slotOccupierMap,
                                   Map<Integer, TaskPlacement> taskPlacementMap) {
        Integer teacherId = task.getTeacherId();
        String classIdsStr = task.getClassIds();
        List<Integer> classIdList = parseClassIds(classIdsStr);
        Integer courseId = task.getCourseId();
        if (teacherId == null || classIdList.isEmpty() || courseId == null) {
            return false;
        }

        Course course = courseMapper.findById(courseId);
        if (course == null) {
            return false;
        }
        for (Integer classId : classIdList) {
            if (clazzMapper.findById(classId) == null) {
                return false;
            }
        }

        //课程数据初始化
        int startWeek = course.getCourseStartWeek() != null ? course.getCourseStartWeek() : 1;
        int endWeek = course.getCourseEndWeek() != null ? course.getCourseEndWeek() : MAX_WEEK;
        int singleHour = course.getCourseSingleHour() != null ? Math.max(1, course.getCourseSingleHour()) : 1;
        int totalStudentNum = getTotalStudentNum(classIdsStr);
        String reqRoomType = course.getRequiredClassroomType();

        // 时间搜索循环：对星期顺序做随机扰动，缓解排课在周前段的头部堆积
        List<Integer> shuffledWeekdays = new ArrayList<>();
        for (int weekday = MIN_WEEKDAY; weekday <= MAX_WEEKDAY; weekday++) {
            shuffledWeekdays.add(weekday);
        }
        Collections.shuffle(shuffledWeekdays);

        for (Integer weekday : shuffledWeekdays) {
            for (int section = MIN_SECTION; section <= MAX_SECTION; section++) {
                int endSection = section + singleHour - 1;

                // 致命边界防守：不跨越午休(5/6)和晚饭(9/10)
                if (!isSectionRangeValid(section, endSection)) {
                    continue;
                }

                // 寻找教室
                for (Classroom room : classroomsAsc) {
                    if (room.getClassroomCap() == null || room.getClassroomCap() < totalStudentNum) {
                        continue;
                    }

                    if (reqRoomType != null && room.getClassroomType() != null
                            && !reqRoomType.equals(room.getClassroomType())) {
                        continue;
                    }

                    if (findFirstConflictReason(teacherId, classIdList, room.getClassroomId(),
                            startWeek, endWeek, weekday, section, singleHour,
                            teacherSet, classroomMap, classMatrixMap) != null) {
                        continue;
                    }

                    // 找到完全绿灯：合班多重落库
                    List<Schedule> toInsert = new ArrayList<>();
                    for (Integer classId : classIdList) {
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
                    }

                    for (Schedule s : toInsert) {
                        scheduleMapper.insert(s);
                    }

                    // 同步更新内存：教师 / 教室 / 全部合班班级矩阵
                    for (int w = startWeek; w <= endWeek; w++) {
                        for (int s = 0; s < singleHour; s++) {
                            int sec = section + s;
                            addTeacherKey(teacherSet, teacherId, w, weekday, sec);
                            classroomMap.computeIfAbsent(room.getClassroomId(), k -> new HashSet<>())
                                    .add(buildSlotKey(w, weekday, sec));
                            for (Integer classId : classIdList) {
                                setClassMatrixSlot(classMatrixMap, classId, w, weekday, sec, true);
                            }
                        }
                    }

                    // 更新任务状态
                    TeachingTask t = new TeachingTask();
                    t.setTaskId(task.getTaskId());
                    t.setTeacherId(task.getTeacherId());
                    t.setCourseId(task.getCourseId());
                    t.setClassIds(task.getClassIds());
                    t.setTaskState(TASK_STATE_SCHEDULED);
                    teachingTaskMapper.update(t);
                    recordTaskOccupancy(task, classIdList, weekday, section, room.getClassroomId(),
                            startWeek, endWeek, singleHour, slotOccupierMap, taskPlacementMap);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 落库成功后记录占位追踪字典，供局部回溯查询「谁占用了该时空资源」。
     */
    private void recordTaskOccupancy(TeachingTaskVO task,
                                     List<Integer> classIdList,
                                     int weekday,
                                     int section,
                                     int classroomId,
                                     int startWeek,
                                     int endWeek,
                                     int singleHour,
                                     Map<String, TeachingTaskVO> slotOccupierMap,
                                     Map<Integer, TaskPlacement> taskPlacementMap) {
        TaskPlacement placement = new TaskPlacement();
        placement.weekday = weekday;
        placement.section = section;
        placement.classroomId = classroomId;
        placement.startWeek = startWeek;
        placement.endWeek = endWeek;
        placement.singleHour = singleHour;
        placement.classIdList = new ArrayList<>(classIdList);
        taskPlacementMap.put(task.getTaskId(), placement);

        for (int w = startWeek; w <= endWeek; w++) {
            for (int s = 0; s < singleHour; s++) {
                int sec = section + s;
                slotOccupierMap.put(buildOccupierSlotKey(w, weekday, sec, classroomId), task);
            }
        }
    }

    /**
     * 从占位追踪字典中清除指定任务的映射。
     */
    private void clearTaskOccupancyFromMaps(TeachingTaskVO task,
                                            TaskPlacement placement,
                                            Map<String, TeachingTaskVO> slotOccupierMap,
                                            Map<Integer, TaskPlacement> taskPlacementMap) {
        if (placement == null) {
            return;
        }
        for (int w = placement.startWeek; w <= placement.endWeek; w++) {
            for (int s = 0; s < placement.singleHour; s++) {
                int sec = placement.section + s;
                slotOccupierMap.remove(buildOccupierSlotKey(w, placement.weekday, sec, placement.classroomId));
            }
        }
        taskPlacementMap.remove(task.getTaskId());
    }

    /**
     * 时空剥离：撤销任务 A 的数据库落库与内存占用标记，并恢复为「未排课」。
     *
     * @return 被剥离前的落位信息，供 fail-safe 原位恢复
     */
    private TaskPlacement rollbackTaskMemoryAndDB(TeachingTaskVO task,
                                                  Set<String> teacherSet,
                                                  Map<Integer, Set<String>> classroomMap,
                                                  Map<Integer, boolean[][][]> classMatrixMap,
                                                  Map<String, TeachingTaskVO> slotOccupierMap,
                                                  Map<Integer, TaskPlacement> taskPlacementMap) {
        TaskPlacement placement = taskPlacementMap.get(task.getTaskId());
        if (placement == null) {
            return null;
        }

        scheduleMapper.deleteByTaskKeys(task.getTeacherId(), task.getCourseId(), placement.classIdList);

        for (int w = placement.startWeek; w <= placement.endWeek; w++) {
            for (int s = 0; s < placement.singleHour; s++) {
                int sec = placement.section + s;
                removeTeacherKey(teacherSet, task.getTeacherId(), w, placement.weekday, sec);
                Set<String> slots = classroomMap.get(placement.classroomId);
                if (slots != null) {
                    slots.remove(buildSlotKey(w, placement.weekday, sec));
                }
                for (Integer classId : placement.classIdList) {
                    setClassMatrixSlot(classMatrixMap, classId, w, placement.weekday, sec, false);
                }
            }
        }

        clearTaskOccupancyFromMaps(task, placement, slotOccupierMap, taskPlacementMap);

        TeachingTask t = new TeachingTask();
        t.setTaskId(task.getTaskId());
        t.setTeacherId(task.getTeacherId());
        t.setCourseId(task.getCourseId());
        t.setClassIds(task.getClassIds());
        t.setTaskState(TASK_STATE_UNSCHEDULED);
        teachingTaskMapper.update(t);

        return placement;
    }

    /**
     * 将任务恢复到指定落位（局部回溯 fail-safe 原位回滚专用，不做冲突搜索）。
     */
    private boolean restoreTaskAtPlacement(TeachingTaskVO task,
                                           TaskPlacement placement,
                                           Set<String> teacherSet,
                                           Map<Integer, Set<String>> classroomMap,
                                           Map<Integer, boolean[][][]> classMatrixMap,
                                           Map<String, TeachingTaskVO> slotOccupierMap,
                                           Map<Integer, TaskPlacement> taskPlacementMap) {
        if (placement == null || task.getTeacherId() == null || task.getClassIds() == null
                || task.getClassIds().isBlank() || task.getCourseId() == null
                || placement.classIdList == null || placement.classIdList.isEmpty()) {
            return false;
        }

        List<Schedule> toInsert = new ArrayList<>();
        for (Integer classId : placement.classIdList) {
            for (int w = placement.startWeek; w <= placement.endWeek; w++) {
                Schedule schedule = new Schedule();
                schedule.setScheduleClassId(classId);
                schedule.setScheduleTeacherId(task.getTeacherId());
                schedule.setScheduleCourseId(task.getCourseId());
                schedule.setScheduleClassroomId(placement.classroomId);
                schedule.setScheduleWeekday(placement.weekday);
                schedule.setScheduleSection(placement.section);
                schedule.setScheduleWeek(w);
                toInsert.add(schedule);
            }
        }
        for (Schedule s : toInsert) {
            scheduleMapper.insert(s);
        }

        for (int w = placement.startWeek; w <= placement.endWeek; w++) {
            for (int s = 0; s < placement.singleHour; s++) {
                int sec = placement.section + s;
                addTeacherKey(teacherSet, task.getTeacherId(), w, placement.weekday, sec);
                classroomMap.computeIfAbsent(placement.classroomId, k -> new HashSet<>())
                        .add(buildSlotKey(w, placement.weekday, sec));
                for (Integer classId : placement.classIdList) {
                    setClassMatrixSlot(classMatrixMap, classId, w, placement.weekday, sec, true);
                }
            }
        }

        TeachingTask t = new TeachingTask();
        t.setTaskId(task.getTaskId());
        t.setTeacherId(task.getTeacherId());
        t.setCourseId(task.getCourseId());
        t.setClassIds(task.getClassIds());
        t.setTaskState(TASK_STATE_SCHEDULED);
        teachingTaskMapper.update(t);

        recordTaskOccupancy(task, placement.classIdList, placement.weekday, placement.section, placement.classroomId,
                placement.startWeek, placement.endWeek, placement.singleHour, slotOccupierMap, taskPlacementMap);
        return true;
    }

    /**
     * 一度局部回溯（踢人置换）：贪心失败后，尝试剥离占用资源的已排任务 A，为当前任务 B 让路。
     * 仅做一层深度，不递归。
     */
    private boolean tryBacktrackResolution(TeachingTaskVO taskB,
                                           List<Classroom> classroomsAsc,
                                           Set<String> teacherSet,
                                           Map<Integer, Set<String>> classroomMap,
                                           Map<Integer, boolean[][][]> classMatrixMap,
                                           Map<String, TeachingTaskVO> slotOccupierMap,
                                           Map<Integer, TaskPlacement> taskPlacementMap) {
        Integer teacherId = taskB.getTeacherId();
        List<Integer> classIdList = parseClassIds(taskB.getClassIds());
        Integer courseId = taskB.getCourseId();
        if (teacherId == null || classIdList.isEmpty() || courseId == null) {
            return false;
        }

        Course course = courseMapper.findById(courseId);
        if (course == null) {
            return false;
        }
        for (Integer classId : classIdList) {
            if (clazzMapper.findById(classId) == null) {
                return false;
            }
        }

        int startWeek = course.getCourseStartWeek() != null ? course.getCourseStartWeek() : 1;
        int endWeek = course.getCourseEndWeek() != null ? course.getCourseEndWeek() : MAX_WEEK;
        int singleHour = course.getCourseSingleHour() != null ? Math.max(1, course.getCourseSingleHour()) : 1;
        int totalStudentNum = getTotalStudentNum(taskB.getClassIds());
        String reqRoomType = course.getRequiredClassroomType();

        List<Integer> shuffledWeekdays = new ArrayList<>();
        for (int weekday = MIN_WEEKDAY; weekday <= MAX_WEEKDAY; weekday++) {
            shuffledWeekdays.add(weekday);
        }
        Collections.shuffle(shuffledWeekdays);

        for (Integer weekday : shuffledWeekdays) {
            for (int section = MIN_SECTION; section <= MAX_SECTION; section++) {
                int endSection = section + singleHour - 1;
                if (!isSectionRangeValid(section, endSection)) {
                    continue;
                }

                for (Classroom room : classroomsAsc) {
                    if (room.getClassroomCap() == null || room.getClassroomCap() < totalStudentNum) {
                        continue;
                    }
                    if (reqRoomType != null && room.getClassroomType() != null
                            && !reqRoomType.equals(room.getClassroomType())) {
                        continue;
                    }

                    TeachingTaskVO taskA = findBacktrackOccupier(taskB, weekday, section,
                            room.getClassroomId(), startWeek, endWeek, singleHour,
                            teacherSet, classMatrixMap, slotOccupierMap);
                    if (taskA == null) {
                        continue;
                    }

                    TaskPlacement savedPlacementA = rollbackTaskMemoryAndDB(taskA, teacherSet, classroomMap,
                            classMatrixMap, slotOccupierMap, taskPlacementMap);
                    if (savedPlacementA == null) {
                        continue;
                    }

                    if (!scheduleOneTask(taskB, classroomsAsc, teacherSet, classroomMap, classMatrixMap,
                            slotOccupierMap, taskPlacementMap)) {
                        restoreTaskAtPlacement(taskA, savedPlacementA, teacherSet, classroomMap, classMatrixMap,
                                slotOccupierMap, taskPlacementMap);
                        continue;
                    }

                    if (!scheduleOneTask(taskA, classroomsAsc, teacherSet, classroomMap, classMatrixMap,
                            slotOccupierMap, taskPlacementMap)) {
                        rollbackTaskMemoryAndDB(taskB, teacherSet, classroomMap, classMatrixMap,
                                slotOccupierMap, taskPlacementMap);
                        restoreTaskAtPlacement(taskA, savedPlacementA, teacherSet, classroomMap, classMatrixMap,
                                slotOccupierMap, taskPlacementMap);
                        continue;
                    }

                    log.info("触发局部回溯成功：任务 {} 置换了 任务 {}", taskB.getTaskId(), taskA.getTaskId());
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 在「任务 B 教师空闲 + 班级空闲」的前提下，查找占用该教室时空资源、且可被踢置换的老任务 A。
     */
    private TeachingTaskVO findBacktrackOccupier(TeachingTaskVO taskB,
                                                 int weekday,
                                                 int section,
                                                 int classroomId,
                                                 int startWeek,
                                                 int endWeek,
                                                 int singleHour,
                                                 Set<String> teacherSet,
                                                 Map<Integer, boolean[][][]> classMatrixMap,
                                                 Map<String, TeachingTaskVO> slotOccupierMap) {
        Integer teacherId = taskB.getTeacherId();
        List<Integer> classIdList = parseClassIds(taskB.getClassIds());
        TeachingTaskVO occupier = null;

        for (int w = startWeek; w <= endWeek; w++) {
            for (int s = 0; s < singleHour; s++) {
                int sec = section + s;
                if (teacherSet.contains(teacherId + "_" + w + "_" + weekday + "_" + sec)) {
                    return null;
                }
                if (isAnyClassMatrixOccupied(classMatrixMap, classIdList, w, weekday, sec)) {
                    return null;
                }

                TeachingTaskVO slotOccupier = slotOccupierMap.get(
                        buildOccupierSlotKey(w, weekday, sec, classroomId));
                if (slotOccupier == null) {
                    return null;
                }
                if (occupier == null) {
                    occupier = slotOccupier;
                } else if (!Objects.equals(occupier.getTaskId(), slotOccupier.getTaskId())) {
                    return null;
                }
            }
        }

        if (occupier == null || Objects.equals(occupier.getTaskId(), taskB.getTaskId())) {
            return null;
        }
        return occupier;
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
        t.setClassIds(task.getClassIds());
        t.setTaskState(TASK_STATE_FAILED);
        teachingTaskMapper.update(t);
    }

    /**
     * 与 {@link #scheduleOneTask} 相同的内存矩阵校验逻辑：命中冲突时返回说明，否则返回 null。
     */
    private String findFirstConflictReason(Integer teacherId,
                                          List<Integer> classIdList,
                                          Integer classroomId,
                                          int startWeek,
                                          int endWeek,
                                          int weekday,
                                          int section,
                                          int singleHour,
                                          Set<String> teacherSet,
                                          Map<Integer, Set<String>> classroomMap,
                                          Map<Integer, boolean[][][]> classMatrixMap) {
        int endSection = section + singleHour - 1;
        if (!isSectionRangeValid(section, endSection)) {
            return "连续课时不能跨越午休或晚饭时段";
        }
        for (int w = startWeek; w <= endWeek; w++) {
            for (int s = 0; s < singleHour; s++) {
                int sec = section + s;
                String slotKey = buildSlotKey(w, weekday, sec);
                if (teacherSet.contains(teacherId + "_" + w + "_" + weekday + "_" + sec)) {
                    return "第" + w + "教学周：教师在该时段已有课或不可用";
                }
                if (isAnyClassMatrixOccupied(classMatrixMap, classIdList, w, weekday, sec)) {
                    return "第" + w + "教学周：合班班级在该时段已有课";
                }
                Set<String> occupied = classroomMap.get(classroomId);
                if (occupied != null && occupied.contains(slotKey)) {
                    return "第" + w + "教学周：所选教室在该时段已被占用";
                }
            }
        }
        return null;
    }

    @Override
    public ManualScheduleValidateResult validateManualSchedule(ManualScheduleDTO dto) {
        String err = validateManualScheduleInternal(dto);
        if (err != null) {
            return ManualScheduleValidateResult.builder().ok(false).message(err).build();
        }
        return ManualScheduleValidateResult.builder().ok(true).message("当前时间与教室可用").build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void manualSchedule(ManualScheduleDTO dto) {
        String err = validateManualScheduleInternal(dto);
        if (err != null) {
            throw new IllegalArgumentException(err);
        }

        TeachingTaskVO task = teachingTaskMapper.findVoByTaskId(dto.getTaskId());
        Course course = courseMapper.findById(task.getCourseId());
        int singleHour = Math.max(1, course.getCourseSingleHour() != null ? course.getCourseSingleHour() : 1);
        int startWeek = dto.getStartWeek() != null ? dto.getStartWeek()
                : (course.getCourseStartWeek() != null ? course.getCourseStartWeek() : 1);
        int endWeek = dto.getEndWeek() != null ? dto.getEndWeek()
                : (course.getCourseEndWeek() != null ? course.getCourseEndWeek() : MAX_WEEK);
        int weekday = dto.getWeekday();
        int section = dto.getSection();

        List<Integer> classIdList = parseClassIds(task.getClassIds());
        List<Schedule> toInsert = new ArrayList<>();
        for (Integer classId : classIdList) {
            for (int w = startWeek; w <= endWeek; w++) {
                Schedule schedule = new Schedule();
                schedule.setScheduleClassId(classId);
                schedule.setScheduleTeacherId(task.getTeacherId());
                schedule.setScheduleCourseId(task.getCourseId());
                schedule.setScheduleClassroomId(dto.getClassroomId());
                schedule.setScheduleWeekday(weekday);
                schedule.setScheduleSection(section);
                schedule.setScheduleWeek(w);
                toInsert.add(schedule);
            }
        }
        for (Schedule s : toInsert) {
            scheduleMapper.insert(s);
        }

        TeachingTask t = new TeachingTask();
        t.setTaskId(task.getTaskId());
        t.setTeacherId(task.getTeacherId());
        t.setCourseId(task.getCourseId());
        t.setClassIds(task.getClassIds());
        t.setTaskState(TASK_STATE_SCHEDULED);
        teachingTaskMapper.update(t);

        log.info("手动排课成功 taskId={}, classroomId={}, weekday={}, section={}, weeks={}-{}",
                task.getTaskId(), dto.getClassroomId(), weekday, section, startWeek, endWeek);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearAndResetSchedules() {
        scheduleMapper.deleteAllSchedules();
        teachingTaskMapper.resetAllTaskStates();
        log.info("一键重置排课完成：已清空 schedule 表并将全部 teaching_task 恢复为「未排课」");
    }

    /**
     * @return 错误提示文案；null 表示通过校验（含冲突矩阵与教室规则）
     */
    private String validateManualScheduleInternal(ManualScheduleDTO dto) {
        if (dto == null || dto.getTaskId() == null || dto.getClassroomId() == null
                || dto.getWeekday() == null || dto.getSection() == null) {
            return "请完整填写任务、教室、星期与节次";
        }

        TeachingTaskVO task = teachingTaskMapper.findVoByTaskId(dto.getTaskId());
        if (task == null) {
            return "教学任务不存在";
        }
        if (!TASK_STATE_UNSCHEDULED.equals(task.getTaskState())) {
            return "仅「未排课」任务可手动编排";
        }

        Integer teacherId = task.getTeacherId();
        List<Integer> classIdList = parseClassIds(task.getClassIds());
        Integer courseId = task.getCourseId();
        if (teacherId == null || classIdList.isEmpty() || courseId == null) {
            return "任务缺少教师、班级或课程信息";
        }

        Course course = courseMapper.findById(courseId);
        Classroom room = classroomMapper.findById(dto.getClassroomId());
        if (course == null) {
            return "课程数据不存在";
        }
        for (Integer classId : classIdList) {
            if (clazzMapper.findById(classId) == null) {
                return "班级数据不存在（ID=" + classId + "）";
            }
        }
        if (room == null) {
            return "教室不存在";
        }
        if (room.getClassroomState() != null && !"可用".equals(room.getClassroomState())) {
            return "教室当前不可用";
        }

        int singleHour = Math.max(1, course.getCourseSingleHour() != null ? course.getCourseSingleHour() : 1);
        int startWeek = dto.getStartWeek() != null ? dto.getStartWeek()
                : (course.getCourseStartWeek() != null ? course.getCourseStartWeek() : 1);
        int endWeek = dto.getEndWeek() != null ? dto.getEndWeek()
                : (course.getCourseEndWeek() != null ? course.getCourseEndWeek() : MAX_WEEK);

        if (startWeek < 1 || endWeek < 1 || startWeek > MAX_WEEK || endWeek > MAX_WEEK) {
            return "教学周必须在 1-" + MAX_WEEK + " 之间";
        }
        if (startWeek > endWeek) {
            return "起始教学周不能大于结束教学周";
        }

        int weekday = dto.getWeekday();
        int section = dto.getSection();
        if (weekday < MIN_WEEKDAY || weekday > MAX_WEEKDAY_MANUAL) {
            return "星期必须在 1（周一）到 7（周日）之间";
        }
        if (section < MIN_SECTION || section > MAX_SECTION) {
            return "节次必须在 " + MIN_SECTION + "-" + MAX_SECTION + " 之间";
        }
        if (section + singleHour - 1 > MAX_SECTION) {
            return "上课节次超出当天最大节次，请减小起始节次或调整课程连堂节数";
        }

        int studentNum = getTotalStudentNum(task.getClassIds());
        if (room.getClassroomCap() != null && room.getClassroomCap() < studentNum) {
            return "教室容量不足（合班需容纳至少 " + studentNum + " 人）";
        }

        String reqRoomType = course.getRequiredClassroomType();
        if (reqRoomType != null && room.getClassroomType() != null
                && !reqRoomType.equals(room.getClassroomType())) {
            return "教室类型不符课程要求（需要：" + reqRoomType + "）";
        }

        Set<String> teacherSet = new HashSet<>();
        Map<Integer, Set<String>> classroomMap = new HashMap<>();
        Map<Integer, boolean[][][]> classMatrixMap = new HashMap<>();
        buildConflictModels(teacherSet, classroomMap, classMatrixMap);

        return findFirstConflictReason(teacherId, classIdList, dto.getClassroomId(),
                startWeek, endWeek, weekday, section, singleHour,
                teacherSet, classroomMap, classMatrixMap);
    }
}
