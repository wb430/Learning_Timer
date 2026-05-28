// utils/LoggerUtil.java
package Util;

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerUtil {
    private static final Logger LOGGER = Logger.getLogger("StudyTimer");

    static {
        try {
            // 创建日志文件处理器
            FileHandler fileHandler = new FileHandler("study_timer.log", true);
            fileHandler.setFormatter(new SimpleFormatter());

            // 添加处理器到日志
            LOGGER.addHandler(fileHandler);

            // 设置日志级别
            LOGGER.setLevel(Level.INFO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void log(Level level, String message) {
        LOGGER.log(level, message);
    }

    public static void log(Level level, String message, Throwable throwable) {
        LOGGER.log(level, message, throwable);
    }
}