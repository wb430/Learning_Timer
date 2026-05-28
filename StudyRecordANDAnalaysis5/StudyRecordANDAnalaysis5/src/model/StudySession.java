package model;


import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StudySession implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    // 会话ID
    private String id;
    // 学习科目
    private String subject;
    // 开始时间
    private LocalDateTime startTime;
    // 结束时间
    private LocalDateTime endTime;
    // 持续时间（毫秒）
    private long durationMillis;
    // 分心记录列表
    private List<DistractionRecord> distractions = new ArrayList<>();
    // 是否完成
    private boolean isCompleted = false;
    private int focusScore; // 专注度评分 (1-10)

    public StudySession(String subject) {
        this.id = java.util.UUID.randomUUID().toString();
        this.subject = subject;
        this.startTime = LocalDateTime.now();
        this.focusScore = 5; // 默认中等专注度
    }

    // Getters and setters
    public String getId() { return id; }
    public String getSubject() { return subject; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public long getDurationMillis() { return durationMillis; }
    public int getDistractions() { return distractions.size(); }
    public boolean isCompleted() { return isCompleted; }
    public int getFocusScore() { return focusScore; }
    public void setFocusScore(int focusScore) { this.focusScore = focusScore; }

    public List<DistractionRecord> getDistractionRecords() {
        return distractions;
    }

    public void addDistraction(DistractionRecord record) {
        distractions.add(record);
    }

    public void endSession() {
        if (!isCompleted) {
            this.endTime = LocalDateTime.now();
            this.durationMillis = Duration.between(startTime, endTime).toMillis();
            this.isCompleted = true;
            calculateFocusScore();
        }
    }

    // 计算专注度评分
    private void calculateFocusScore() {
        if (distractions.isEmpty()) {
            focusScore = 10; // 无分心，最高评分
            return;
        }

        // 分心次数越多，评分越低
        long totalDistractionTime = getTotalDistractionTime();
        double distractionRatio = (double) totalDistractionTime / durationMillis;

        // 评分算法
        focusScore = Math.max(1, (int) (10 - distractionRatio * 10 - distractions.size() * 0.5));
    }

    // 计算分心总时间
    public long getTotalDistractionTime() {
        long totalTime = 0;
        LocalDateTime lastDistractionStart = null;

        for (DistractionRecord record : distractions) {
            if (lastDistractionStart != null) {
                totalTime += Duration.between(lastDistractionStart, record.getTimestamp()).toMillis();
            }
            lastDistractionStart = record.getTimestamp();
        }

        // 如果最后一次分心后没有恢复记录，计算到会话结束的时间
        if (lastDistractionStart != null && endTime != null) {
            totalTime += Duration.between(lastDistractionStart, endTime).toMillis();
        }

        return totalTime;
    }

    // 计算分心频率（每分钟分心次数）
    public double getDistractionFrequency() {
        if (durationMillis <= 0) return 0;
        double minutes = durationMillis / (1000.0 * 60);
        return distractions.size() / minutes;
    }
}