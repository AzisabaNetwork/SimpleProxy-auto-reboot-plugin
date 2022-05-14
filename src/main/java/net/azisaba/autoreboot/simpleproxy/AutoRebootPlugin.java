package net.azisaba.autoreboot.simpleproxy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.azisaba.autoreboot.common.Protocol;
import net.azisaba.autoreboot.common.Util;
import net.azisaba.autoreboot.common.util.LimitedArrayStack;
import net.azisaba.autoreboot.simpleproxy.connection.ProxyMessageHandler;
import net.azisaba.simpleProxy.api.event.EventHandler;
import net.azisaba.simpleProxy.api.event.EventPriority;
import net.azisaba.simpleProxy.api.event.connection.RemoteConnectionActiveEvent;
import net.azisaba.simpleProxy.api.event.proxy.ProxyInitializeEvent;
import net.azisaba.simpleProxy.api.plugin.Plugin;
import net.azisaba.simpleProxy.api.yaml.YamlConfiguration;
import net.azisaba.simpleProxy.api.yaml.YamlObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.SystemInfo;
import oshi.software.os.OperatingSystem;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class AutoRebootPlugin extends Plugin {
    private static final int SECRET_LENGTH = 128;
    private final OperatingSystem operatingSystem = new SystemInfo().getOperatingSystem();
    private final File configFile = new File("./plugins/AutoReboot/config.yml");
    @NotNull public String token = "";
    public boolean doRealReboot = false;
    @Nullable public String customRebootCommand = null;
    public boolean debugAlwaysReboot = false;
    public long uptimeThreshold = 0;

    // this thing can store up to 512 KiB of secrets
    // (SECRET_LENGTH * size / 1024) KiB
    public final LimitedArrayStack<byte[]> knownSecrets = new LimitedArrayStack<>(4096);

    @EventHandler
    public void onProxyInitialization(ProxyInitializeEvent e) {
        try {
            if (configFile.exists() && configFile.isFile()) {
                YamlObject obj = new YamlConfiguration(configFile).asObject();
                token = obj.getString("token");
                doRealReboot = obj.getBoolean("do-real-reboot", false);
                customRebootCommand = obj.getString("custom-reboot-command", null);
                debugAlwaysReboot = obj.getBoolean("debug-always-reboot", false);
                uptimeThreshold = obj.getLong("uptime-threshold", 0);
            }
        } catch (IOException ex) {
            getLogger().error("Failed to load config.yml", ex);
        }
        Util.validateToken(token);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onRemoteConnectionActivation(RemoteConnectionActiveEvent e) {
        if (e.getListenerInfo().getProtocol() != net.azisaba.simpleProxy.api.config.Protocol.TCP) {
            return;
        }
        if (!shouldReboot()) {
            return;
        }
        ByteBuf buf = Unpooled.buffer();
        buf.writeCharSequence(Protocol.MAGIC, StandardCharsets.UTF_8); // magic
        buf.writeCharSequence(token, StandardCharsets.UTF_8); // token
        Protocol.writeString(buf, Protocol.PB_REBOOT); // packet id
        byte[] secret = Util.generateRandomBytes(SECRET_LENGTH);
        knownSecrets.add(secret);
        Protocol.writeByteArray(buf, secret); // secret
        e.getChannel().pipeline().addFirst(new ProxyMessageHandler(this));
        // flush is required before writeAndFlush because there is some unsent buffer
        e.getChannel().flush().writeAndFlush(buf);
    }

    public boolean shouldReboot() {
        return debugAlwaysReboot || operatingSystem.getSystemUptime() > uptimeThreshold;
    }
}
