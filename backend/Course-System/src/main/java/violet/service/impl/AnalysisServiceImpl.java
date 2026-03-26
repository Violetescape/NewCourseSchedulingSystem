package violet.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import violet.mapper.ClassroomMapper;
import violet.mapper.ClazzMapper;
import violet.mapper.ScheduleMapper;
import violet.pojo.ClassHealthDTO;
import violet.pojo.Classroom;
import violet.pojo.ClassroomEfficiencyDTO;
import violet.pojo.Clazz;
import violet.pojo.HeatmapNodeDTO;
import violet.pojo.ScheduleRawDTO;
import violet.service.AnalysisService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AnalysisServiceImpl implements AnalysisService {

    private static final int TOTAL_WEEKS = 20;
    private static final int TOTAL_WEEKDAYS = 5;
    private static final int TOTAL_SECTIONS = 12;
    private static final double TIME_BASE = TOTAL_WEEKS * TOTAL_WEEKDAYS * TOTAL_SECTIONS;

    @Autowired
    private ScheduleMapper scheduleMapper;
    @Autowired
    private ClassroomMapper classroomMapper;
    @Autowired
    private ClazzMapper clazzMapper;

    @Override
    public List<ClassroomEfficiencyDTO> getClassroomEfficiency() {
        List<Classroom> classrooms = classroomMapper.findAllOrderByCapacityAsc();
        List<ScheduleRawDTO> rawList = safeList(scheduleMapper.findAllRawForConflict());
        List<Clazz> classes = safeList(clazzMapper.findAll());

        Map<Integer, Integer> classSizeMap = classes.stream()
                .filter(c -> c.getClassId() != null)
                .collect(Collectors.toMap(Clazz::getClassId, c -> nvlInt(c.getClassNum()), (a, b) -> a));

        Map<Integer, List<ScheduleRawDTO>> roomSchedules = rawList.stream()
                .filter(r -> r.getClassroomId() != null)
                .collect(Collectors.groupingBy(ScheduleRawDTO::getClassroomId));

        List<ClassroomEfficiencyDTO> result = new ArrayList<>();
        for (Classroom room : safeList(classrooms)) {
            Integer roomId = room.getClassroomId();
            int roomCap = nvlInt(room.getClassroomCap());
            List<ScheduleRawDTO> roomRaw = roomSchedules.getOrDefault(roomId, Collections.emptyList());

            int totalOccupiedSections = 0;
            double ratioSum = 0.0;
            for (ScheduleRawDTO r : roomRaw) {
                int singleHour = safeSingleHour(r.getCourseSingleHour());
                int classSize = classSizeMap.getOrDefault(r.getClassId(), 0);
                double ratio = roomCap > 0 ? (classSize * 1.0 / roomCap) : 0.0;
                ratioSum += ratio * singleHour;
                totalOccupiedSections += singleHour;
            }

            double timeUtilization = totalOccupiedSections / TIME_BASE * 100.0;
            double spaceLoadRate = totalOccupiedSections == 0 ? 0.0 : (ratioSum / totalOccupiedSections) * 100.0;

            result.add(ClassroomEfficiencyDTO.builder()
                    .classroomId(roomId)
                    .classroomName(room.getClassroomName())
                    .timeUtilization(round2(timeUtilization))
                    .spaceLoadRate(round2(spaceLoadRate))
                    .build());
        }
        return result;
    }

    @Override
    public List<ClassHealthDTO> getClassHealth() {
        List<Clazz> classes = safeList(clazzMapper.findAll());
        List<ScheduleRawDTO> rawList = safeList(scheduleMapper.findAllRawForConflict());

        // classId -> week(1..20) -> weekday(1..5) -> occupied sections
        Map<Integer, Map<Integer, Map<Integer, Set<Integer>>>> occupancy = new HashMap<>();
        for (ScheduleRawDTO r : rawList) {
            Integer classId = r.getClassId();
            Integer week = r.getWeek();
            Integer weekday = r.getWeekday();
            Integer startSection = r.getSection();
            if (classId == null || week == null || weekday == null || startSection == null) {
                continue;
            }
            if (week < 1 || week > TOTAL_WEEKS || weekday < 1 || weekday > TOTAL_WEEKDAYS) {
                continue;
            }
            int singleHour = safeSingleHour(r.getCourseSingleHour());
            Set<Integer> secSet = occupancy
                    .computeIfAbsent(classId, k -> new HashMap<>())
                    .computeIfAbsent(week, k -> new HashMap<>())
                    .computeIfAbsent(weekday, k -> new HashSet<>());
            for (int i = 0; i < singleHour; i++) {
                int sec = startSection + i;
                if (sec >= 1 && sec <= TOTAL_SECTIONS) {
                    secSet.add(sec);
                }
            }
        }

        List<ClassHealthDTO> result = new ArrayList<>();
        for (Clazz clazz : classes) {
            Integer classId = clazz.getClassId();
            List<Double> weeklyScores = new ArrayList<>();
            for (int week = 1; week <= TOTAL_WEEKS; week++) {
                Map<Integer, Set<Integer>> dayMap = occupancy
                        .getOrDefault(classId, Collections.emptyMap())
                        .getOrDefault(week, Collections.emptyMap());
                double compactness = calcWeeklyCompactness(dayMap);
                double balance = calcWeeklyBalance(dayMap);
                double score = compactness * 0.6 + balance * 0.4;
                weeklyScores.add(round2(score));
            }
            result.add(ClassHealthDTO.builder()
                    .classId(classId)
                    .className(clazz.getClassName())
                    .weeklyScores(weeklyScores)
                    .build());
        }
        return result;
    }

    @Override
    public Map<Integer, List<HeatmapNodeDTO>> getHeatmapNodes() {
        List<ScheduleRawDTO> rawList = safeList(scheduleMapper.findAllRawForConflict());
        int[][][] counts = new int[TOTAL_WEEKS][TOTAL_WEEKDAYS][TOTAL_SECTIONS];

        for (ScheduleRawDTO r : rawList) {
            Integer week = r.getWeek();
            Integer weekday = r.getWeekday();
            Integer section = r.getSection();
            if (week == null || weekday == null || section == null) {
                continue;
            }
            if (week < 1 || week > TOTAL_WEEKS
                    || weekday < 1 || weekday > TOTAL_WEEKDAYS
                    || section < 1 || section > TOTAL_SECTIONS) {
                continue;
            }
            counts[week - 1][weekday - 1][section - 1] += 1;
        }

        Map<Integer, List<HeatmapNodeDTO>> result = new LinkedHashMap<>();
        for (int week = 1; week <= TOTAL_WEEKS; week++) {
            List<HeatmapNodeDTO> weeklyNodes = new ArrayList<>();
            for (int weekday = 0; weekday < TOTAL_WEEKDAYS; weekday++) {
                for (int section = 0; section < TOTAL_SECTIONS; section++) {
                    weeklyNodes.add(HeatmapNodeDTO.builder()
                            .weekday(weekday)
                            .section(section)
                            .occupiedCount(counts[week - 1][weekday][section])
                            .build());
                }
            }
            result.put(week, weeklyNodes);
        }
        return result;
    }

    private double calcWeeklyCompactness(Map<Integer, Set<Integer>> dayMap) {
        double sum = 0.0;
        for (int weekday = 1; weekday <= TOTAL_WEEKDAYS; weekday++) {
            Set<Integer> sections = dayMap.getOrDefault(weekday, Collections.emptySet());
            sum += calcDayCompactness(sections);
        }
        return sum / TOTAL_WEEKDAYS;
    }

    private double calcDayCompactness(Set<Integer> sections) {
        if (sections == null || sections.isEmpty()) {
            return 100.0;
        }
        double score = 100.0;

        score -= 15.0 * calcFragmentPenalty(sections, 1, 5);
        score -= 15.0 * calcFragmentPenalty(sections, 6, 9);
        score -= 15.0 * calcFragmentPenalty(sections, 10, 12);

        boolean morning = hasInRange(sections, 1, 5);
        boolean afternoon = hasInRange(sections, 6, 9);
        boolean evening = hasInRange(sections, 10, 12);
        int blockCount = (morning ? 1 : 0) + (afternoon ? 1 : 0) + (evening ? 1 : 0);
        if (blockCount <= 1) {
            // no penalty
        } else if (blockCount == 2) {
            if (morning && evening && !afternoon) {
                score -= 25.0;
            } else {
                score -= 10.0;
            }
        } else {
            score -= 20.0;
        }

        return clamp(score, 0.0, 100.0);
    }

    private int calcFragmentPenalty(Set<Integer> sections, int start, int end) {
        List<Integer> inBlock = sections.stream()
                .filter(Objects::nonNull)
                .filter(s -> s >= start && s <= end)
                .sorted()
                .collect(Collectors.toList());
        if (inBlock.isEmpty()) {
            return 0;
        }
        int min = inBlock.get(0);
        int max = inBlock.get(inBlock.size() - 1);
        int actual = inBlock.size();
        return Math.max(0, (max - min + 1) - actual);
    }

    private boolean hasInRange(Set<Integer> sections, int start, int end) {
        return sections.stream().anyMatch(s -> s != null && s >= start && s <= end);
    }

    private double calcWeeklyBalance(Map<Integer, Set<Integer>> dayMap) {
        double[] hours = new double[TOTAL_WEEKDAYS];
        double mean = 0.0;
        for (int i = 0; i < TOTAL_WEEKDAYS; i++) {
            int weekday = i + 1;
            hours[i] = dayMap.getOrDefault(weekday, Collections.emptySet()).size();
            mean += hours[i];
        }
        mean /= TOTAL_WEEKDAYS;

        double variance = 0.0;
        for (double h : hours) {
            double diff = h - mean;
            variance += diff * diff;
        }
        variance /= TOTAL_WEEKDAYS;
        double sigma = Math.sqrt(variance);

        double score = 100.0 - (sigma / 5.0) * 100.0;
        return clamp(score, 0.0, 100.0);
    }

    private int safeSingleHour(Integer singleHour) {
        return singleHour == null ? 1 : Math.max(1, singleHour);
    }

    private int nvlInt(Integer v) {
        return v == null ? 0 : v;
    }

    private <T> List<T> safeList(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }

    private double clamp(double n, double min, double max) {
        return Math.max(min, Math.min(max, n));
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}

