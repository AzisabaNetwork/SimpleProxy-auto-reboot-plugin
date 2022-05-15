package net.azisaba.autoreboot.common.network;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

public class NamedPacket<P extends PacketListener, T extends Packet<P>> {
    private final String name;
    private final Supplier<@NotNull T> packetConstructor;

    public NamedPacket(@NotNull String name, @NotNull Supplier<T> packetConstructor) {
        this.name = name;
        this.packetConstructor = packetConstructor;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public T create() {
        return Objects.requireNonNull(packetConstructor.get(), "packetConstructor returned null");
    }
}
