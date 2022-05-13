package net.azisaba.autoreboot.common;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

/**
 * Packet structure is as follows:
 * <ul>
 *     <li>{@link #MAGIC}</li>
 *     <li>(Pre-configured) Token</li>
 *     <li>Packet ID</li>
 *     <li>(Additional data, if any)</li>
 * </ul>
 */
public final class Protocol {
    @NotNull
    public static final String MAGIC = Protocol.class.getTypeName();

    // Proxy -> Backend

    // [secret (byte array)]
    public static final String PB_REBOOT = "reboot_request";

    // Backend -> Proxy

    // [secret (byte array)]
    public static final String BP_REBOOT_ACK = "reboot_ack";

    public static boolean verifyMagic(@NotNull ByteBuf buf) {
        return buf.readCharSequence(MAGIC.length(), StandardCharsets.UTF_8).toString().equals(MAGIC);
    }

    /**
     * Writes a string to the buffer.
     * @param buf the buffer
     * @param str the string
     */
    public static void writeString(@NotNull ByteBuf buf, @NotNull String str) {
        buf.writeInt(str.length());
        buf.writeCharSequence(str, StandardCharsets.UTF_8);
    }

    /**
     * Reads a string from the buffer.
     * @throws IllegalArgumentException if the string is not valid (e.g. length is < 0)
     * @param buf the buffer
     * @return the string
     */
    @NotNull
    public static String readString(@NotNull ByteBuf buf) {
        int len = buf.readInt();
        if (len < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        return buf.readCharSequence(len, StandardCharsets.UTF_8).toString();
    }

    /**
     * Writes a byte array to the buffer.
     * @param buf the buffer
     * @param bytes the byte array
     */
    public static void writeByteArray(@NotNull ByteBuf buf, byte @NotNull [] bytes) {
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }

    /**
     * Reads a byte array from the buffer.
     * @param buf the buffer
     * @return the byte array
     */
    public static byte @NotNull [] readByteArray(@NotNull ByteBuf buf) {
        int len = buf.readInt();
        if (len < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        byte[] bytes = new byte[len];
        buf.readBytes(bytes);
        return bytes;
    }
}
