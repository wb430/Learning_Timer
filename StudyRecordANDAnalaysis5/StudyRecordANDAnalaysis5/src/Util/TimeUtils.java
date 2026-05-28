// utils/TimeUtils.java
package Util;

public class TimeUtils {
    public static String formatTime(long millis) {
        long seconds = millis / 1000;
        long hours = seconds / 3600;
        seconds %= 3600;
        long minutes = seconds / 60;
        seconds %= 60;
        
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
    
    public static String formatDuration(long millis) {
        long seconds = millis / 1000;
        
        if (seconds < 60) {
            return seconds + "秒";
        }
        
        long minutes = seconds / 60;
        seconds %= 60;
        
        if (minutes < 60) {
            return minutes + "分" + seconds + "秒";
        }
        
        long hours = minutes / 60;
        minutes %= 60;
        
        return hours + "小时" + minutes + "分" + seconds + "秒";
    }
}