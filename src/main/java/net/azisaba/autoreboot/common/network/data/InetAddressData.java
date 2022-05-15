package net.azisaba.autoreboot.common.network.data;

import io.netty.buffer.ByteBuf;
import net.azisaba.autoreboot.common.network.Packet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class InetAddressData {
    private final boolean ipv4;
    private final String hostAddress;

    @Contract(pure = true)
    public InetAddressData(boolean ipv4, @NotNull String hostAddress) {
        this.ipv4 = ipv4;
        this.hostAddress = hostAddress;
    }

    public InetAddressData(@NotNull ByteBuf buf) {
        ipv4 = buf.readBoolean();
        hostAddress = Packet.readString(buf);
    }

    public void encode(@NotNull ByteBuf buf) {
        buf.writeBoolean(ipv4);
        Packet.writeString(buf, hostAddress);
    }

    @Override
    public String toString() {
        return "InetAddressData{" +
                "ipv4=" + ipv4 +
                ", hostAddress='" + hostAddress + '\'' +
                '}';
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InetAddressData)) return false;
        InetAddressData that = (InetAddressData) o;
        return ipv4 == that.ipv4 && hostAddress.equals(that.hostAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ipv4, hostAddress);
    }
}
