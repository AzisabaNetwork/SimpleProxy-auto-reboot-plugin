package net.azisaba.autoreboot.simpleproxy.connection;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import net.azisaba.autoreboot.common.Protocol;
import net.azisaba.autoreboot.common.Util;
import net.azisaba.autoreboot.common.connection.AbstractMessageHandler;
import net.azisaba.autoreboot.simpleproxy.AutoRebootPlugin;
import net.azisaba.simpleProxy.proxy.ProxyInstance;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProxyMessageHandler extends AbstractMessageHandler {
    private final AutoRebootPlugin plugin;
    private final AtomicBoolean acceptingRebootAck = new AtomicBoolean(true);

    public ProxyMessageHandler(@NotNull AutoRebootPlugin plugin) {
        super(plugin.token);
        this.plugin = plugin;
    }

    @Override
    public void processPacket(@NotNull Channel ch, @NotNull String packetId, @NotNull ByteBuf buf) {
        if (Protocol.BP_REBOOT_ACK.equals(packetId)) {
            // verify the packet
            byte[] secret = Protocol.readByteArray(buf); // secret
            boolean validSecret = false;
            for (byte[] knownSecret : plugin.knownSecrets) {
                if (Arrays.equals(knownSecret, secret)) {
                    validSecret = true;
                    break;
                }
            }
            if (!validSecret) {
                throw new IllegalStateException("Unknown secret " + Util.toHexString(secret, 64));
            }

            if (!acceptingRebootAck.compareAndSet(true, false)) {
                // we are no longer accepting reboot ack
                // the packet itself should be considered as valid, so we just return instead of throwing exception
                return;
            }
            plugin.getLogger().info("Received reboot ack from " + ch.remoteAddress());
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
        } else {
            throw new IllegalArgumentException("Unknown packet id: " + packetId);
        }
    }
}
