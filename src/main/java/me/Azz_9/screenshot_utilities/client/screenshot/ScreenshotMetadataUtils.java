package me.Azz_9.screenshot_utilities.client.screenshot;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;

import org.jspecify.annotations.NonNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.CRC32;

public class ScreenshotMetadataUtils {
	
	private static final @NonNull String KEY_X = "X";
	private static final @NonNull String KEY_Y = "Y";
	private static final @NonNull String KEY_Z = "Z";
	private static final @NonNull String KEY_DIMENSION = "Dimension";
	private static final @NonNull String KEY_BIOME = "Biome";
	private static final @NonNull String KEY_WORLD = "World";
	private static final @NonNull String KEY_SERVER = "Server";
	private static final @NonNull String KEY_TIMESTAMP = "Timestamp";
	private static final @NonNull String KEY_TAGS = "Tags";

	public static void add(File imageFile, ScreenshotMetadata meta) {
		try {
			if (meta == null) return;

			// Lire l'image originale en bytes bruts
			byte[] originalBytes = Files.readAllBytes(imageFile.toPath());

			// Construire les chunks tEXt à injecter
			List<byte[]> textChunks = new ArrayList<>();
			textChunks.add(buildTextChunk(KEY_X, String.valueOf(meta.x())));
			textChunks.add(buildTextChunk(KEY_Y, String.valueOf(meta.y())));
			textChunks.add(buildTextChunk(KEY_Z, String.valueOf(meta.z())));
			textChunks.add(buildTextChunk(KEY_DIMENSION, meta.dimension()));
			textChunks.add(buildTextChunk(KEY_BIOME, meta.biome()));
			textChunks.add(buildTextChunk(KEY_WORLD, meta.worldName() != null ? meta.worldName() : ""));
			textChunks.add(buildTextChunk(KEY_SERVER, meta.server() != null ? meta.server() : "singleplayer"));
			textChunks.add(buildTextChunk(KEY_TIMESTAMP, String.valueOf(meta.timestamp())));
			textChunks.add(buildTextChunk(KEY_TAGS, String.valueOf(meta.timestamp())));

			// Insérer les chunks juste après le chunk IHDR (offset 33)
			byte[] newBytes = injectChunks(originalBytes, textChunks);
			Files.write(imageFile.toPath(), newBytes);

		} catch (Exception e) {
			e.printStackTrace();
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
				meta.containsKey(KEY_X) ? Integer.parseInt(meta.get("X")) : null,
				meta.containsKey(KEY_Y) ? Integer.parseInt(meta.get("Y")) : null,
				meta.containsKey(KEY_Z) ? Integer.parseInt(meta.get("Z")) : null,
				meta.get(KEY_DIMENSION),
				meta.get(KEY_BIOME),
				meta.get(KEY_WORLD),
				meta.get(KEY_SERVER),
				meta.containsKey(KEY_TIMESTAMP) ? Long.parseLong(meta.get("Timestamp")) : null,
				meta.containsKey(KEY_TAGS) ? Arrays.asList(meta.get("Tags").split(",")) : List.of()
		);
	}

	public static ScreenshotMetadata collect() {
		if (MINECRAFT.player == null || MINECRAFT.level == null)
			return new ScreenshotMetadata(
				null, null, null, 
				null, null, null, null,
				System.currentTimeMillis(), new ArrayList<>()
		);

		String server = null;
		String worldName = null;

		if (MINECRAFT.getCurrentServer() != null) {
			server = MINECRAFT.getCurrentServer().ip;
		} else if (MINECRAFT.getSingleplayerServer() != null) {
			worldName = MINECRAFT.getSingleplayerServer().getWorldData().getLevelName();
		}
		
		String biome = null;
		ResourceKey<Biome> biomeKey = MINECRAFT.level.getBiome(MINECRAFT.player.getOnPos()).unwrapKey().orElse(null);
		if (biomeKey != null) {
			biome = biomeKey.toString();
		}

		return new ScreenshotMetadata(
				Mth.floor(MINECRAFT.player.getX()),
				Mth.floor(MINECRAFT.player.getY()),
				Mth.floor(MINECRAFT.player.getZ()),
				MINECRAFT.level.dimension().toString(),
				biome,
				worldName,
				server,
				System.currentTimeMillis(),
				new ArrayList<>()
		);
	}
}
