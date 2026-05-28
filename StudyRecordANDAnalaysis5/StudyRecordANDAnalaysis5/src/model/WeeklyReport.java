// model/WeeklyReport.java
package model;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class WeeklyReport {
    private LocalDate startDate;
    private LocalDate endDate;
    private List<StudySession> sessions;
    
    public WeeklyReport(LocalDate startDate, LocalDate endDate, List<StudySession> sessions) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.sessions = sessions;
    }
    
    // Getters
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public List<StudySession> getSessions() { return sessions; }
    
    // 计算总学习时长
    public long getTotalStudyTime() {
        return sessions.stream()
            .filter(StudySession::isCompleted)
            .mapToLong(StudySession::getDurationMillis)
            .sum();
    }
    
    // 计算总分心次数
    public int getTotalDistractions() {
        return sessions.stream()
            .filter(StudySession::isCompleted)
            .mapToInt(StudySession::getDistractions)
            .sum();
    }
    
    // 计算平均专注度
    public double getAverageFocusScore() {
        List<StudySession> completedSessions = sessions.stream()
            .filter(StudySession::isCompleted)
            .collect(Collectors.toList());
            
        if (completedSessions.isEmpty()) {
            return 0;
        }
        
        return completedSessions.stream()
            .mapToInt(StudySession::getFocusScore)
            .average()
            .orElse(0);
    }
    
    // 按科目统计学习时间
    public Map<String, Long> getStudyTimeBySubject() {
        Map<String, Long> subjectTimeMap = new LinkedHashMap<>();
        
        for (StudySession session : sessions) {
            if (!session.isCompleted()) continue;
            
            String subject = session.getSubject();
            long duration = session.getDurationMillis();
            
            subjectTimeMap.put(subject, subjectTimeMap.getOrDefault(subject, 0L) + duration);
        }
        
        // 按学习时间排序
        return subjectTimeMap.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), LinkedHashMap::putAll);
    }
    
    // 获取某科目学习时间占比
    public double getPercentageBySubject(String subject) {
        long subjectTime = getStudyTimeBySubject().getOrDefault(subject, 0L);
        long totalTime = getTotalStudyTime();
        
        if (totalTime == 0) return 0;
        
        return (double) subjectTime / totalTime * 100;
    }
    
    // 按天统计学习时间
    public Map<LocalDate, Long> getDailyStudyTime() {
        Map<LocalDate, Long> dailyTimeMap = new LinkedHashMap<>();
        
        for (StudySession session : sessions) {
            if (!session.isCompleted()) continue;
            
            LocalDate date = session.getStartTime().toLocalDate();
            long duration = session.getDurationMillis();
            
            dailyTimeMap.put(date, dailyTimeMap.getOrDefault(date, 0L) + duration);
        }
        
        // 按日期排序
        return dailyTimeMap.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), LinkedHashMap::putAll);
    }
    
    // 获取每天的专注度评分
    public Map<LocalDate, Double> getDailyFocusScores() {
        Map<LocalDate, List<StudySession>> dailySessions = new LinkedHashMap<>();
        
        for (StudySession session : sessions) {
            if (!session.isCompleted()) continue;
            
            LocalDate date = session.getStartTime().toLocalDate();
            dailySessions.computeIfAbsent(date, k -> new ArrayList<>()).add(session);
        }
        
        Map<LocalDate, Double> dailyScores = new LinkedHashMap<>();
        
        for (Map.Entry<LocalDate, List<StudySession>> entry : dailySessions.entrySet()) {
            double averageScore = entry.getValue().stream()
                .mapToInt(StudySession::getFocusScore)
                .average()
                .orElse(0);
                
            dailyScores.put(entry.getKey(), averageScore);
        }
        
        // 按日期排序
        return dailyScores.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), LinkedHashMap::putAll);
    }
    
    // 获取分心原因统计
    public Map<String, Integer> getDistractionReasons() {
        Map<String, Integer> reasonCountMap = new LinkedHashMap<>();
        
        for (StudySession session : sessions) {
            for (DistractionRecord record : session.getDistractionRecords()) {
                String reason = record.getReason();
                reasonCountMap.put(reason, reasonCountMap.getOrDefault(reason, 0) + 1);
            }
        }
        
        // 按次数排序
        return reasonCountMap.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), LinkedHashMap::putAll);
    }
}