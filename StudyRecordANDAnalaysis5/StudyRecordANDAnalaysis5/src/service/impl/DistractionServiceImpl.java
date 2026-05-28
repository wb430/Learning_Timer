package service.impl;

import model.DistractionRecord;
import service.DistractionService;
import service.StudyDAO;
import service.StudyService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 分心记录服务实现类，负责管理分心记录的存储和查询
 */
public class DistractionServiceImpl implements DistractionService {
    private final StudyDAO studyDAO;
    private final StudyService studyService;
    private final Map<String, List<DistractionRecord>> sessionRecords;
    private int nextRecordId = 1;

    public DistractionServiceImpl(StudyService studyService) {
        this.studyDAO = new StudyDAOFileImpl();
        this.studyService = studyService;
        this.sessionRecords = new HashMap<>();
    }

    @Override
    public synchronized DistractionRecord addDistraction(String sessionId, String reason, boolean autoDetected, String subject) {
        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString();
        }
        // 创建新的分心记录
        DistractionRecord record = new DistractionRecord(subject);
        record.setId(String.valueOf(nextRecordId++));
        record.setSubject(subject);
        record.setTimestamp(LocalDateTime.now());
        record.setReason(reason);
        record.setAutoDetected(autoDetected);
        // 添加到会话记录
        sessionRecords.computeIfAbsent(sessionId, k -> new ArrayList<>()).add(record);
        System.out.println("添加分心记录: sessionId=" + sessionId + ", reason=" + reason  + ", subject=" + record.getSubject());
        return record;
    }

    @Override
    public synchronized List<DistractionRecord> getDistractionsBySession(String sessionId) {
        return sessionRecords.getOrDefault(sessionId, new ArrayList<>());
    }

    @Override
    public synchronized List<DistractionRecord> getAllDistractions() {
        List<DistractionRecord> allRecords = new ArrayList<>();
        for (List<DistractionRecord> records : sessionRecords.values()) {
            allRecords.addAll(records);
        }
        System.out.println("获取本次学习所有分心记录: 共 " + allRecords.size() + " 条");
        for (DistractionRecord r : allRecords) {
            System.out.println("记录: id=" + r.getId() + ", reason=" + r.getReason() + ", subject=" + r.getSubject());
        }
        return allRecords;
    }

    @Override
    public synchronized void clearAll() {
        studyDAO.deleteAllSessions();

    }
}