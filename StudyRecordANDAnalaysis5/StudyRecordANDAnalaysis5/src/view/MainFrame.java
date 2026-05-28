// view/MainFrame.java
package view;

import service.DistractionService;
import service.StudyService;
import service.impl.DistractionServiceImpl;
import service.impl.StudyServiceImpl;
import view.pannel.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame {
    private static  MainFrame instance;
    private final StudyService studyService;
    private final DistractionService distractionService;
    private TimerPanel timerPanel;
    private HistoryPanel historyPanel;
    private ReportPanel reportPanel;
    private SettingsPanel settingsPanel;
    private CardLayout cardLayout;
    private JPanel contentPanel;

    JMenuItem timerItem = new JMenuItem("计时器");
    JMenuItem historyItem = new JMenuItem("历史记录");
    JMenuItem reportItem = new JMenuItem("学习报告");
    JMenuItem settingsItem = new JMenuItem("设置");
    JMenuItem exitItem = new JMenuItem("退出");

    private MainFrame() {

        this.studyService = new StudyServiceImpl();
        this.distractionService = new DistractionServiceImpl(studyService);
        initComponents();
        setupLayout();
        setupListeners();

        setTitle("学习计时器");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setVisible(true);


    }



    private void initComponents() {
        // 初始化面板
        timerPanel = new TimerPanel(studyService,this, distractionService);
        historyPanel = new HistoryPanel(studyService);
        reportPanel = new ReportPanel(studyService);
        settingsPanel = new SettingsPanel(studyService,distractionService);
        
        // 初始化卡片布局
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        
        // 添加面板到卡片布局
        contentPanel.add(timerPanel, "TIMER");
        contentPanel.add(historyPanel, "HISTORY");
        contentPanel.add(reportPanel, "REPORT");
        contentPanel.add(settingsPanel, "SETTINGS");
    }

    private void setupLayout() {
        // 创建菜单栏
        JMenuBar menuBar = new JMenuBar();
        
        // 添加菜单项到菜单栏
        menuBar.add(timerItem);
        menuBar.add(historyItem);
        menuBar.add(reportItem);
        menuBar.add(settingsItem);
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(exitItem);
        
        // 设置菜单栏
        setJMenuBar(menuBar);
        
        // 添加内容面板
        add(contentPanel, BorderLayout.CENTER);
    }

    private void setupListeners() {
        // 计时器菜单项事件
        timerItem.addActionListener(e -> cardLayout.show(contentPanel, "TIMER"));
        
        // 历史记录菜单项事件
        historyItem.addActionListener(e -> {
            historyPanel.loadHistoryData();
            cardLayout.show(contentPanel, "HISTORY");
        });
        
        // 学习报告菜单项事件
        reportItem.addActionListener(e -> cardLayout.show(contentPanel, "REPORT"));
        
        // 设置菜单项事件
        settingsItem.addActionListener(e -> cardLayout.show(contentPanel, "SETTINGS"));
        
        // 退出菜单项事件
        exitItem.addActionListener(e -> confirmExit());
        
        // 窗口关闭事件
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });
    }

    private void confirmExit() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "确定要退出学习计时器吗？",
            "退出确认",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            // 释放资源
            timerPanel.dispose();
            
            // 退出应用程序
            System.exit(0);
        }

    }
    public static MainFrame getInstance() {
        if (instance == null) {
            synchronized (MainFrame.class) {
                if (instance == null) {
                    instance = new MainFrame();
                }
            }
        }
        return instance;
    }



    public static void main(String[] args) {
        System.out.println("程序启动: " + System.currentTimeMillis());
        try {
            // 使用invokeLater替代invokeAndWait，避免主线程阻塞
            SwingUtilities.invokeLater(() -> {
                try {
                    MainFrame frame = MainFrame.getInstance();
                    System.out.println("MainFrame创建成功: " + frame);
                    frame.setVisible(true);
                } catch (Exception e) {
                    System.err.println("UI初始化异常: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            System.err.println("程序启动失败: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}