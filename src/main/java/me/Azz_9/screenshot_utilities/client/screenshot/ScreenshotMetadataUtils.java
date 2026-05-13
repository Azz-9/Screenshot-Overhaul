package me.Azz_9.screenshot_utilities.client.screenshot;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import net.minecraft.SharedConstants;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.PackResources;
import net.minecraft.world.level.biome.Biome;

import org.jspecify.annotations.NonNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.CRC32;

import me.Azz_9.screenshot_utilities.ScreenshotLogger;
import me.Azz_9.screenshot_utilities.client.config.Config;
import me.Azz_9.screenshot_utilities.compat.CompatManager;
import me.Azz_9.screenshot_utilities.compat.IrisCompat;

public class ScreenshotMetadataUtils {

	private static final @NonNull String KEY_X = "X";
	private static final @NonNull String KEY_Y = "Y";
	private static final @NonNull String KEY_Z = "Z";
	private static final @NonNull String KEY_DIMENSION = "Dimension";
	private static final @NonNull String KEY_BIOME = "Biome";
	private static final @NonNull String KEY_SEED = "Seed";
	private static final @NonNull String KEY_WORLD = "World";
	private static final @NonNull String KEY_SERVER = "Server";
	private static final @NonNull String KEY_VERSION = "Version";
	private static final @NonNull String KEY_RESOURCE_PACKS = "ResourcePacks";
	private static final @NonNull String KEY_SHADER = "Shader";
	private static final @NonNull String KEY_TIMESTAMP = "Timestamp";
	private static final @NonNull String KEY_TAGS = "Tags";

	public static void add(File imageFile, ScreenshotMetadata meta) {
		try {
			byte[] originalBytes = Files.readAllBytes(imageFile.toPath());
			byte[] newBytes = injectIntoBytes(originalBytes, meta);
			Files.write(imageFile.toPath(), newBytes);
		} catch (Exception e) {
			ScreenshotLogger.error("Could not save screenshot metadata to {} : {}",
					Config.getInstance().getScreenshotsDir().relativize(imageFile.toPath()), e.getMessage());
		}
	}

	/**
	 * Construit un chunk PNG tEXt valide :
	 * [4 bytes longueur] [4 bytes type "tEXt"] [données] [4 bytes CRC]
	 */
	private static byte[] buildTextChunk(String keyword, String value) throws Exception {
		byte[] data = (keyword + "\0" + value).getBytes(StandardCharsets.ISO_8859_1);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(out);

		dos.writeInt(data.length);           // Longueur
		dos.write("tEXt".getBytes());        // Type
		dos.write(data);                     // Contenu

		// CRC sur type + contenu
		CRC32 crc = new CRC32();
		crc.update("tEXt".getBytes());
		crc.update(data);
		dos.writeInt((int) crc.getValue());

		return out.toByteArray();
	}

	/**
	 * Injecte les chunks après le IHDR (position 33 dans tout PNG valide)
	 */
	private static byte[] injectChunks(byte[] original, List<byte[]> chunks) throws Exception {
		// Les 8 premiers bytes = signature PNG
		// Bytes 8-32 = chunk IHDR (longueur fixe)
		int insertAt = 33;

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		out.write(original, 0, insertAt);     // Signature + IHDR

		for (byte[] chunk : chunks) {
			out.write(chunk);                 // Nos chunks custom
		}

		out.write(original, insertAt, original.length - insertAt); // Reste du fichier
		return out.toByteArray();
	}

	public static byte[] injectIntoBytes(byte[] originalBytes, ScreenshotMetadata meta) throws Exception {
		if (meta == null) return originalBytes;

		List<byte[]> textChunks = new ArrayList<>();
		if (meta.getX() != null) textChunks.add(buildTextChunk(KEY_X, String.valueOf(meta.getX())));
		if (meta.getY() != null) textChunks.add(buildTextChunk(KEY_Y, String.valueOf(meta.getY())));
		if (meta.getZ() != null) textChunks.add(buildTextChunk(KEY_Z, String.valueOf(meta.getZ())));
		if (meta.getDimension() != null)
			textChunks.add(buildTextChunk(KEY_DIMENSION, meta.getDimension().toString()));
		if (meta.getBiome() != null) textChunks.add(buildTextChunk(KEY_BIOME, meta.getBiome().toString()));
		if (meta.getSeed() != null) textChunks.add(buildTextChunk(KEY_SEED, String.valueOf(meta.getSeed())));
		if (meta.getWorldName() != null) textChunks.add(buildTextChunk(KEY_WORLD, meta.getWorldName()));
		if (meta.getServerIp() != null) textChunks.add(buildTextChunk(KEY_SERVER, meta.getServerIp()));
		if (meta.getServerIp() != null) textChunks.add(buildTextChunk(KEY_VERSION, meta.getVersion()));
		if (meta.getResourcePacks().isEmpty())
			textChunks.add(buildTextChunk(KEY_RESOURCE_PACKS, String.join(",", meta.getResourcePacks())));
		if (meta.getShader() != null) textChunks.add(buildTextChunk(KEY_SHADER, meta.getShader()));
		if (meta.getTimestamp() != null)
			textChunks.add(buildTextChunk(KEY_TIMESTAMP, String.valueOf(meta.getTimestamp())));
		if (!meta.getTags().isEmpty()) textChunks.add(buildTextChunk(KEY_TAGS, String.join(",", meta.getTags())));

		return injectChunks(originalBytes, textChunks);
	}

