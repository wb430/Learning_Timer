package service;
import model.DistractionRecord;
import model.StudySession;

import java.util.List;

public interface StudyDAO {
    // 会话管理
    void saveSession(StudySession session);
    List<StudySession> loadAllSessions();

    // 分心记录管理
    void saveDistractionRecord(DistractionRecord record);
    List<DistractionRecord> loadAllDistractionRecords();
    void deleteAllSessions();
}