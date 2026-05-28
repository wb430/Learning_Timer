package view.pannel;

import Util.TimeUtils;
import Util.UIUtils;
import model.WeeklyReport;
import service.StudyService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ReportPanel extends JPanel {
    private final StudyService studyService;
    private JComboBox<String> timeFilter;
    private JButton generateButton;
    private JTextArea reportTextArea;
    private JScrollPane scrollPane;

    public ReportPanel(StudyService studyService) {
        this.studyService = studyService;
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(245, 245, 245));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initComponents();
        setupLayout();
        setupListeners();
    }

    private void initComponents() {
        // 初始化时间过滤器
        timeFilter = new JComboBox<>();
        timeFilter.addItem("本周");
        timeFilter.addItem("上周");
        timeFilter.addItem("本月");
        timeFilter.addItem("上月");
        timeFilter.addItem("全部");

        // 初始化生成按钮
        generateButton = new JButton("生成报告");
        generateButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        // 初始化报告文本区域
        reportTextArea = new JTextArea();
        reportTextArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        reportTextArea.setEditable(false);
        reportTextArea.setLineWrap(true);
        reportTextArea.setWrapStyleWord(true);

        // 添加文本区域到滚动面板
        scrollPane = new JScrollPane(reportTextArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
    }

    private void setupLayout() {
        // 顶部面板 - 过滤器和按钮
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        topPanel.setBackground(new Color(245, 245, 245));
        topPanel.add(new JLabel("时间范围:"));
        topPanel.add(timeFilter);
        topPanel.add(generateButton);

        // 添加所有面板到主面板
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void setupListeners() {
        // 生成报告按钮事件
        generateButton.addActionListener(e -> generateReport());
    }

    private void generateReport() {
        try {
            String selectedTime = (String) timeFilter.getSelectedItem();
            LocalDate today = LocalDate.now();
            WeeklyReport report;

            switch (selectedTime) {
                case "本周":
                    LocalDate startOfWeek = today.with(java.time.DayOfWeek.MONDAY);
                    report = studyService.generateWeeklyReport(startOfWeek, today);
                    break;
                case "上周":
                    LocalDate startOfLastWeek = today.minusWeeks(1).with(java.time.DayOfWeek.MONDAY);
                    LocalDate endOfLastWeek = startOfLastWeek.plusDays(6);
                    report = studyService.generateWeeklyReport(startOfLastWeek, endOfLastWeek);
                    break;
                case "本月":
                    LocalDate startOfMonth = today.withDayOfMonth(1);
                    LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());
                    report = studyService.generateWeeklyReport(startOfMonth, endOfMonth);
                    break;
                case "上月":
                    LocalDate startOfLastMonth = today.minusMonths(1).withDayOfMonth(1);
                    LocalDate endOfLastMonth = startOfLastMonth.withDayOfMonth(startOfLastMonth.lengthOfMonth());
                    report = studyService.generateWeeklyReport(startOfLastMonth, endOfLastMonth);
                    break;
                default: // 全部
                    report = studyService.generateWeeklyReport(
                            LocalDate.of(2000, 1, 1), // 任意早期日期
                            today
                    );
            }

            displayReport(report);
        } catch (Exception ex) {
            UIUtils.showErrorDialog("错误", "生成报告失败: " + ex.getMessage());
        }
    }

    private void displayReport(WeeklyReport report) {
        StringBuilder reportBuilder = new StringBuilder();

        // 报告标题
        reportBuilder.append("学习报告\n\n");

        // 报告时间范围
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        reportBuilder.append("时间范围: ")
                .append(report.getStartDate().format(formatter))
                .append(" - ")
                .append(report.getEndDate().format(formatter))
                .append("\n\n");

        // 学习统计
        reportBuilder.append("总学习时长: ").append(TimeUtils.formatDuration(report.getTotalStudyTime())).append("\n");
        reportBuilder.append("总分心次数: ").append(report.getTotalDistractions()).append("\n");
        reportBuilder.append("平均专注度: ").append(String.format("%.1f", report.getAverageFocusScore())).append("/10\n\n");

        // 按科目统计
        reportBuilder.append("按科目统计:\n");
        report.getStudyTimeBySubject().forEach((subject, time) -> {
            reportBuilder.append("- ")
                    .append(subject)
                    .append(": ")
                    .append(TimeUtils.formatDuration(time))
                    .append(" (")
                    .append(String.format("%.1f", report.getPercentageBySubject(subject)))
                    .append("%)\n");
        });
        reportBuilder.append("\n");

        // 按天统计
        reportBuilder.append("按天统计:\n");
        report.getDailyStudyTime().forEach((date, time) -> {
            reportBuilder.append("- ")
                    .append(date.format(formatter))
                    .append(": ")
                    .append(TimeUtils.formatDuration(time))
                    .append("\n");
        });
        reportBuilder.append("\n");

        // 专注度趋势
        reportBuilder.append("专注度趋势:\n");
        report.getDailyFocusScores().forEach((date, score) -> {
            reportBuilder.append("- ")
                    .append(date.format(formatter))
                    .append(": ")
                    .append(String.format("%.1f", score))
                    .append("/10\n");
        });
        reportBuilder.append("\n");

        // 分心原因分析
        reportBuilder.append("分心原因分析:\n");
        report.getDistractionReasons().forEach((reason, count) -> {
            reportBuilder.append("- ")
                    .append(reason)
                    .append(": ")
                    .append(count)
                    .append("次\n");
        });

        // 显示报告
        reportTextArea.setText(reportBuilder.toString());
    }
}