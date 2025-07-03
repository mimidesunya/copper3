package jp.cssj.sakae.gc.image.util;

import java.awt.geom.AffineTransform;

import jp.cssj.sakae.gc.GC;
import jp.cssj.sakae.gc.image.Image;
import jp.cssj.sakae.gc.image.WrappedImage;

/**
 * UAの単位にスケールされた画像です。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: TransformedImage.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public class CenteredImage extends WrappedImage {
	private final double width, height;
	private final AffineTransform at;

	public CenteredImage(Image image, double width, double height) {
		super(image);
		assert image != null;
		this.width = width;
		this.height = height;
		double s = width / image.getWidth();
		double tx = 0, ty = 0;
		if (image.getHeight() * s > height) {
			s = height / image.getHeight();
			tx = (width - image.getWidth() * s) / 2;
		}
		else {
			ty = (height - image.getHeight() * s) / 2;
		}
		this.at = new AffineTransform(s, 0, 0, s, tx, ty);
	}

	public void drawTo(GC gc) {
		gc.begin();
		gc.transform(this.at);
		this.image.drawTo(gc);
		gc.end();
	}

	public String getAltString() {
		return this.image.getAltString();
	}

	public double getWidth() {
		return this.width;
	}

	public double getHeight() {
		return this.height;
	}

	public String toString() {
		return super.toString() + "/image=" + this.image + ",width=" + this.width + ",height=" + this.height;
	}
}