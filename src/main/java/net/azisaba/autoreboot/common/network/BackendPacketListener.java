package net.azisaba.autoreboot.common.network;

import net.azisaba.autoreboot.common.network.protocol.BackendboundRebootRequestPacket;
import org.jetbrains.annotations.NotNull;

public interface BackendPacketListener extends PacketListener {
    void handle(@NotNull BackendboundRebootRequestPacket packet);
}
