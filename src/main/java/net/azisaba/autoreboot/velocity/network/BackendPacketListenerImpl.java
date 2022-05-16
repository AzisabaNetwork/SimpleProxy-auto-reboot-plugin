package net.azisaba.autoreboot.velocity.network;

import net.azisaba.autoreboot.common.network.BackendPacketListener;
import net.azisaba.autoreboot.common.network.Protocol;
import net.azisaba.autoreboot.common.network.data.InetAddressData;
import net.azisaba.autoreboot.common.network.protocol.BackendboundRebootRequestPacket;
import net.azisaba.autoreboot.common.network.protocol.ProxyboundRebootAckPacket;
import net.azisaba.autoreboot.common.util.Scheduler;
import net.azisaba.autoreboot.velocity.AutoRebootPlugin;
import net.azisaba.autoreboot.velocity.util.PlayerUtil;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class BackendPacketListenerImpl implements BackendPacketListener {
    private final AutoRebootPlugin plugin;

    public BackendPacketListenerImpl(@NotNull AutoRebootPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handle(@NotNull BackendboundRebootRequestPacket packet) {
        plugin.getLogger().info("Received reboot request packet, their IP addresses are: " + packet.getIpAddressList());

        Runnable announce = () -> {
            // announce using BungeeProxyAnnouncer
            for (InetAddressData data : packet.getIpAddressList()) {
                if (data.isLoopbackAddress()) {
                    continue;
                }
                String ip = data.getHostAddress();
                for (Component component : plugin.getRebootComponent()) {
                    PlayerUtil.announce(plugin.getServer(), ip, component, true);
                }
            }
        };

        Scheduler.schedule(1000 * 10, announce); // -30 minutes
        Scheduler.schedule(1000 * 60 * 5, announce); // -25 minutes
        Scheduler.schedule(1000 * 60 * 10, announce); // -20 minutes
        Scheduler.schedule(1000 * 60 * 15, announce); // -15 minutes
        Scheduler.schedule(1000 * 60 * 20, announce); // -10 minutes
        Scheduler.schedule(1000 * 60 * 25, announce); // -5 minutes
        Scheduler.schedule(1000 * 60 * 29, announce); // -1 minutes

        // send reboot_ack packet to notify the proxy server
        plugin.getJedisBox().getPubSubHandler().publish(Protocol.BP_REBOOT_ACK.getName(), new ProxyboundRebootAckPacket(packet.getSecret()));
    }
}
