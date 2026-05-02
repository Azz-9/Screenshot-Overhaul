package me.Azz_9.screenshot_utilities.client.screenshot;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.CRC32;

public class ScreenshotMetadataUtils {

	public static void addMetadata(File imageFile, ScreenshotMetadata meta) {
		try {
			if (meta == null) return;

			// Lire l'image originale en bytes bruts
			byte[] originalBytes = Files.readAllBytes(imageFile.toPath());

			// Construire les chunks tEXt à injecter
			List<byte[]> textChunks = new ArrayList<>();
			textChunks.add(buildTextChunk("X", String.valueOf(meta.x())));
			textChunks.add(buildTextChunk("Y", String.valueOf(meta.y())));
			textChunks.add(buildTextChunk("Z", String.valueOf(meta.z())));
			textChunks.add(buildTextChunk("Dimension", meta.dimension()));
			textChunks.add(buildTextChunk("Server", meta.server() != null ? meta.server() : "singleplayer"));
			textChunks.add(buildTextChunk("World", meta.worldName() != null ? meta.worldName() : ""));
			textChunks.add(buildTextChunk("Timestamp", String.valueOf(meta.timestamp())));

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

	public static ScreenshotMetadata readMetadata(File imageFile) throws Exception {
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
				meta.containsKey("X") ? Integer.parseInt(meta.get("X")) : null,
				meta.containsKey("Y") ? Integer.parseInt(meta.get("Y")) : null,
				meta.containsKey("Z") ? Integer.parseInt(meta.get("Z")) : null,
				meta.get("Dimension"),
				meta.get("World"),
				meta.get("Server"),
				meta.containsKey("Timestamp") ? Long.parseLong(meta.get("Timestamp")) : null,
				meta.containsKey("Tags") ? Arrays.asList(meta.get("Tags").split(",")) : List.of()
		);
	}
}
