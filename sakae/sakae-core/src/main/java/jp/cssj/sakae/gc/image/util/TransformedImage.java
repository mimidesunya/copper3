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
public class TransformedImage extends WrappedImage {
	private final AffineTransform at;

	public TransformedImage(Image image, AffineTransform at) {
		super(image);
		assert image != null;
		assert at != null;
		this.at = at;
	}

	public AffineTransform getTransform() {
		return this.at;
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
		return this.image.getWidth() * this.at.getScaleX();
	}

	public double getHeight() {
		return this.image.getHeight() * this.at.getScaleY();
	}

	public String toString() {
		return super.toString() + "/image=" + this.image + ",at=" + this.at;
	}
}