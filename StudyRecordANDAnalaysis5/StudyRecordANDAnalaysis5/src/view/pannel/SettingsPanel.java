// view/panels/SettingsPanel.java
package view.pannel;


import Util.ConfigUtil;
import Util.UIUtils;
import service.DistractionService;
import service.StudyService;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class SettingsPanel extends JPanel {
    private StudyService studyService;
    private DistractionService distractionService;
    private JCheckBox autoDistractionCheck;
    private JSpinner focusLossThresholdSpinner;
    private JButton clearButton;

    private JButton saveButton;
    private JButton resetButton;

    public SettingsPanel(StudyService studyService, DistractionService distractionService) {
     this.studyService = studyService;
     this.distractionService = distractionService;
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(245, 245, 245));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initComponents();
        setupLayout();
        setupListeners();
        loadSettings();
    }

    private void initComponents() {
        JPanel clearPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        clearPanel.setBackground(new Color(245, 245, 245));

        clearButton = new JButton("清除学习记录");
        clearButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        clearButton.setForeground(Color.RED);
        clearButton.addActionListener(e -> confirmClearRecords());



        // 自动分心检测
        autoDistractionCheck = new JCheckBox("启用自动分心检测");
        
        // 焦点丢失阈值
        focusLossThresholdSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 30, 1));

        // 按钮
        saveButton = new JButton("保存设置");
        resetButton = new JButton("重置默认值");
    }

    private void setupLayout() {
        // 创建网格布局面板
        JPanel settingsPanel = new JPanel(new GridBagLayout());
        settingsPanel.setBackground(new Color(245, 245, 245));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        
        // 添加自动分心检测选项
        gbc.gridwidth = 2;
        settingsPanel.add(autoDistractionCheck, gbc);
        gbc.gridy++;
        
        // 添加焦点丢失阈值
        gbc.gridwidth = 1;
        settingsPanel.add(new JLabel("窗口失焦阈值(秒):"), gbc);
        gbc.gridx = 1;
        settingsPanel.add(focusLossThresholdSpinner, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        

        
        // 添加按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(245, 245, 245));
        buttonPanel.add(clearButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(saveButton);
        
        // 添加所有面板到主面板
        add(new JLabel("学习计时器设置", SwingConstants.CENTER), BorderLayout.NORTH);
        add(settingsPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setupListeners() {
        // 保存按钮事件
        saveButton.addActionListener(e -> saveSettings());
        
        // 重置按钮事件
        resetButton.addActionListener(e -> resetSettings());
    }

    private void loadSettings() {
        // 从配置文件加载设置
        autoDistractionCheck.setSelected(Boolean.parseBoolean(
            ConfigUtil.getProperty("auto.distraction.detection.enabled", "true")));
        
        focusLossThresholdSpinner.setValue(Integer.parseInt(
            ConfigUtil.getProperty("focus.loss.threshold", "3")) / 1000);
        

    }

    private void saveSettings() {
        try {
            // 保存设置到配置文件
            ConfigUtil.setProperty("auto.distraction.detection.enabled", 
                String.valueOf(autoDistractionCheck.isSelected()));
            
            ConfigUtil.setProperty("focus.loss.threshold", 
                String.valueOf((Integer) focusLossThresholdSpinner.getValue() * 1000));
            

            
            // 保存配置文件
            ConfigUtil.saveProperties();
            
            UIUtils.showInfoDialog("成功", "设置已保存！");
            //重启应用
            if (JOptionPane.showConfirmDialog(this, "设置已保存，是否重启应用以应用新设置？", "重启应用", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        } catch (IOException ex) {
            UIUtils.showErrorDialog("错误", "保存设置失败: " + ex.getMessage());
        }
    }

    private void resetSettings() {
        // 重置为默认值
        autoDistractionCheck.setSelected(true);
        focusLossThresholdSpinner.setValue(3);

    }
    private void confirmClearRecords() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "确定要清除所有学习记录和分心记录吗？此操作不可恢复！",
                "确认清除",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {

               studyService.clearAllStudyRecords();

                distractionService.clearAll();

                JOptionPane.showMessageDialog(
                        this,
                        "所有学习记录已成功清除！",
                        "操作成功",
                        JOptionPane.INFORMATION_MESSAGE
                );
                // 重新启动后刷新
                JOptionPane.showMessageDialog(
                        this,
                        "请重新启动应用以刷新！",
                        "操作成功",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "清除记录失败: " + ex.getMessage(),
                        "操作失败",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
}