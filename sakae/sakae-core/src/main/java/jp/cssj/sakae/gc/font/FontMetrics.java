package jp.cssj.sakae.gc.font;

import java.io.Serializable;

import jp.cssj.sakae.font.FontSource;

/**
 * 実際に割り付けられたフォントの情報です。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: FontMetrics.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public interface FontMetrics extends Serializable {
	/**
	 * フォントのサイズを返します。
	 * 
	 * @return
	 */
	public double getFontSize();

	/**
	 * フォントの高さを返します。
	 * 
	 * @return
	 */
	public double getXHeight();

	/**
	 * フォントのベースラインより上の部分の長さを返します。
	 * 
	 * @return
	 */
	public double getAscent();

	/**
	 * フォントのベースラインより下の部分の長さを返します。
	 * 
	 * @return
	 */
	public double getDescent();

	/**
	 * グリフの進行幅を返します。
	 * 
	 * @param gid
	 * @return
	 */
	public double getAdvance(int gid);

	/**
	 * グリフの横幅を返します。
	 * 
	 * @param gid
	 * @return
	 */
	public double getWidth(int gid);

	/**
	 * スペースの幅を返します。
	 * 
	 * @return
	 */
	public double getSpaceAdvance();

	/**
	 * グリフ間のカーニングを返します。
	 * 
	 * @param gid
	 * @return
	 */
	public double getKerning(int gid, int sgid);

	/**
	 * FontSourceを返します。
	 * 
	 * @return
	 */
	public FontSource getFontSource();
}