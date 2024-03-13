package jp.cssj.sakae.gc.image;

/**
 * 画像を内包する画像です。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: Image.java 1034 2013-10-23 05:51:57Z miyabe $
 */
public abstract class WrappedImage implements Image {
	protected final Image image;

	public WrappedImage(Image image) {
		this.image = image;
	}

	public Image getRootImage() {
		if (this.image instanceof WrappedImage) {
			return ((WrappedImage)this.image).getRootImage();
		}
		return this.image;
	}
}
