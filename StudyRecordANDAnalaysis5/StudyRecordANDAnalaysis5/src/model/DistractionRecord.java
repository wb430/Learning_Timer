package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DistractionRecord implements java.io.Serializable {
    // 定义序列化ID
    private static final long serialVersionUID = 1L;

    // 定义分心记录的ID
    private String id;
    // 定义分心记录的时间戳
    private LocalDateTime timestamp;
    // 定义分心记录的原因
    private String reason;
    // 定义分心记录的上下文
    private String subject;
    // 定义分心记录是否为自动检测
    private boolean isAutoDetected;
    // 定义分心记录的会话ID
    private String sessionId;

    // 新增构造函数，支持传入 sessionId
    public DistractionRecord(String sessionId, String subject) {
        this.id = String.valueOf(System.currentTimeMillis());
        this.sessionId = sessionId;
        this.subject = subject;
        this.timestamp = LocalDateTime.now();
        this.reason = "窗口失焦";
        this.isAutoDetected = false;
    }

    // 兼容老代码的构造函数
    public DistractionRecord(String subject) {
        this(System.currentTimeMillis() + "", subject);
    }

    // Getters and setters
    // 设置时间戳
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setId(String id) {
        this.id = id;
    }
    // 获取ID
    public String getId() { return id; }
    // 获取时间戳
    public LocalDateTime getTimestamp() { return timestamp; }


    // 获取原因
    public String getReason() { return reason; }
    // 设置原因
    public void setReason(String reason) { this.reason = reason; }

    public String getSubject() { return subject; }

    public void setSubject(String subject) { this.subject = subject; }
    // 获取是否为自动检测
    public boolean isAutoDetected() { return isAutoDetected; }
    // 设置是否为自动检测
    public void setAutoDetected(boolean autoDetected) { isAutoDetected = autoDetected; }

    // sessionId getter/setter
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    // 格式化时间显示
    public String getFormattedTime() {
        return timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    // 计算分心持续时间
    public long getDurationMillis(LocalDateTime endTime) {
        return java.time.Duration.between(timestamp, endTime).toMillis();
    }
}