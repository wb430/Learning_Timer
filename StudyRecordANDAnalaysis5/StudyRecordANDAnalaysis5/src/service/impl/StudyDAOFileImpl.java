package service.impl;

import Util.LoggerUtil;
import model.DistractionRecord;
import model.StudySession;
import service.StudyDAO;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class StudyDAOFileImpl implements StudyDAO {
    private static final String SESSIONS_FILE = "study_sessions.csv";
    private static final String DISTRACTIONS_FILE = "distraction_records.csv";

    @Override
    public void saveSession(StudySession session) {
        List<StudySession> sessions = loadAllSessions();

        // 检查是否已存在该会话
        boolean found = false;
        for (int i = 0; i < sessions.size(); i++) {
            if (sessions.get(i).getId().equals(session.getId())) {
                sessions.set(i, session);
                found = true;
                break;
            }
        }

        if (!found) {
            sessions.add(session);
        }

        // 保存会话列表
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SESSIONS_FILE))) {
            oos.writeObject(sessions);
        } catch (IOException e) {
            LoggerUtil.log(java.util.logging.Level.SEVERE, "保存学习会话失败", e);
        }
    }

    @Override
    public List<StudySession> loadAllSessions() {
        File file = new File(SESSIONS_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<StudySession>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            LoggerUtil.log(java.util.logging.Level.SEVERE, "加载学习会话失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public void saveDistractionRecord(DistractionRecord record) {
        List<DistractionRecord> records = loadAllDistractionRecords();
        records.add(record);

        // 保存分心记录列表
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DISTRACTIONS_FILE))) {
            oos.writeObject(records);
        } catch (IOException e) {
            LoggerUtil.log(java.util.logging.Level.SEVERE, "保存分心记录失败", e);
        }
    }

    @Override
    public List<DistractionRecord> loadAllDistractionRecords() {
        File file = new File(DISTRACTIONS_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<DistractionRecord>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            LoggerUtil.log(java.util.logging.Level.SEVERE, "加载分心记录失败", e);
            return new ArrayList<>();
        }
    }
    @Override
    public void deleteAllSessions() {
        File file = new File(SESSIONS_FILE);
        if (file.exists()) {
            file.delete();
        }
        File distractionsFile = new File(DISTRACTIONS_FILE);
        if (distractionsFile.exists()) {
            distractionsFile.delete();
        }
    }


}