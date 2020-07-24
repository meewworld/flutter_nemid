package dk.meew.flutter_nemid.utilities;

import java.io.UnsupportedEncodingException;

public final class StringHelper {
    public static String toUtf8String(byte[] bytes) {
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 not supported", e);
        }
    }

    public static byte[] toUtf8Bytes(String s) {
        try {
            return s.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 not supported", e);
        }
    }
}
