package net.azisaba.autoreboot.common.util;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public class ByteBufUtil {
    public static byte @NotNull [] toByteArray(@NotNull ByteBuf buf) {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), bytes);
        return bytes;
    }
}
