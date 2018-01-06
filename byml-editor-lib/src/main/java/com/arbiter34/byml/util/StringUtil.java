package com.arbiter34.byml.util;

public class StringUtil {

    public static byte[] stringToAscii(String string) {
        byte[] bytes = new byte[string.length() + 1];
        for (int i = 0; i < string.length(); i++) {
            bytes[i] = (byte)string.charAt(i);
        }
        bytes[string.length()] = '\0';
        return bytes;
    }
}
