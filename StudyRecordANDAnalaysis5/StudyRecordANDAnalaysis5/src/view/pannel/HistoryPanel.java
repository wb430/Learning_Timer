// view/panels/HistoryPanel.java
package view.pannel;


import Util.TimeUtils;
import Util.UIUtils;
import model.DistractionRecord;
import model.StudySession;
import service.StudyService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HistoryPanel extends JPanel {
    private final StudyService studyService;
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private JScrollPane scrollPane;
    private JComboBox<String> subjectFilter;
    private JButton refreshButton;
    private JButton viewDetailsButton;

    public HistoryPanel(StudyService studyService) {
        this.studyService = studyService;
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(245, 245, 245));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initComponents();
        setupLayout();
        setupListeners();
        loadHistoryData();
    }

    private void initComponents() {
        // 初始化表格模型
        tableModel = new DefaultTableModel(
            new String[]{"序号", "科目", "开始时间", "时长", "分心次数", "分心时间","专注度"},
            0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // 初始化表格
        historyTable = new JTable(tableModel);
        historyTable.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        historyTable.setRowHeight(30);
        historyTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // 设置列宽
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        historyTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        historyTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        historyTable.getColumnModel().getColumn(6).setPreferredWidth(80);


        // 添加表格到滚动面板
        scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // 初始化过滤器
        subjectFilter = new JComboBox<>();
        subjectFilter.addItem("全部科目");
        
        // 初始化按钮
        refreshButton = new JButton("刷新");
        refreshButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        
        viewDetailsButton = new JButton("查看详情");
        viewDetailsButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        viewDetailsButton.setEnabled(false);
    }

    private void setupLayout() {
        // 顶部面板 - 过滤器和按钮
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        topPanel.setBackground(new Color(245, 245, 245));
        topPanel.add(new JLabel("科目筛选:"));
        topPanel.add(subjectFilter);
        topPanel.add(refreshButton);
        topPanel.add(viewDetailsButton);


        // 添加所有面板到主面板
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void setupListeners() {
        // 刷新按钮事件
        refreshButton.addActionListener(e -> loadHistoryData());
        
        // 科目筛选事件
        subjectFilter.addActionListener(e -> filterHistoryData());
        
        // 表格选择事件
        historyTable.getSelectionModel().addListSelectionListener(e -> {
            viewDetailsButton.setEnabled(historyTable.getSelectedRow() != -1);
        });
        
        // 查看详情按钮事件
        viewDetailsButton.addActionListener(e -> {
            int selectedRow = historyTable.getSelectedRow();
            if (selectedRow != -1) {
                String sessionId = (String) tableModel.getValueAt(selectedRow, 0);
                showSessionDetails(sessionId);
            }
        });
    }

    public void loadHistoryData() {
        // 清空表格
        tableModel.setRowCount(0);
        
        // 获取所有科目并更新筛选器
        subjectFilter.removeAllItems();
        subjectFilter.addItem("全部科目");
        studyService.getAllSubjects().forEach(subjectFilter::addItem);
        
        // 获取所有学习会话
        List<StudySession> sessions = studyService.getAllSessions();
        
        // 按开始时间降序排列
        sessions.sort((s1, s2) -> s2.getStartTime().compareTo(s1.getStartTime()));
        
        // 添加到表格
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        for (int i = 0; i < sessions.size(); i++) {
            StudySession session = sessions.get(i);
            tableModel.addRow(new Object[]{
                session.getId(),
                session.getSubject(),
                session.getStartTime().format(formatter),
                TimeUtils.formatDuration(session.getDurationMillis()),
                session.getDistractions(),
                session.getTotalDistractionTime()/1000,
                session.getFocusScore() + "/10"
            });
        }
    }

    private void filterHistoryData() {
        // 清空表格
        tableModel.setRowCount(0);
        
        String selectedSubject = (String) subjectFilter.getSelectedItem();
        List<StudySession> sessions;
        
        // 根据选择的科目筛选
        if ("全部科目".equals(selectedSubject)) {
            sessions = studyService.getAllSessions();
        } else {
            sessions = studyService.getSessionsBySubject(selectedSubject);
        }
        
        // 按开始时间降序排列
        sessions.sort((s1, s2) -> s2.getStartTime().compareTo(s1.getStartTime()));
        
        // 添加到表格
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        for (StudySession session : sessions) {
            tableModel.addRow(new Object[]{
                session.getId(),
                session.getSubject(),
                session.getStartTime().format(formatter),
                TimeUtils.formatDuration(session.getDurationMillis()),
                session.getDistractions(),
                session.getTotalDistractionTime()/1000,
                session.getFocusScore() + "/10"
            });
        }
    }

    private void showSessionDetails(String sessionId) {
        // 这里简化处理，实际应该根据sessionId获取详细信息
        int selectedRow = historyTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        String subject = (String) tableModel.getValueAt(selectedRow, 1);
        String startTime = (String) tableModel.getValueAt(selectedRow, 2);
        String duration = (String) tableModel.getValueAt(selectedRow, 3);
        int distractions = (int) tableModel.getValueAt(selectedRow, 4);
        long totalDistractionTime = (long) tableModel.getValueAt(selectedRow, 5);
        String focusScore = (String) tableModel.getValueAt(selectedRow, 6);
        
        // 构建详情对话框
        StringBuilder details = new StringBuilder();
        details.append("学习详情\n\n");
        details.append("科目: ").append(subject).append("\n");
        details.append("开始时间: ").append(startTime).append("\n");
        details.append("时长: ").append(duration).append("\n");
        details.append("分心次数: ").append(distractions).append("\n");
        details.append("分心总时间: ").append(totalDistractionTime).append("\n");
        details.append("专注度评分: ").append(focusScore).append("\n\n");
        
        // 添加分心记录
        details.append("分心记录:\n");
        studyService.getDistractionRecordsBySession(sessionId).forEach(record -> {
            details.append("- ")
                   .append(record.getFormattedTime())
                   .append(" - ")
                   .append(record.getReason())
                   .append(record.isAutoDetected() ? " (自动检测)" : "")
                   .append("\n");
        });
        
        // 显示详情对话框
        UIUtils.showInfoDialog("学习详情", details.toString());
    }
}