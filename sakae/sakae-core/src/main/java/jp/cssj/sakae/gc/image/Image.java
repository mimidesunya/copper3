package jp.cssj.sakae.gc.image;

import jp.cssj.sakae.gc.GC;
import jp.cssj.sakae.gc.GraphicsException;

/**
 * 画像です。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: Image.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public interface Image {
	/**
	 * 画像の幅を返します。
	 * 
	 * @return
	 */
	public double getWidth();

	/**
	 * 画像の高さを返します。
	 * 
	 * @return
	 */
	public double getHeight();

	/**
	 * 画像を描画します。
	 * 
	 * @param gc
	 */
	public void drawTo(GC gc) throws GraphicsException;

	/**
	 * 画像に相当する文字列を返します。
	 * 
	 * @return
	 */
	public String getAltString();
}
