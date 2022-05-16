package net.azisaba.autoreboot.common.network.data;

import io.netty.buffer.ByteBuf;
import net.azisaba.autoreboot.common.network.Packet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class InetAddressData {
    private final boolean ipv4;
    private final String hostAddress;
    private final boolean loopbackAddress;

    @Contract(pure = true)
    public InetAddressData(boolean ipv4, @NotNull String hostAddress, boolean loopbackAddress) {
        Objects.requireNonNull(hostAddress, "hostAddress");
        this.ipv4 = ipv4;
        this.hostAddress = hostAddress;
        this.loopbackAddress = loopbackAddress;
    }

    public InetAddressData(@NotNull ByteBuf buf) {
        ipv4 = buf.readBoolean();
        hostAddress = Packet.readString(buf);
        loopbackAddress = buf.readBoolean();
    }

    public void encode(@NotNull ByteBuf buf) {
        buf.writeBoolean(ipv4);
        Packet.writeString(buf, hostAddress);
        buf.writeBoolean(loopbackAddress);
    }

    @Override
    public String toString() {
        return "InetAddressData{" +
                "ipv4=" + ipv4 +
                ", hostAddress='" + hostAddress + '\'' +
                ", loopbackAddress=" + loopbackAddress +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InetAddressData)) return false;
        InetAddressData that = (InetAddressData) o;
        return ipv4 == that.ipv4 && loopbackAddress == that.loopbackAddress && hostAddress.equals(that.hostAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ipv4, hostAddress, loopbackAddress);
    }

    public boolean isIpv4() {
        return ipv4;
    }

    @NotNull
    public String getHostAddress() {
        return hostAddress;
    }

    public boolean isLoopbackAddress() {
        return loopbackAddress;
    }
}
