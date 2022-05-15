package net.azisaba.autoreboot.common.network.protocol;

import io.netty.buffer.ByteBuf;
import net.azisaba.autoreboot.common.network.BackendPacketListener;
import net.azisaba.autoreboot.common.network.Packet;
import net.azisaba.autoreboot.common.network.data.InetAddressData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BackendboundRebootRequestPacket extends Packet<BackendPacketListener> {
    private final List<InetAddressData> ipAddressList = new ArrayList<>();
    private byte[] secret;

    public BackendboundRebootRequestPacket() {
    }

    public BackendboundRebootRequestPacket(byte @NotNull [] secret, @NotNull List<InetAddressData> ipAddressList) {
        this.secret = secret;
        this.ipAddressList.addAll(ipAddressList);
    }

    @Override
    public void decode(@NotNull ByteBuf buf) {
        secret = readByteArray(buf);
        ipAddressList.addAll(readList(buf, InetAddressData::new));
    }

    @Override
    public void encode(@NotNull ByteBuf buf) {
        writeByteArray(buf, secret);
        writeList(buf, ipAddressList, (b, t) -> t.encode(b));
    }

    @Override
    public void handle(@NotNull BackendPacketListener packetListener) {
        packetListener.handle(this);
    }

    public byte @NotNull [] getSecret() {
        return secret;
    }

    @NotNull
    public List<@NotNull InetAddressData> getIpAddressList() {
        return ipAddressList;
    }
}
