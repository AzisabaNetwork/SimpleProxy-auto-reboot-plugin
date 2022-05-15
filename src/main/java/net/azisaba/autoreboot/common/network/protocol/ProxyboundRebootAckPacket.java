package net.azisaba.autoreboot.common.network.protocol;

import io.netty.buffer.ByteBuf;
import net.azisaba.autoreboot.common.network.Packet;
import net.azisaba.autoreboot.common.network.ProxyPacketListener;
import org.jetbrains.annotations.NotNull;

public class ProxyboundRebootAckPacket extends Packet<ProxyPacketListener> {
    private byte[] secret;

    public ProxyboundRebootAckPacket() {
    }

    public ProxyboundRebootAckPacket(byte @NotNull [] secret) {
        this.secret = secret;
    }

    @Override
    public void decode(@NotNull ByteBuf buf) {
        secret = readByteArray(buf);
    }

    @Override
    public void encode(@NotNull ByteBuf buf) {
        writeByteArray(buf, secret);
    }

    @Override
    public void handle(@NotNull ProxyPacketListener packetListener) {
        packetListener.handle(this);
    }

    public byte @NotNull [] getSecret() {
        return secret;
    }
}
