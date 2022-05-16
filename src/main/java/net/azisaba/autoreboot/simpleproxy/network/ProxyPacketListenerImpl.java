package net.azisaba.autoreboot.simpleproxy.network;

import net.azisaba.autoreboot.common.Util;
import net.azisaba.autoreboot.common.network.ProxyPacketListener;
import net.azisaba.autoreboot.common.network.protocol.ProxyboundRebootAckPacket;
import net.azisaba.autoreboot.simpleproxy.AutoRebootPlugin;
import net.azisaba.simpleProxy.proxy.ProxyInstance;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProxyPacketListenerImpl implements ProxyPacketListener {
    public static final AtomicBoolean rebootAccepted = new AtomicBoolean(false);
    private final AutoRebootPlugin plugin;

    public ProxyPacketListenerImpl(@NotNull AutoRebootPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handle(@NotNull ProxyboundRebootAckPacket packet) {
        // verify the packet
        boolean validSecret = false;
        for (byte[] knownSecret : plugin.knownSecrets) {
            if (Arrays.equals(knownSecret, packet.getSecret())) {
                validSecret = true;
                break;
            }
        }
        if (!validSecret) {
            throw new IllegalStateException("Unknown secret " + Util.filterAscii(Util.toHexString(packet.getSecret())));
        }

        if (!rebootAccepted.compareAndSet(false, true)) {
            // we are no longer accepting reboot ack
            // the packet itself should be considered as valid, so we just return instead of throwing exception
            return;
        }
        plugin.getLogger().info("Received reboot ack");
        // Close listeners
        ProxyInstance.getInstance().getConnectionListener().closeFutures();
        if (plugin.doRealReboot) {
            try {
                plugin.getLogger().info("Attempting to schedule the reboot...");
                Util.reboot(30, plugin.customRebootCommand);
                plugin.getLogger().info("This proxy will reboot in 30 minutes.");
            } catch (Exception e) {
                plugin.getLogger().warn("Failed to schedule reboot", e);
            }
        } else {
            plugin.getLogger().info("Reboot requested, but do-real-reboot is false.");
        }
    }
}
