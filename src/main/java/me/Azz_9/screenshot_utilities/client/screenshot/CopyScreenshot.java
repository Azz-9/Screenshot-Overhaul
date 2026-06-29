package me.Azz_9.screenshot_utilities.client.screenshot;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
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
import me.Azz_9.screenshot_utilities.client.gui.components.toasts.CustomToastId;

public class CopyScreenshot {
	public static void copyToClipboardWithToastError(@NonNull File imageFile, @Nullable Runnable onCopy, @Nullable Runnable onError) {
		copyToClipboard(imageFile, onCopy, () -> {
			if (onError != null) onError.run();

			MINECRAFT.execute(() ->
					SystemToast.add(
							MINECRAFT.getToastManager(),
							CustomToastId.SCREENSHOT_COPY_FAILED,
							net.minecraft.network.chat.Component.translatable("screenshot_utilities.copy_failed.title"),
							Component.translatable("screenshot_utilities.copy_failed.message", imageFile.getName())
					)
			);
		});
	}

	public static void copyToClipboard(@NonNull File imageFile, @Nullable Runnable onCopy, @Nullable Runnable onError) {
		Util.ioPool().execute(() -> {
			try {
				BufferedImage image = ImageIO.read(imageFile);
				Toolkit toolkit = Toolkit.getDefaultToolkit();
				toolkit.getSystemClipboard().setContents(new TransferableImage(image), null);
				if (onCopy != null) onCopy.run();
			} catch (Exception e) {
				ScreenshotLogger.warn("Failed to copy screenshot : {}", e.getMessage());
				if (onError != null) onError.run();
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