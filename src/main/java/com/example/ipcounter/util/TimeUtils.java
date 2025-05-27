package com.example.ipcounter.util;

import java.time.Duration;

public class TimeUtils {
    public static String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        long millis = duration.toMillisPart();
        return String.format("%d hours, %d minutes, %d seconds, %d milliseconds", hours, minutes, seconds, millis);
    }
}