	public static ScreenshotMetadata read(File imageFile) throws Exception {
		Map<String, String> meta = new LinkedHashMap<>();
		DataInputStream dis = new DataInputStream(new FileInputStream(imageFile));

		dis.skipBytes(8); // Signature PNG

		while (dis.available() > 0) {
			int length = dis.readInt();
			byte[] typeBytes = new byte[4];
			dis.readFully(typeBytes);
			String type = new String(typeBytes);

			byte[] data = new byte[length];
			dis.readFully(data);
			dis.skipBytes(4); // CRC

			if (type.equals("tEXt")) {
				String raw = new String(data, StandardCharsets.ISO_8859_1);
				int sep = raw.indexOf('\0');
				if (sep != -1) {
					meta.put(raw.substring(0, sep), raw.substring(sep + 1));
				}
			}

			if (type.equals("IEND")) break;
		}
		dis.close();

		return new ScreenshotMetadata(
				meta.containsKey(KEY_X) ? Long.parseLong(meta.get(KEY_X)) : null,
				meta.containsKey(KEY_Y) ? Long.parseLong(meta.get(KEY_Y)) : null,
				meta.containsKey(KEY_Z) ? Long.parseLong(meta.get(KEY_Z)) : null,
				Identifier.tryParse(meta.get(KEY_DIMENSION)),
				Identifier.tryParse(meta.get(KEY_BIOME)),
				meta.containsKey(KEY_SEED) ? Long.parseLong(meta.get(KEY_SEED)) : null,
				meta.get(KEY_WORLD),
				meta.get(KEY_SERVER),
				meta.get(KEY_VERSION),
				meta.containsKey(KEY_RESOURCE_PACKS) ? Arrays.asList(meta.get(KEY_RESOURCE_PACKS).split(",")) : List.of(),
				meta.get(KEY_SHADER),
				meta.containsKey(KEY_TIMESTAMP) ? Long.parseLong(meta.get(KEY_TIMESTAMP)) : null,
				meta.containsKey(KEY_TAGS) ? Arrays.asList(meta.get(KEY_TAGS).split(",")) : List.of()
		);
	}

	public static ScreenshotMetadata collect() {
		if (MINECRAFT.player == null || MINECRAFT.level == null)
			return new ScreenshotMetadata(
					null, null, null,
					null, null, null, null, null,
					SharedConstants.getCurrentVersion().name(),
					MINECRAFT.getResourceManager().listPacks().map(PackResources::packId).toList(),
					null, System.currentTimeMillis(), new ArrayList<>()
			);

		IntegratedServer singleplayerServer = MINECRAFT.getSingleplayerServer();

		String serverIp = null;
		String worldName = null;

		if (MINECRAFT.getCurrentServer() != null) {
			serverIp = MINECRAFT.getCurrentServer().ip;
		} else if (singleplayerServer != null) {
			worldName = singleplayerServer.getWorldData().getLevelName();
		}

		ResourceKey<Biome> biomeKey = MINECRAFT.level.getBiome(MINECRAFT.player.getOnPos()).unwrapKey().orElse(null);
		Identifier biomeId = biomeKey == null ? null : biomeKey.identifier();

		Long seed = null;
		if (singleplayerServer != null && singleplayerServer.getAllLevels().iterator().hasNext()) {
			try (ServerLevel serverLevel = singleplayerServer.getAllLevels().iterator().next()) {
				seed = serverLevel.getSeed();
			} catch (Exception ignored) {
			}
		}

		String shader = null;
		if (CompatManager.irisPresent()) {
			shader = IrisCompat.getShaderName();
		}

		return new ScreenshotMetadata(
				(long) Math.floor(MINECRAFT.player.getX()),
				(long) Math.floor(MINECRAFT.player.getY()),
				(long) Math.floor(MINECRAFT.player.getZ()),
				MINECRAFT.level.dimension().identifier(),
				biomeId,
				seed,
				worldName,
				serverIp,
				SharedConstants.getCurrentVersion().name(),
				MINECRAFT.getResourceManager().listPacks().map(PackResources::packId).toList(),
				shader,
				System.currentTimeMillis(),
				new ArrayList<>()
		);
	}

	public static void update(File imageFile, ScreenshotMetadata meta) {
		try {
			byte[] originalBytes = Files.readAllBytes(imageFile.toPath());
			byte[] stripped = stripTextChunks(originalBytes);
			byte[] newBytes = injectIntoBytes(stripped, meta);
			Files.write(imageFile.toPath(), newBytes);
		} catch (Exception e) {
			ScreenshotLogger.error("Could not update screenshot metadata for {} : {}",
					Config.getInstance().getScreenshotsDir().relativize(imageFile.toPath()), e.getMessage());
		}
	}

	/**
	 * Retourne les bytes PNG sans aucun chunk tEXt
	 */
	private static byte[] stripTextChunks(byte[] original) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(original));

		// Signature PNG
		byte[] signature = new byte[8];
		dis.readFully(signature);
		out.write(signature);

		while (dis.available() > 0) {
			int length = dis.readInt();
			byte[] typeBytes = new byte[4];
			dis.readFully(typeBytes);
			String type = new String(typeBytes, StandardCharsets.US_ASCII);

			byte[] data = new byte[length];
			dis.readFully(data);
			byte[] crc = new byte[4];
			dis.readFully(crc);

			if (type.equals("tEXt")) continue; // On saute les chunks existants

			// On réécrit les autres chunks intacts
			DataOutputStream dos = new DataOutputStream(out);
			dos.writeInt(length);
			out.write(typeBytes);
			out.write(data);
			out.write(crc);
		}

		return out.toByteArray();
	}
}
