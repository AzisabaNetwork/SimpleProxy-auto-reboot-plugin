package net.azisaba.autoreboot.common;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.function.Function;

public class Util {
    private static final SecureRandom RANDOM = new SecureRandom();

    @Contract("null, _ -> null")
    public static <T, R> R mapIfNotNull(@Nullable T t, @NotNull Function<T, R> function) {
        if (t == null) {
            return null;
        }
        return function.apply(t);
    }

    @Contract("null -> fail")
    public static void validateToken(@Nullable String token) {
        if (token == null) {
            throw new IllegalArgumentException("token is null");
        }
        if (token.contains("\"")) {
            throw new IllegalArgumentException("token cannot contain \"");
        }
        if (token.contains("\\")) {
            throw new IllegalArgumentException("token cannot contain \\");
        }
        if (token.length() < 50) {
            throw new IllegalArgumentException("token must be at least 50 characters long");
        }
    }

    public static void reboot(int timeInMinutes, @Nullable String customRebootCommand) throws RuntimeException, IOException {
        if (customRebootCommand != null && !customRebootCommand.isEmpty()) {
            Runtime.getRuntime().exec(customRebootCommand);
            return;
        }
        String os = System.getProperty("os.name");
        String command;
        if ("Linux".equals(os) || "Mac OS X".equals(os)) {
            command = "shutdown -r +" + timeInMinutes;
        } else if ("Windows".equals(os)) {
            command = "shutdown.exe -r -t " + timeInMinutes * 60;
        } else {
            throw new RuntimeException("Unsupported operating system: " + os);
        }
        Runtime.getRuntime().exec(command);
    }

    public static byte @NotNull [] generateRandomBytes(int length) {
        byte[] bytes = new byte[length];
        RANDOM.nextBytes(bytes);
        return bytes;
    }

    private static final char[] LOOKUP_TABLE_LOWER = new char[]{0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66};

    // took from https://stackoverflow.com/a/58118078
    public static @NotNull String toHexString(byte @NotNull [] byteArray, int maxLen) {
        if (maxLen == 0) return "";
        char[] buffer = new char[byteArray.length * 2];
        for (int i = 0; i < byteArray.length; i++) {
            // extract the upper 4 bit and look up char (0-A)
            buffer[i << 1] = LOOKUP_TABLE_LOWER[(byteArray[i] >> 4) & 0xF];
            // extract the lower 4 bit and look up char (0-A)
            buffer[(i << 1) + 1] = LOOKUP_TABLE_LOWER[(byteArray[i] & 0xF)];
        }
        String s = new String(buffer);
        if (maxLen < 0) return s;
        return s.substring(0, Math.min(s.length(), maxLen));
    }

    public static @NotNull String toHexString(byte @NotNull [] byteArray) {
        return toHexString(byteArray, -1);
    }

    @Contract(pure = true)
    public static @NotNull String filterAscii(@NotNull String str) {
        return str.replaceAll("[^\\x20-\\x7F]", "?");
    }
}
