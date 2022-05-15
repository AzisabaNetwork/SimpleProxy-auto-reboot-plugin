package net.azisaba.autoreboot.velocity.network;

import net.azisaba.autoreboot.common.network.BackendPacketListener;
import net.azisaba.autoreboot.common.network.Protocol;
import net.azisaba.autoreboot.common.network.protocol.BackendboundRebootRequestPacket;
import net.azisaba.autoreboot.common.network.protocol.ProxyboundRebootAckPacket;
import net.azisaba.autoreboot.velocity.AutoRebootPlugin;
import org.jetbrains.annotations.NotNull;

public class BackendPacketListenerImpl implements BackendPacketListener {
    private final AutoRebootPlugin plugin;

    public BackendPacketListenerImpl(@NotNull AutoRebootPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handle(@NotNull BackendboundRebootRequestPacket packet) {
        plugin.getLogger().info("Hi {}!", packet.getIpAddressList());
        // send reboot_ack packet
        plugin.getJedisBox().getPubSubHandler().publish(Protocol.BP_REBOOT_ACK.getName(), new ProxyboundRebootAckPacket(packet.getSecret()));
    }
}
