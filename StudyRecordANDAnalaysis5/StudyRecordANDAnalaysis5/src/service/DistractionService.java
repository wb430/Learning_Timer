package service;

import model.DistractionRecord;

import java.util.List;

/**
 * 分心记录服务接口，定义分心记录的管理方法
 */
public interface DistractionService {
    /**
     * 添加分心记录
     * @param sessionId 会话ID
     * @param reason 分心原因
     * @param autoDetected 是否自动检测
     * @param subject 学习科目
     * @return 分心记录对象
     */
    DistractionRecord addDistraction(String sessionId, String reason, boolean autoDetected, String subject);

    /**
     * 获取指定会话的分心记录
     * @param sessionId 会话ID
     * @return 分心记录列表
     */
    List<DistractionRecord> getDistractionsBySession(String sessionId);

    /**
     * 获取所有分心记录
     * @return 分心记录列表
     */
    List<DistractionRecord> getAllDistractions();

    /**
     * 清除所有分心记录
     */
    void clearAll();
}