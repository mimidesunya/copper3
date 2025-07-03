package jp.cssj.homare.css.value;

import jp.cssj.sakae.gc.font.FontFamily;
import jp.cssj.sakae.gc.font.FontFamilyList;

/**
 * フォントファミリーです。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: FontFamilyValue.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public class FontFamilyValue extends FontFamilyList implements Value {
	private static final long serialVersionUID = 0L;

	public static final FontFamilyValue SERIF = new FontFamilyValue(FontFamily.SERIF_VALUE);

	public static final FontFamilyValue SANS_SERIF = new FontFamilyValue(FontFamily.SANS_SERIF_VALUE);

	public static final FontFamilyValue CURSIVE = new FontFamilyValue(FontFamily.CURSIVE_VALUE);

	public static final FontFamilyValue FANTASY = new FontFamilyValue(FontFamily.FANTASY_VALUE);

	public static final FontFamilyValue MONOSPACE = new FontFamilyValue(FontFamily.MONOSPACE_VALUE);

	public FontFamilyValue(FontFamily[] entries) {
		super(entries);
	}

	public FontFamilyValue(FontFamily f1) {
		super(f1);
	}

	public short getValueType() {
		return TYPE_FONT_FAMILY;
	}
}