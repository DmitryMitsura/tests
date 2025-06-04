package com.example.ipcounter.util;

public class IpUtils {
    public static int parseIpToInt(String ipStr) {
        int result = 0;
        int shift = 24;
        int acc = 0;
        int dots = 0;

        for (int i = 0; i < ipStr.length(); i++) {
            char c = ipStr.charAt(i);

            if (c >= '0' && c <= '9') {
                acc = acc * 10 + (c - '0');
                if (acc > 255) throw new IllegalArgumentException("Invalid IP octet: " + acc);
            } else if (c == '.') {
                if (++dots > 3) throw new IllegalArgumentException("Too many dots");
                result |= (acc << shift);
                shift -= 8;
                acc = 0;
            } else if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                throw new IllegalArgumentException("Invalid character in IP: " + c);
            }
        }

        if (dots != 3) throw new IllegalArgumentException("Not enough octets");
        result |= acc;
        return result;
    }
}
