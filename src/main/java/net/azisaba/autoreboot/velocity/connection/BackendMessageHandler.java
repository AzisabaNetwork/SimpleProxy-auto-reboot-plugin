package net.azisaba.autoreboot.velocity.connection;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import net.azisaba.autoreboot.common.Protocol;
import net.azisaba.autoreboot.common.connection.AbstractMessageHandler;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

@SuppressWarnings("unused")
public class BackendMessageHandler extends AbstractMessageHandler {
    public BackendMessageHandler(@NotNull String token) {
        super(token);
    }

    @Override
    public void processPacket(@NotNull Channel ch, @NotNull String packetId, @NotNull ByteBuf buf) {
        if (Protocol.PB_REBOOT.equals(packetId)) {
            // send reboot_ack packet
            ByteBuf buffer = Unpooled.buffer();
            buffer.writeCharSequence(Protocol.MAGIC, StandardCharsets.UTF_8);
            buffer.writeCharSequence(token, StandardCharsets.UTF_8);
            buffer.writeInt(Protocol.BP_REBOOT_ACK.length());
            buffer.writeCharSequence(Protocol.BP_REBOOT_ACK, StandardCharsets.UTF_8);
            Protocol.writeByteArray(buffer, Protocol.readByteArray(buf));
            ch.pipeline().firstContext().flush().writeAndFlush(buffer);
        } else {
            throw new IllegalArgumentException("Unknown packet id " + packetId);
        }
    }
}
