package model;

import Util.LoggerUtil;
import service.DistractionService;
import service.StudyService;
import view.pannel.TimerPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

/**
 * 分心检测器 - 整合窗口焦点监听和组件焦点监听功能
 */
public class DistractionDetector implements WindowFocusListener, FocusListener {
    private JFrame mainFrame;
    private DistractionService distractionService;
    private StudyService studyService;
    private TimerPanel timerPanel;
    private List<JPanel> productivePanels; // 学习相关面板
    private Timer focusLossTimer;
    private long focusLossThreshold; // 失焦持续时间阈值（毫秒）
    private boolean isMainFrameFocused = true;
    private Component lastFocusedComponent;
    private LocalDateTime lastDistractionTime;
    private long debounceInterval; // 防抖间隔（毫秒）
    private LocalDateTime sessionStartTime; // 会话开始时间
    private boolean isSessionActive = false; // 会话活动状态
    private Consumer<LocalDateTime> distractioncallback;

    public DistractionDetector(JFrame mainFrame, DistractionService distractionService,
                               StudyService studyService, TimerPanel timerPanel, Consumer<LocalDateTime> callback) {
        this.mainFrame = mainFrame;
        this.distractionService = distractionService;
        this.studyService = studyService;
        this.timerPanel = timerPanel;
        this.distractioncallback = callback;
        this.focusLossThreshold = 3000; // 默认3秒
        this.debounceInterval = 30000; // 默认30秒防抖
        this.productivePanels = new ArrayList<>();

        // 只添加窗口焦点监听器
        mainFrame.addWindowFocusListener(this);

        // 初始化会话状态
        updateSessionStatus();
    }

    /**
     * 添加学习相关面板，切换到这些面板不会被视为分心
     */
    public void addProductivePanel(JPanel panel) {
        productivePanels.add(panel);
        // 为面板及其子组件添加焦点监听器
        addFocusListenersRecursively(panel);
    }

    private void addFocusListenersRecursively(Component component) {
        component.addFocusListener(this);

        if (component instanceof Container) {
            Container container = (Container) component;
            for (Component child : container.getComponents()) {
                addFocusListenersRecursively(child);
            }
        }
    }

    @Override
    public void windowGainedFocus(WindowEvent e) {
        LoggerUtil.log(java.util.logging.Level.INFO, "主窗口获得焦点");
        isMainFrameFocused = true;

        // 取消失焦计时器
        if (focusLossTimer != null) {
            focusLossTimer.cancel();
            focusLossTimer = null;
        }
    }

    @Override
    public void windowLostFocus(WindowEvent e) {
        LoggerUtil.log(java.util.logging.Level.INFO, "主窗口失去焦点");
        isMainFrameFocused = false;

        // 更新会话状态
        updateSessionStatus();

        // 启动失焦计时器
        if (focusLossTimer != null) {
            focusLossTimer.cancel();
        }

        focusLossTimer = new Timer();
        focusLossTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // 双重检查会话状态
                updateSessionStatus();
                if (!isMainFrameFocused && isSessionActive) {
                    recordDistraction("窗口失焦");
                }
            }
        }, focusLossThreshold);
    }

    @Override
    public void focusGained(FocusEvent e) {
        // 不再处理分心逻辑
    }

    @Override
    public void focusLost(FocusEvent e) {
        // 不再处理分心逻辑
    }

    private boolean isComponentInProductivePanel(Component component) {
        if (component == null) {
            return false;
        }

        // 检查组件是否在学习面板中
        for (JPanel panel : productivePanels) {
            if (isComponentInContainer(component, panel)) {
                return true;
            }
        }

        return false;
    }

    private boolean isComponentInContainer(Component component, Container container) {
        if (component == container) {
            return true;
        }

        Component parent = component.getParent();
        while (parent != null) {
            if (parent == container) {
                return true;
            }
            parent = parent.getParent();
        }

        return false;
    }

    private String getComponentDescription(Component component) {
        if (component instanceof JFrame) {
            return ((JFrame) component).getTitle();
        } else if (component instanceof JDialog) {
            return ((JDialog) component).getTitle();
        } else if (component instanceof JComponent) {
            String name = component.getName();
            if (name != null && !name.isEmpty()) {
                return name;
            }
        }

        return component.getClass().getSimpleName();
    }

    private void recordDistraction(String reason) {
        // 再次确认会话状态
        if (!isSessionActive) {
            LoggerUtil.log(java.util.logging.Level.WARNING, "尝试记录分心，但会话已结束");
            return;
        }
        // 获取当前学习科目
        String subject = timerPanel.getSubject();
        if (subject == null || subject.isEmpty()) {
            subject = "未指定科目";
        }
        // 通过StudyService添加分心记录
        studyService.addDistraction(subject, reason, LocalDateTime.now(), true);
        // 更新UI
        SwingUtilities.invokeLater(() -> {
            timerPanel.updateDistractionList();
        });
        LoggerUtil.log(java.util.logging.Level.INFO, "记录分心: " + reason);
    }

    // 更新会话状态
    private void updateSessionStatus() {
        try {
            isSessionActive = studyService.isSessionInProgress();
            if (isSessionActive && sessionStartTime == null) {
                sessionStartTime = LocalDateTime.now();
            }
        } catch (Exception e) {
            LoggerUtil.log(java.util.logging.Level.SEVERE, "更新会话状态失败", e);
            isSessionActive = false;
        }
    }

    // 设置失焦阈值
    public void setFocusLossThreshold(long threshold) {
        this.focusLossThreshold = threshold;
    }

    // 设置防抖间隔
    public void setDebounceInterval(long interval) {
        this.debounceInterval = interval;
    }

    // 释放资源
    public void dispose() {
        if (focusLossTimer != null) {
            focusLossTimer.cancel();
            focusLossTimer = null;
        }

        mainFrame.removeWindowFocusListener(this);
        mainFrame = null;
        distractionService = null;
        studyService = null;
        timerPanel = null;
        productivePanels.clear();
        productivePanels = null;
        sessionStartTime = null;
        isSessionActive = false;
    }

    // 暂停分心检测
    public void pauseDetection() {
        if (mainFrame != null) {
            mainFrame.removeWindowFocusListener(this);
        }
    }

    // 恢复分心检测
    public void resumeDetection() {
        if (mainFrame != null) {
            mainFrame.addWindowFocusListener(this);
        }
    }
}