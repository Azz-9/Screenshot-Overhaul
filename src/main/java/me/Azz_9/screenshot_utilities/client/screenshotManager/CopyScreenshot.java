package me.Azz_9.screenshot_utilities.client.screenshotManager;

import org.jspecify.annotations.NonNull;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class CopyScreenshot {
	public static void copyToClipboard(@NonNull BufferedImage image) {
		Transferable transferableImage = new Transferable() {
			@Override
			public DataFlavor[] getTransferDataFlavors() {
				return new DataFlavor[]{DataFlavor.imageFlavor};
			}

			@Override
			public boolean isDataFlavorSupported(DataFlavor flavor) {
				return flavor == DataFlavor.imageFlavor;
			}

			@Override
			public @NonNull Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
				if (!isDataFlavorSupported(flavor)) {
					throw new UnsupportedFlavorException(flavor);
				}
				return image;
			}
		};

		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(transferableImage, null);
	}
}
