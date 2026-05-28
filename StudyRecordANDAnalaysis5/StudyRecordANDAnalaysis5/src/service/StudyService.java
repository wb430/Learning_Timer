package service;

import model.DistractionRecord;
import model.StudySession;
import model.WeeklyReport;

import java.time.LocalDate;
import java.util.List;

public interface StudyService {
    // 会话管理
    // 开始会话
    void startSession(String subject);
    // 暂停会话
    void pauseSession();
    // 恢复会话
    void resumeSession();
    // 结束会话
    void endSession();
    // 判断会话是否在进行中
    boolean isSessionInProgress();
    // 获取当前会话
    StudySession getCurrentSession();

    // 分心记录
    // 添加分心记录
    void addDistraction(String context, String reason, java.time.LocalDateTime timestamp, boolean isAutoDetected);
    // 默认添加分心记录，不指定时间戳和是否自动检测
    default void addDistraction(String context, String reason, boolean isAutoDetected) {
        addDistraction(context, reason, java.time.LocalDateTime.now(), isAutoDetected);
    }
    // 默认添加分心记录，不指定原因和时间戳，自动检测为false
    default void addDistraction(String context, String reason) {
        addDistraction(context, reason, java.time.LocalDateTime.now(), false);
    }
    // 默认添加分心记录，不指定原因和时间戳，自动检测为false
    default void addDistraction(String context) {
        addDistraction(context, "未指定原因", java.time.LocalDateTime.now(), false);
    }
    // 获取所有分心记录
    List<DistractionRecord> getDistractionRecords();
    // 根据会话ID获取分心记录
    List<DistractionRecord> getDistractionRecordsBySession(String sessionId);

    // 数据获取
    // 获取所有会话
    List<StudySession> getAllSessions();
    // 根据科目获取会话
    List<StudySession> getSessionsBySubject(String subject);
    // 根据日期获取会话
    List<StudySession> getSessionsByDate(LocalDate date);
    // 获取所有科目
    List<String> getAllSubjects();

    // 报告生成
    // 生成周报
    WeeklyReport generateWeeklyReport(LocalDate startDate, LocalDate endDate);
    // 生成月报
    WeeklyReport generateMonthlyReport(LocalDate date);

    // 统计数据
    // 获取总学习时间
    long getTotalStudyTime();
    // 获取总分心次数
    int getTotalDistractions();
    // 获取平均专注度
    double getAverageFocusScore();


    String getCurrentSessionId();
    void addSessionListener(SessionListener listener);
    void removeSessionListener(SessionListener listener);
    List<DistractionRecord> getDistractionRecordsByDateRange(LocalDate startDate, LocalDate endDate);

    // 新增：根据科目查询分心记录
    List<DistractionRecord> getDistractionRecordsBySubject(String subject);

    void clearAllStudyRecords();
}