package net.azisaba.autoreboot.simpleproxy;

import net.azisaba.autoreboot.common.CommonLogger;
import net.azisaba.autoreboot.common.Util;
import net.azisaba.autoreboot.common.network.Protocol;
import net.azisaba.autoreboot.common.network.Side;
import net.azisaba.autoreboot.common.network.protocol.BackendboundRebootRequestPacket;
import net.azisaba.autoreboot.common.util.JedisBox;
import net.azisaba.autoreboot.common.util.LimitedArrayStack;
import net.azisaba.autoreboot.simpleproxy.network.ProxyPacketListenerImpl;
import net.azisaba.simpleProxy.api.event.EventHandler;
import net.azisaba.simpleProxy.api.event.EventPriority;
import net.azisaba.simpleProxy.api.event.connection.RemoteConnectionActiveEvent;
import net.azisaba.simpleProxy.api.event.proxy.ProxyShutdownEvent;
import net.azisaba.simpleProxy.api.plugin.Plugin;
import net.azisaba.simpleProxy.api.yaml.YamlConfiguration;
import net.azisaba.simpleProxy.api.yaml.YamlObject;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.SystemInfo;
import oshi.software.os.OperatingSystem;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class AutoRebootPlugin extends Plugin {
    private static final int SECRET_LENGTH = 128;
    private final OperatingSystem operatingSystem = new SystemInfo().getOperatingSystem();
    private final ProxyPacketListenerImpl proxyPacketListener = new ProxyPacketListenerImpl(this);
    private final JedisBox jedisBox;
    public final boolean doRealReboot;
    @Nullable
    public final String customRebootCommand;
    public final boolean debugAlwaysReboot;
    public final long uptimeThreshold;

    // this thing can store up to 512 KiB of secrets
    // (SECRET_LENGTH * size / 1024) KiB
    public final LimitedArrayStack<byte[]> knownSecrets = new LimitedArrayStack<>(4096);

    public AutoRebootPlugin() {
        try {
            File configFile = new File("./plugins/AutoReboot/config.yml");
            if (configFile.exists() && configFile.isFile()) {
                YamlObject obj = new YamlConfiguration(configFile).asObject();
                YamlObject redisObj = Objects.requireNonNull(obj.getObject("redis"), "redis obj");
                String redisHostname = redisObj.getString("hostname");
                int redisPort = redisObj.getInt("port", 6379);
                String redisUsername = redisObj.getString("username");
                String redisPassword = redisObj.getString("password");
                jedisBox = new JedisBox(
                        Side.PROXY,
                        CommonLogger.create(LogManager.getLogger("AutoReboot")),
                        getProxyPacketListener(),
                        redisHostname,
                        redisPort,
                        redisUsername,
                        redisPassword);
                doRealReboot = obj.getBoolean("do-real-reboot", false);
                customRebootCommand = obj.getString("custom-reboot-command", null);
                debugAlwaysReboot = obj.getBoolean("debug-always-reboot", false);
                uptimeThreshold = obj.getLong("uptime-threshold", 0);
            } else {
                throw new RuntimeException("config.yml does not exist or is not a file.");
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @EventHandler
    public void onProxyShutdown(ProxyShutdownEvent e) {
        getJedisBox().close();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onRemoteConnectionActivation(RemoteConnectionActiveEvent e) {
        if (e.getListenerInfo().getProtocol() != net.azisaba.simpleProxy.api.config.Protocol.TCP) {
            return;
        }
        if (!shouldReboot()) {
            return;
        }
        byte[] secret = Util.generateRandomBytes(SECRET_LENGTH);
        knownSecrets.add(secret);
        getJedisBox().getPubSubHandler().publish(Protocol.PB_REBOOT.getName(), new BackendboundRebootRequestPacket(secret, Util.getIPAddressList()));
    }

    public boolean shouldReboot() {
        return !ProxyPacketListenerImpl.rebootAccepted.get() && (debugAlwaysReboot || operatingSystem.getSystemUptime() > uptimeThreshold);
    }

    @NotNull
    public JedisBox getJedisBox() {
        return jedisBox;
    }

    @NotNull
    public ProxyPacketListenerImpl getProxyPacketListener() {
        return proxyPacketListener;
    }
}
