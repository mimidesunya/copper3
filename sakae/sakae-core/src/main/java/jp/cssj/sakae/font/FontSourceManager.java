package jp.cssj.sakae.font;

import jp.cssj.sakae.gc.font.FontStyle;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: PDFFontSourceManager.java,v 1.1 2007-05-06 15:37:19 miyabe Exp
 *          $
 */
public interface FontSourceManager {
	/**
	 * フォントスタイルにマッチするフォントを返します。
	 * 
	 * @param fontStyle
	 *            nullの場合は全てのフォントを返します。
	 * @return fontSourceの配列
	 */
	public FontSource[] lookup(FontStyle fontStyle);
}