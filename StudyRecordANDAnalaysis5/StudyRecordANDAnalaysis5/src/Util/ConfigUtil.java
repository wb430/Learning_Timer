// utils/ConfigUtil.java
package Util;

import java.io.*;
import java.util.Properties;

public class ConfigUtil {
    private static final String CONFIG_FILE = "config.properties";
    private static final Properties properties = new Properties();

    static {
        loadProperties();
    }

    private static void loadProperties() {
        File configFile = new File(CONFIG_FILE);

        try (InputStream input = new FileInputStream(configFile)) {
            properties.load(input);
        } catch (FileNotFoundException e) {
            // 文件不存在时创建默认配置
            System.out.println("配置文件不存在，创建默认配置");
            createDefaultConfig();
        } catch (IOException e) {
            System.err.println("加载配置文件失败: " + e.getMessage());
        }

    }

    private static void createDefaultConfig() {
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            // 设置默认配置
            properties.setProperty("auto.distraction.detection.enabled", "true");
            properties.setProperty("focus.loss.threshold", "3000");  // 3秒


            // 保存默认配置
            properties.store(output, "学习计时器默认配置");
            System.out.println("默认配置文件已创建: " + CONFIG_FILE);
        } catch (IOException e) {
            System.err.println("创建默认配置文件失败: " + e.getMessage());
        }
    }
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    public static void saveProperties() throws IOException {
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            properties.store(output, "学习计时器配置文件");
        }
    }
    public static boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        // 支持多种表示布尔值的字符串
        return value.trim().equalsIgnoreCase("true") ||
                value.trim().equalsIgnoreCase("yes") ||
                value.trim().equalsIgnoreCase("on") ||
                value.trim().equalsIgnoreCase("1");
    }
}