package jp.cssj.homare.style.box.params;

import jp.cssj.sakae.gc.image.Image;

/**
 * 背景画像です。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: BackgroundImage.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public class BackgroundImage {
	/**
	 * 背景を繰り返しません。
	 */
	public static final byte REPEAT_NO = 0;

	/**
	 * 背景を水平方向に繰り返します。
	 */
	public static final byte REPEAT_X = 1;

	/**
	 * 背景を垂直方向に繰り返します。
	 */
	public static final byte REPEAT_Y = 2;

	/**
	 * 背景をタイル状に繰り返します。
	 */
	public static final byte REPEAT = 3;

	/**
	 * 背景の位相をページに合わせます。
	 */
	public static final byte ATTACHMENT_SCROLL = 0;

	/**
	 * 背景の位相をボックスに合わせます。
	 */
	public static final byte ATTACHMENT_FIXED = ATTACHMENT_SCROLL + 1;

	/**
	 * 背景画像です。
	 */
	public final Image image;

	/**
	 * 背景画像の繰り返し方法です。
	 * <p>
	 * REPEAT_X定数を使用してください。
	 * </p>
	 */
	public final byte repeat;

	/**
	 * 背景画像の貼り付け方法です。
	 * <p>
	 * ATTACHMENT_X定数を使用してください。
	 * </p>
	 */
	public final byte attachment;

	/**
	 * 背景画像の位置です。
	 */
	public final Offset position;

	/**
	 * 背景画像の大きさです。
	 */
	public final Dimension size;

	public static BackgroundImage create(Image image, byte repeat, byte attachment, Offset position, Dimension size) {
		return new BackgroundImage(image, repeat, attachment, position, size);
	}

	private BackgroundImage(Image image, byte repeat, byte attachment, Offset position, Dimension size) {
		assert image != null && position != null && size != null;
		this.image = image;
		this.repeat = repeat;
		this.attachment = attachment;
		this.position = position;
		this.size = size;
	}

	public String toString() {
		return super.toString() + "[image=" + this.image + ",repeat=" + this.repeat + ",attachment=" + this.attachment
				+ ",position=" + this.position + ",size=" + this.size + "]";
	}
}
