package service;

import java.util.EventListener;

/**
 * 学习会话监听器接口，用于监听学习会话的开始和结束事件
 */
public interface SessionListener extends EventListener {
    /**
     * 会话开始事件
     * @param sessionId 会话ID
     */
    void onSessionStarted(String sessionId);
    
    /**
     * 会话结束事件
     * @param sessionId 会话ID
     */
    void onSessionEnded(String sessionId);
}