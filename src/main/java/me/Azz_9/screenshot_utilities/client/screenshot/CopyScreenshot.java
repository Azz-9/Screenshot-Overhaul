package me.Azz_9.screenshot_utilities.client.screenshot;

import net.minecraft.util.Util;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import me.Azz_9.screenshot_utilities.ScreenshotLogger;

public class CopyScreenshot {
	public static void copyToClipboard(@NonNull File imageFile) {
		copyToClipboard(imageFile, null);
	}

	public static void copyToClipboard(@NonNull File imageFile, @Nullable Runnable onCopy) {
		Util.ioPool().execute(() -> {
			try {
				BufferedImage image = ImageIO.read(imageFile);
				Toolkit toolkit = Toolkit.getDefaultToolkit();
				toolkit.getSystemClipboard().setContents(new TransferableImage(image), null);
				if (onCopy != null) onCopy.run();
			} catch (Exception e) {
				ScreenshotLogger.warn("Failed to copy screenshot : {}", e.getMessage());
			}
		});
	}

	record TransferableImage(BufferedImage image) implements Transferable {
		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[]{DataFlavor.imageFlavor};
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return DataFlavor.imageFlavor.equals(flavor);
		}

		@Override
		public @NonNull Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
			if (!isDataFlavorSupported(flavor)) throw new UnsupportedFlavorException(flavor);
			return image;
		}
	}
}