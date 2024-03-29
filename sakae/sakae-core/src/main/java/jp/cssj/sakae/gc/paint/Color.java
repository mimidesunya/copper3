package jp.cssj.sakae.gc.paint;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: Color.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public interface Color extends Paint {
	public static final short RGB = 1;

	public static final short CMYK = 2;

	public static final short GRAY = 3;

	public static final short RGBA = 4;

	public short getColorType();

	public float getRed();

	public float getGreen();

	public float getBlue();

	public float getAlpha();

	public float getComponent(int i);
}