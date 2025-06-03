package com.example.ipcounter.util;

public class IpUtils {
    public static long parseIpToLong(String ipStr) {
        String[] parts = ipStr.trim().split("\\.");
        if (parts.length != 4) throw new IllegalArgumentException("Invalid IP format");
        long ip = 0;
        for (String part : parts) {
            int octet = Integer.parseInt(part);
            if (octet < 0 || octet > 255)
                throw new IllegalArgumentException("Invalid IP octet: " + part);
            ip = (ip << 8) | octet;
        }
        return ip;
    }
}
