package net.azisaba.autoreboot.common.network;

import net.azisaba.autoreboot.common.network.protocol.ProxyboundRebootAckPacket;
import org.jetbrains.annotations.NotNull;

public interface ProxyPacketListener extends PacketListener {
    void handle(@NotNull ProxyboundRebootAckPacket packet);
}
