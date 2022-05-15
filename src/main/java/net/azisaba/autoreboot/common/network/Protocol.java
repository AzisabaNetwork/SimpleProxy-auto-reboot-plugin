package net.azisaba.autoreboot.common.network;

import net.azisaba.autoreboot.common.network.protocol.BackendboundRebootRequestPacket;
import net.azisaba.autoreboot.common.network.protocol.ProxyboundRebootAckPacket;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Packet structure is as follows:
 * <ul>
 *     <li>Packet ID (int + char sequence)</li>
 *     <li>(Additional data, if any)</li>
 * </ul>
 */
public final class Protocol {
    private static final Map<String, NamedPacket<?, ?>> PACKET_MAP = new ConcurrentHashMap<>();

    // Proxy -> Backend

    // [secret (byte array)]
    // [ip address list (string list)]
    public static final NamedPacket<BackendPacketListener, BackendboundRebootRequestPacket> PB_REBOOT = register("reboot_request", BackendboundRebootRequestPacket::new);

    // Backend -> Proxy

    // [secret (byte array)]
    public static final NamedPacket<ProxyPacketListener, ProxyboundRebootAckPacket> BP_REBOOT_ACK = register("reboot_ack", ProxyboundRebootAckPacket::new);

    @NotNull
    @Contract("_, _ -> new")
    private static <P extends PacketListener, T extends Packet<P>> NamedPacket<P, T> register(@NotNull String name, @NotNull Supplier<T> packetConstructor) {
        if (PACKET_MAP.containsKey(name)) {
            throw new IllegalArgumentException("Duplicate packet name: " + name);
        }
        NamedPacket<P, T> packet = new NamedPacket<>(name, packetConstructor);
        PACKET_MAP.put(packet.getName(), packet);
        return packet;
    }

    @Nullable
    public static NamedPacket<?, ?> getByName(@NotNull String name) {
        return PACKET_MAP.get(name);
    }
}
