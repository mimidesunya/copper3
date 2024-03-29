package jp.cssj.sakae.font;

import java.io.IOException;
import java.io.Serializable;

import jp.cssj.sakae.gc.GC;
import jp.cssj.sakae.gc.GraphicsException;
import jp.cssj.sakae.gc.text.Text;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: Font.java 1601 2020-04-18 03:42:26Z miyabe $
 */
public interface Font extends Serializable {
	/**
	 * フォントソースを返します。
	 * 
	 * @return
	 */
	public FontSource getFontSource();

	/**
	 * 文字をグリフコードに変換します。
	 * 
	 * @param c
	 * @return
	 */
	public int toGID(int c);

	/**
	 * グリフの進行方向の進行幅を返します。
	 * 
	 * @param gid
	 * @return
	 */
	public short getAdvance(int gid);

	/**
	 * グリフの進行方向の横幅を返します。
	 * 
	 * @param gid
	 * @return
	 */
	public short getWidth(int gid);

	/**
	 * グリフのカーニングを返します。
	 * 
	 * @param sgid
	 * @param gid
	 * @return
	 */
	public short getKerning(int sgid, int gid);

	/**
	 * 合字を返します。 合字が存在しない場合は0を返します。
	 * 
	 * @param gid
	 * @param cid
	 * @return
	 */
	public int getLigature(int gid, int cid);

	/**
	 * ランをグラフィックコンテキストに出力します。
	 * 
	 * @param gc
	 * @param text
	 * @throws IOException
	 */
	public void drawTo(GC gc, Text text) throws IOException, GraphicsException;
}
