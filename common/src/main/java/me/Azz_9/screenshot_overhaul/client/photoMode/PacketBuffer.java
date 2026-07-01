package me.Azz_9.screenshot_overhaul.client.photoMode;

import static me.Azz_9.screenshot_overhaul.CommonClass.MINECRAFT;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

import me.Azz_9.screenshot_overhaul.ScreenshotLogger;

public class PacketBuffer {
	private static final @NonNull List<Packet<ClientGamePacketListener>> packets = new ArrayList<>();

	public synchronized static void addPacket(@NonNull Packet<ClientGamePacketListener> packet) {
		packets.add(packet);
	}

	public synchronized static void applyAndClear() {
		ClientPacketListener packetListener = MINECRAFT.getConnection();
		if (packetListener == null) return;

		for (Packet<ClientGamePacketListener> packet : packets) {
			switch (packet) {
				case ClientboundBlockUpdatePacket clientboundBlockUpdatePacket ->
						packetListener.handleBlockUpdate(clientboundBlockUpdatePacket);
				case ClientboundBlockEventPacket clientboundBlockEventPacket ->
						packetListener.handleBlockEvent(clientboundBlockEventPacket);
				case ClientboundBlockDestructionPacket clientboundBlockDestructionPacket ->
						packetListener.handleBlockDestruction(clientboundBlockDestructionPacket);
				case ClientboundSectionBlocksUpdatePacket clientboundSectionBlocksUpdatePacket ->
						packetListener.handleChunkBlocksUpdate(clientboundSectionBlocksUpdatePacket);
				default -> ScreenshotLogger.warn("Unknown buffered packet type : " + packet.getClass().getName());
			}
		}

		packets.clear();
	}
}
