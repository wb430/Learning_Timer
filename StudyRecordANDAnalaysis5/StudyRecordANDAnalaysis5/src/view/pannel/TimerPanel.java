// view/panels/TimerPanel.java
package view.pannel;


import Util.ConfigUtil;
import Util.LoggerUtil;
import Util.TimeUtils;
import Util.UIUtils;
import model.DistractionDetector;
import model.DistractionRecord;
import model.StudySession;
import service.DistractionService;
import service.StudyService;
import view.MainFrame;
import view.components.CustomButton;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class TimerPanel extends JPanel {
    private final MainFrame mainFrame;
    private final StudyService studyService;
    private final DistractionService distractionService;
    private JLabel timerLabel;
    private JLabel subjectLabel;
    private JLabel distractionLabel;
    private JLabel focusScoreLabel;
    private CustomButton startButton, pauseButton, endButton, distractionButton;
    private JTextField subjectField;
    private Timer timer;
    private long startTimeMillis = 0;
    private long elapsedTimeMillis = 0;
    private int distractionCount = 0;
    private boolean isPaused = false;
    private DistractionDetector distractionDetector;
  //  private ScreenActivityMonitor screenMonitor;
    private DefaultListModel<DistractionRecord> distractionListModel;

    private boolean isAutoDistractionEnabled = true;
    private AtomicInteger consecutiveDistractions = new AtomicInteger(0);


    public TimerPanel(StudyService studyService, MainFrame mainFrame, DistractionService distractionService) {
        this.distractionService = distractionService;
        this.studyService = studyService;
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(245, 245, 245));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initComponents();
        setupLayout();
        setupListeners();
        setupAutoDistractionDetection();
    }

    private void initComponents() {
        // 计时器标签
        timerLabel = new JLabel("00:00:00", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 60));
        timerLabel.setForeground(new Color(67, 160, 71));
        timerLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        // 科目标签和输入框
        subjectLabel = new JLabel("科目: ", SwingConstants.CENTER);
        subjectLabel.setFont(new Font("微软雅黑", Font.PLAIN, 18));

        subjectField = new JTextField("Java", 15);
        subjectField.setFont(new Font("微软雅黑", Font.PLAIN, 16));

        // 分心计数标签
        distractionLabel = new JLabel("分心次数: 0", SwingConstants.CENTER);
        distractionLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        distractionLabel.setForeground(new Color(220, 50, 50));

        // 专注度评分标签
        focusScoreLabel = new JLabel("专注度: 10/10", SwingConstants.CENTER);
        focusScoreLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        focusScoreLabel.setForeground(new Color(33, 150, 243));

        // 按钮
        startButton = new CustomButton("开始学习", new Color(67, 160, 71), Color.WHITE);
        pauseButton = new CustomButton("暂停", new Color(245, 124, 0), Color.WHITE);
        endButton = new CustomButton("结束学习", new Color(211, 47, 47), Color.WHITE);
        distractionButton = new CustomButton("记录分心", new Color(156, 39, 176), Color.WHITE);

        // 禁用初始状态下的按钮
        pauseButton.setEnabled(false);
        endButton.setEnabled(false);
        distractionButton.setEnabled(false);

        distractionListModel = new DefaultListModel<>();

        // 计时器
        timer = new Timer(1000, e -> updateTimer());
    }

    private void setupLayout() {
        // 顶部面板 - 科目选择
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        topPanel.setBackground(new Color(245, 245, 245));
        topPanel.add(subjectLabel);
        topPanel.add(subjectField);

        // 中间面板 - 计时器
        JPanel timerPanel = new JPanel(new BorderLayout());
        timerPanel.setBackground(new Color(245, 245, 245));
        timerPanel.add(timerLabel, BorderLayout.CENTER);

        // 底部面板 - 按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(new Color(245, 245, 245));
        buttonPanel.add(startButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(endButton);
        buttonPanel.add(distractionButton);

        // 分心计数和专注度面板
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        statsPanel.setBackground(new Color(245, 245, 245));
        statsPanel.add(distractionLabel);
        statsPanel.add(focusScoreLabel);

        add(topPanel, BorderLayout.NORTH);
        add(timerPanel, BorderLayout.CENTER);

        // 创建一个底部容器，包含按钮面板和统计面板
        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.add(buttonPanel, BorderLayout.NORTH);
        bottomContainer.add(statsPanel, BorderLayout.SOUTH);

        add(bottomContainer, BorderLayout.SOUTH);

    }

    private void setupListeners() {
        startButton.addActionListener(e -> startStudy());
        pauseButton.addActionListener(e -> pauseStudy());
        endButton.addActionListener(e -> endStudy());
        distractionButton.addActionListener(e -> addDistraction());
    }

    private void setupAutoDistractionDetection() {
        if (ConfigUtil.getBooleanProperty("auto.distraction.detection.enabled", true)) {
            try {
                // 初始化分心检测器
                distractionDetector = new DistractionDetector(mainFrame, distractionService, studyService, this,timestamp -> {


                        SwingUtilities.invokeLater(() -> {
                            addAutoDistraction("窗口失焦");

                        });

                });
                // 添加TimerPanel作为学习相关面板
                distractionDetector.addProductivePanel(this);




                LoggerUtil.log(Level.INFO, "自动分心检测已启用");
            } catch (Exception e) {
                LoggerUtil.log(Level.SEVERE, "初始化自动分心检测失败", e);
                UIUtils.showErrorDialog("错误", "自动分心检测初始化失败: " + e.getMessage());
                isAutoDistractionEnabled = false;
            }
        }

        }


    private void startStudy() {
        String subject = subjectField.getText().trim();
        if (subject.isEmpty()) {
            UIUtils.showInfoDialog("提示", "请输入学习科目");
            return;
        }

        try {
            // 开始新的学习会话
            studyService.startSession(subject);

            // 更新UI
            startButton.setEnabled(false);
            pauseButton.setEnabled(true);
            endButton.setEnabled(true);
            distractionButton.setEnabled(true);
            subjectField.setEnabled(false);

            // 重置计时器
            startTimeMillis = System.currentTimeMillis();
            elapsedTimeMillis = 0;
            timerLabel.setText("00:00:00");
            distractionCount = 0;
            distractionLabel.setText("分心次数: 0");
            focusScoreLabel.setText("专注度: 10/10");
            consecutiveDistractions.set(0);

            // 启动计时器
            timer.start();

            // 启动自动分心检测
            if (isAutoDistractionEnabled) {
                distractionDetector.setFocusLossThreshold(3000); // 3秒无焦点视为分心

            }
            // 3秒倒计时
            startCountdown();
        } catch (Exception ex) {
            LoggerUtil.log(java.util.logging.Level.SEVERE, "开始学习失败", ex);
            UIUtils.showErrorDialog("错误", "开始学习失败: " + ex.getMessage());
        }
    }

    private void startCountdown() {
        timer.stop();
        AtomicInteger countdown = new AtomicInteger(3);
        timerLabel.setText(countdown.toString());
        timerLabel.setForeground(Color.RED);

        Timer countdownTimer = new Timer(1000, e -> {
            int current = countdown.decrementAndGet();
            timerLabel.setText(String.valueOf(current));

            if (current <= 0) {
                ((Timer)e.getSource()).stop();
                timerLabel.setText("00:00:00");
                timerLabel.setForeground(new Color(67, 160, 71));
                timer.start();
            }
        });

        countdownTimer.start();
    }

    private void pauseStudy() {
        if (isPaused) {
            // 恢复学习
            studyService.resumeSession();
            isPaused = false;
            pauseButton.setText("暂停");
            startTimeMillis = System.currentTimeMillis() - elapsedTimeMillis;
            timer.start();

            // 显示恢复提示
            UIUtils.showInfoDialog("提示", "继续学习 " + subjectField.getText());
        } else {
            // 暂停学习
            studyService.pauseSession();
            isPaused = true;
            pauseButton.setText("继续");
            elapsedTimeMillis = System.currentTimeMillis() - startTimeMillis;
            timer.stop();

            // 显示暂停提示
            UIUtils.showInfoDialog("提示", "学习已暂停");
        }
    }

    private void endStudy() {
        try {
            StudySession session = studyService.getCurrentSession(); // 先获取session
            studyService.endSession();
            timer.stop();
            startButton.setEnabled(true);
            pauseButton.setEnabled(false);
            endButton.setEnabled(false);
            distractionButton.setEnabled(false);
            subjectField.setEnabled(true);
            pauseButton.setText("暂停");
            int distractionCount = session != null ? session.getDistractions() : 0;
            String message = "学习科目: " + subjectField.getText() + "\n" +
                    "学习时长: " + TimeUtils.formatDuration(elapsedTimeMillis) + "\n" +
                    "分心次数: " + distractionCount + "\n" +
                    "专注度评分: " + (session != null ? session.getFocusScore() : "N/A") + "/10";
            UIUtils.showInfoDialog("学习完成", message);
            resetUI();
        } catch (Exception ex) {
            LoggerUtil.log(java.util.logging.Level.SEVERE, "结束学习失败", ex);
            UIUtils.showErrorDialog("错误", "结束学习失败: " + ex.getMessage());
        }
    }




    private void addDistraction() {
        if (!studyService.isSessionInProgress() || isPaused) {
            UIUtils.showInfoDialog("提示", "请先开始学习");
            return;
        }
        // 暂停自动分心检测
        if (distractionDetector != null) distractionDetector.pauseDetection();
        String reason = (String) JOptionPane.showInputDialog(
                this,
                "记录分心原因（可选）:",
                "分心记录",
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                ""
        );
        // 恢复自动分心检测
        if (distractionDetector != null) distractionDetector.resumeDetection();
        if (reason == null) return;
        String subject = subjectField.getText();
        studyService.addDistraction(subject, reason.isEmpty() ? "未指定" : reason, LocalDateTime.now(), false);
        SwingUtilities.invokeLater(this::updateDistractionList);
    }

    private void addAutoDistraction(String reason) {
        String subject = subjectField.getText();
        if (studyService.getCurrentSessionId() == null) {
            LoggerUtil.log(Level.WARNING, "尝试添加分心记录，但当前会话ID为空");
            return;
        }
        // 增加条件：确保学习已进行至少10秒，避免开始时误检测
        if (elapsedTimeMillis < 10000) {
            return;
        }
        studyService.addDistraction(subject, reason, LocalDateTime.now(), true);
        // 统一刷新分心列表和UI
        SwingUtilities.invokeLater(this::updateDistractionList);
    }

    private void updateTimer() {
        if (!isPaused) {
            elapsedTimeMillis = System.currentTimeMillis() - startTimeMillis;
            timerLabel.setText(TimeUtils.formatTime(elapsedTimeMillis));
        }
    }

    private void updateFocusScore() {
        // 简单算法：基于分心次数和学习时长计算专注度评分
        double baseScore = 10.0;
        double distractionPenalty = distractionCount * 0.5;
        double timeBonus = Math.min(1.0, elapsedTimeMillis / (30 * 60 * 1000.0)); // 30分钟满分

        int focusScore = Math.max(1, (int) Math.round(baseScore - distractionPenalty + timeBonus));
        focusScoreLabel.setText("专注度: " + focusScore + "/10");

        // 根据专注度设置不同颜色
        if (focusScore >= 8) {
            focusScoreLabel.setForeground(new Color(67, 160, 71)); // 绿色
        } else if (focusScore >= 5) {
            focusScoreLabel.setForeground(new Color(245, 124, 0)); // 橙色
        } else {
            focusScoreLabel.setForeground(new Color(211, 47, 47)); // 红色
        }
    }
    public String getSubject() {
        return subjectField.getText().trim();
    }
    public void updateDistractionList() {
        if (distractionListModel != null) {
            distractionListModel.clear();
            String sessionId = studyService.getCurrentSessionId();
            if (sessionId != null) {
                java.util.List<DistractionRecord> records = studyService.getDistractionRecordsBySession(sessionId);
                for (DistractionRecord record : records) {
                    distractionListModel.addElement(record);
                }
            }
            int distractionCount = studyService.getCurrentSession() != null ? studyService.getCurrentSession().getDistractions() : 0;
            distractionLabel.setText("分心次数: " + distractionCount);
            updateFocusScore();
        }
    }
    private void resetUI() {
        timerLabel.setText("00:00:00");
        distractionLabel.setText("分心次数: 0");
        focusScoreLabel.setText("专注度: 0/10");
        subjectField.setText("Java");
        distractionCount = 0;
        consecutiveDistractions.set(0);



    }

    // 释放资源
    public void dispose() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
        if (distractionDetector != null) {
            distractionDetector.dispose();
        }

    }
}