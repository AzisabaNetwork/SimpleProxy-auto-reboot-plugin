package net.azisaba.autoreboot.common.network;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class Packet<T extends PacketListener> {
    public abstract void decode(@NotNull ByteBuf buf);

    public abstract void encode(@NotNull ByteBuf buf);

    public abstract void handle(@NotNull T packetListener);

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

    /**
     * Writes a list to the buffer (int + value).
     * @param buf the buffer
     * @param list the list
     * @param writer the writer
     */
    public static <T> void writeList(@NotNull ByteBuf buf, @NotNull List<T> list, BiConsumer<ByteBuf, T> writer) {
        buf.writeInt(list.size());
        for (T t : list) {
            writer.accept(buf, t);
        }
    }

    /**
     * Reads a list from the buffer (int + value).
     * @param buf the buffer
     * @param reader the reader
     * @return the list
     * @param <T> the type
     * @throws IllegalArgumentException if length is < 0
     */
    @NotNull
    public static <T> List<T> readList(@NotNull ByteBuf buf, @NotNull Function<ByteBuf, T> reader) {
        int len = buf.readInt();
        if (len < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        List<T> list = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            list.add(reader.apply(buf));
        }
        return list;
    }
}
