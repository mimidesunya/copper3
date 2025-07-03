package jp.cssj.homare.css.value;

import jp.cssj.sakae.gc.font.FontStyle;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: FontStyleValue.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public class FontStyleValue implements Value {
	public static final FontStyleValue NORMAL_VALUE = new FontStyleValue(FontStyle.FONT_STYLE_NORMAL);

	public static final FontStyleValue ITALIC_VALUE = new FontStyleValue(FontStyle.FONT_STYLE_ITALIC);

	public static final FontStyleValue OBLIQUE_VALUE = new FontStyleValue(FontStyle.FONT_STYLE_OBLIQUE);

	private final byte fontStyle;

	private FontStyleValue(byte fontStyle) {
		this.fontStyle = fontStyle;
	}

	public short getValueType() {
		return TYPE_FONT_STYLE;
	}

	/**
	 * スタイルコードを返します。
	 * 
	 * @return
	 */
	public byte getFontStyle() {
		return this.fontStyle;
	}

	public String toString() {
		switch (this.fontStyle) {
		case FontStyle.FONT_STYLE_NORMAL:
			return "normal";

		case FontStyle.FONT_STYLE_ITALIC:
			return "italic";

		case FontStyle.FONT_STYLE_OBLIQUE:
			return "oblique";

		default:
			throw new IllegalStateException();
		}
	}
}