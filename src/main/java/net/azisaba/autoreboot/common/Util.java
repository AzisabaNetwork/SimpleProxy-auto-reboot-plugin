package net.azisaba.autoreboot.common;

import net.azisaba.autoreboot.common.network.data.InetAddressData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Consumer;
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

    public static void reboot(int timeInMinutes, @Nullable String customRebootCommand) throws RuntimeException, IOException {
        if (customRebootCommand != null && !customRebootCommand.isEmpty()) {
            setupPrinter(Runtime.getRuntime().exec(new String[]{"/bin/bash", "-c", customRebootCommand}));
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
        setupPrinter(Runtime.getRuntime().exec(command));
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
        return toHexString(byteArray, 64);
    }

    @Contract(pure = true)
    public static @NotNull String filterAscii(@NotNull String str) {
        return str.replaceAll("[^\\x20-\\x7F]", "?");
    }

    public static <E> void forEachEnumeration(@NotNull Enumeration<E> enumeration, @NotNull Consumer<E> action) {
        while (enumeration.hasMoreElements()) {
            action.accept(enumeration.nextElement());
        }
    }

    @Contract(pure = true)
    public static @NotNull List<@NotNull InetAddressData> getIPAddressList() {
        List<InetAddressData> list = new ArrayList<>();
        try {
            forEachEnumeration(
                    NetworkInterface.getNetworkInterfaces(),
                    networkInterface ->
                            forEachEnumeration(
                                    networkInterface.getInetAddresses(),
                                    inetAddress ->
                                            list.add(new InetAddressData(inetAddress instanceof Inet4Address, inetAddress.getHostAddress(), inetAddress.isLoopbackAddress()))
                            )
            );
        } catch (SocketException ignored) {
        }
        return list;
    }

    public static void setupPrinter(@NotNull Process process) {
        Thread t = new Thread(() -> {
            try (InputStreamReader isr = new InputStreamReader(process.getInputStream());
                 BufferedReader br = new BufferedReader(isr)) {
                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        t.setDaemon(true);
        t.start();
    }
}
