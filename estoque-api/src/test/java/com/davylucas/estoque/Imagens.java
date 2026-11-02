package com.davylucas.estoque;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

final class Imagens {

	private Imagens() {
	}

	/** PNG real 4x4. */
	static byte[] png() throws Exception {
		BufferedImage img = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(img, "png", out);
		return out.toByteArray();
	}
}
