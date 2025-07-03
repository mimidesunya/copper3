package jp.cssj.sakae.g2d.image;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;

import jp.cssj.sakae.g2d.gc.G2dGC;
import jp.cssj.sakae.gc.GC;
import jp.cssj.sakae.gc.GraphicsException;
import jp.cssj.sakae.gc.image.Image;
import jp.cssj.sakae.pdf.gc.PdfGC;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: RasterImageImpl.java 1633 2023-02-12 03:22:32Z miyabe $
 */

public class RasterImageImpl implements RasterImage, ImageObserver {
	private BufferedImage image;

	private final String altString;

	private int width = -1, height = -1;

	public RasterImageImpl(BufferedImage image, String altString) {
		if (image == null) {
			throw new NullPointerException();
		}
		this.image = image;
		this.altString = altString;
	}

	public RasterImageImpl(BufferedImage image) {
		this(image, null);
	}

	public BufferedImage getImage() {
		return this.image;
	}

	public synchronized double getWidth() {
		if (this.width == -1) {
			this.width = this.image.getWidth(this);
			while (this.width == -1) {
				try {
					this.wait(1000);
				} catch (InterruptedException e) {
					return this.width = 0;
				}
			}
		}
		return this.width;
	}

	public synchronized double getHeight() {
		if (this.height == -1) {
			this.height = this.image.getHeight(this);
			while (this.height == -1) {
				try {
					this.wait(1000);
				} catch (InterruptedException e) {
					return this.height = 0;
				}
			}
		}
		return this.height;
	}

	public String getAltString() {
		return this.altString;
	}

	public void drawTo(GC gc) throws GraphicsException {
		if (gc instanceof PdfGC) {
			try {
				Image image = ((PdfGC) gc).getPDFGraphicsOutput().getPdfWriter().addImage(this.image);
				gc.drawImage(image);
			} catch (IOException e) {
				throw new GraphicsException(e);
			}
		} else {
			java.awt.Image image = this.image;
			Graphics2D g2d = ((G2dGC) gc).getGraphics2D();
			AffineTransform at = g2d.getTransform();
			g2d.drawImage(image, null, null);
			if (image != this.image) {
				g2d.setTransform(at);
				image.flush();
			}
		}
	}

	public synchronized boolean imageUpdate(java.awt.Image img, int infoflags, int x, int y, int width, int height) {
		if ((infoflags & (ERROR | ABORT)) != 0) {
			this.width = this.height = 0;
		} else {
			if ((infoflags & WIDTH) != 0) {
				this.width = width;
			}
			if ((infoflags & HEIGHT) != 0) {
				this.height = height;
			}
		}
		this.notifyAll();
		return (this.width == -1 || this.height == -1);
	}
	
	public boolean isRasterImage() {
		return true;
	}

	public synchronized void dispose() {
		if (this.image != null) {
			this.image.flush();
			this.image = null;
		}
	}

	protected void finalize() throws Throwable {
		super.finalize();
		this.dispose();
	}
}