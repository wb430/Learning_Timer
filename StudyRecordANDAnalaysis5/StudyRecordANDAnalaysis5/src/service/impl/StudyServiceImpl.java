package service.impl;

import Util.LoggerUtil;
import service.StudyDAO;
import model.DistractionRecord;
import model.StudySession;
import model.WeeklyReport;
import service.SessionListener;
import service.StudyService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class StudyServiceImpl implements StudyService {
    // 学习数据访问对象
    private final StudyDAO studyDAO;
    // 学习会话列表
    private final List<StudySession> studySessions;
    // 当前学习会话
    private StudySession currentSession;
    // 是否暂停学习会话
    private boolean isSessionPaused = false;
    // 会话计数器
    private final AtomicInteger sessionCounter = new AtomicInteger(0);
    // 会话监听器列表
    private final List<SessionListener> listeners = new CopyOnWriteArrayList<>();

    // 构造函数，初始化学习数据访问对象和学习会话列表
    public StudyServiceImpl() {
        this.studyDAO = new StudyDAOFileImpl();
        this.studySessions = new CopyOnWriteArrayList<>(studyDAO.loadAllSessions());
        sessionCounter.set(studySessions.size());

        LoggerUtil.log(java.util.logging.Level.INFO, "StudyService初始化完成，已加载 " + studySessions.size() + " 个学习会话");
    }

    // 开始新的学习会话
    @Override
    public synchronized void startSession(String subject) {
        LoggerUtil.log(java.util.logging.Level.INFO, "开始新的学习会话: " + subject);

        // 如果当前会话不为空且未完成，则抛出异常
        if (currentSession != null && !currentSession.isCompleted()) {
            throw new IllegalStateException("已有正在进行的学习会话");
        }

        // 创建新的学习会话
        currentSession = new StudySession(subject);
        isSessionPaused = false;



        // 通知监听器会话开始
        notifySessionStarted(currentSession.getId());
    }

    // 暂停学习会话
    @Override
    public void pauseSession() {
        LoggerUtil.log(java.util.logging.Level.INFO, "暂停学习会话");

        // 如果当前会话为空、已完成或已暂停，则直接返回
        if (currentSession == null || currentSession.isCompleted() || isSessionPaused) {
            return;
        }

        // 设置会话暂停状态
        isSessionPaused = true;

    }

    // 恢复学习会话
    @Override
    public void resumeSession() {
        LoggerUtil.log(java.util.logging.Level.INFO, "恢复学习会话");

        // 如果当前会话为空、已完成或未暂停，则直接返回
        if (currentSession == null || currentSession.isCompleted() || !isSessionPaused) {
            return;
        }

        // 设置会话暂停状态
        isSessionPaused = false;

    }

    // 结束学习会话
    @Override
    public synchronized void endSession() {
        LoggerUtil.log(java.util.logging.Level.INFO, "结束学习会话");

        // 如果当前会话为空或已完成，则直接返回
        if (currentSession == null || currentSession.isCompleted()) {
            return;
        }




        // 结束当前会话
        currentSession.endSession();
        // 将当前会话添加到会话列表中
        studySessions.add(currentSession);

        try {
            // 保存当前会话
            studyDAO.saveSession(currentSession);
            LoggerUtil.log(java.util.logging.Level.INFO, "学习会话已保存: " + currentSession.getId());
        } catch (Exception e) {
            LoggerUtil.log(java.util.logging.Level.SEVERE, "保存学习会话失败", e);
        }

        // 清空当前会话
        StudySession savedSession = currentSession;
        currentSession = null;
        isSessionPaused = false;

        // 触发会话完成事件
        notifySessionCompleted(savedSession);
    }

    // 判断是否有学习会话在进行
    @Override
    public boolean isSessionInProgress() {
        return currentSession != null && !currentSession.isCompleted() && !isSessionPaused;
    }

    // 获取当前学习会话
    @Override
    public StudySession getCurrentSession() {
        return currentSession;
    }

    // 添加分心记录
    @Override
    public synchronized void addDistraction(String subject, String reason, LocalDateTime timestamp, boolean isAutoDetected) {
        LoggerUtil.log(java.util.logging.Level.INFO, "添加分心记录: context=" + subject + ", reason=" + reason +
                ", timestamp=" + timestamp + ", isAutoDetected=" + isAutoDetected);

        // 如果当前会话为空，则抛出异常
        if (currentSession == null) {
            throw new IllegalStateException("没有正在进行的学习会话");
        }

        // 创建分心记录，传入 sessionId
        DistractionRecord record = new DistractionRecord(currentSession.getId(), subject);
        record.setReason(reason);
        record.setTimestamp(timestamp);
        record.setAutoDetected(isAutoDetected);

        // 将分心记录添加到当前会话中
        currentSession.addDistraction(record);

        try {
            // 保存分心记录
            studyDAO.saveDistractionRecord(record);
            LoggerUtil.log(java.util.logging.Level.FINE, "分心记录已保存: " + record);

        } catch (Exception e) {
            LoggerUtil.log(java.util.logging.Level.SEVERE, "保存分心记录失败", e);
        }
    }

    // 获取所有分心记录
    @Override
    public List<DistractionRecord> getDistractionRecords() {
        return studyDAO.loadAllDistractionRecords();
    }

    // 根据会话ID获取分心记录
    @Override
    public List<DistractionRecord> getDistractionRecordsBySession(String sessionId) {
        List<DistractionRecord> allRecords = getDistractionRecords();
        return allRecords.stream()
                .filter(record -> sessionId.equals(record.getSessionId()) || sessionId.equals(record.getSubject())) // 兼容老数据
                .collect(Collectors.toList());
    }

    // 获取所有学习会话
    @Override
    public List<StudySession> getAllSessions() {
        return new ArrayList<>(studySessions);
    }

    // 根据科目获取学习会话
    @Override
    public List<StudySession> getSessionsBySubject(String subject) {
        return studySessions.stream()
                .filter(session -> session.getSubject().equalsIgnoreCase(subject))
                .collect(Collectors.toList());
    }

    // 根据日期获取学习会话
    @Override
    public List<StudySession> getSessionsByDate(LocalDate date) {
        return studySessions.stream()
                .filter(session -> session.getStartTime().toLocalDate().equals(date))
                .collect(Collectors.toList());
    }

    // 获取所有科目
    @Override
    public List<String> getAllSubjects() {
        Set<String> subjects = new HashSet<>();
        for (StudySession session : studySessions) {
            subjects.add(session.getSubject());
        }
        return new ArrayList<>(subjects);
    }

    // 生成周报
    @Override
    public WeeklyReport generateWeeklyReport(LocalDate startDate, LocalDate endDate) {
        LoggerUtil.log(java.util.logging.Level.INFO, "生成周报: " + startDate + " - " + endDate);

        // 获取指定日期范围内的学习会话
        List<StudySession> weeklySessions = studySessions.stream()
                .filter(session -> !session.getStartTime().toLocalDate().isBefore(startDate) &&
                        !session.getStartTime().toLocalDate().isAfter(endDate))
                .collect(Collectors.toList());

        // 返回周报
        return new WeeklyReport(startDate, endDate, weeklySessions);
    }

    // 生成月报
    @Override
    public WeeklyReport generateMonthlyReport(LocalDate date) {
        LoggerUtil.log(java.util.logging.Level.INFO, "生成月报: " + date.getMonth() + " " + date.getYear());

        // 获取指定日期范围内的学习会话
        LocalDate startDate = date.withDayOfMonth(1);
        LocalDate endDate = date.withDayOfMonth(date.lengthOfMonth());

        List<StudySession> monthlySessions = studySessions.stream()
                .filter(session -> !session.getStartTime().toLocalDate().isBefore(startDate) &&
                        !session.getStartTime().toLocalDate().isAfter(endDate))
                .collect(Collectors.toList());

        // 返回月报
        return new WeeklyReport(startDate, endDate, monthlySessions);
    }

    // 获取总学习时间
    @Override
    public long getTotalStudyTime() {
        return studySessions.stream()
                .filter(StudySession::isCompleted)
                .mapToLong(StudySession::getDurationMillis)
                .sum();
    }

    // 获取总分心次数
    @Override
    public int getTotalDistractions() {
        return studySessions.stream()
                .filter(StudySession::isCompleted)
                .mapToInt(StudySession::getDistractions)
                .sum();
    }

    // 获取平均专注度
    @Override
    public double getAverageFocusScore() {
        List<StudySession> completedSessions = studySessions.stream()
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

    @Override
    public String getCurrentSessionId() {
        return currentSession != null ? currentSession.getId() : null;
    }

    @Override
    public void addSessionListener(SessionListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeSessionListener(SessionListener listener) {
        listeners.remove(listener);
    }

    private void notifySessionStarted(String sessionId) {
        for (SessionListener listener : listeners) {
            listener.onSessionStarted(sessionId);
        }
    }

    private void notifySessionCompleted(StudySession session) {
        for (SessionListener listener : listeners) {
            listener.onSessionEnded(session.getId());
        }
    }
    @Override
    public List<DistractionRecord> getDistractionRecordsByDateRange(LocalDate startDate, LocalDate endDate) {
        return getDistractionRecords().stream()
                .filter(record -> {
                    LocalDate recordDate = record.getTimestamp().toLocalDate();
                    return !recordDate.isBefore(startDate) && !recordDate.isAfter(endDate);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<DistractionRecord> getDistractionRecordsBySubject(String subject) {
        return getDistractionRecords().stream()
                .filter(record -> record.getSubject().equalsIgnoreCase(subject))
                .collect(Collectors.toList());
    }


    @Override
    public void clearAllStudyRecords() {
        // 清除所有学习会话
        studyDAO.deleteAllSessions();

    }
}