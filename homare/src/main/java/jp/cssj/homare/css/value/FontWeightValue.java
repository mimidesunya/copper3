package jp.cssj.homare.css.value;

import java.io.Serializable;

import jp.cssj.sakae.gc.font.FontStyle;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: FontWeightValue.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public class FontWeightValue implements Value, Serializable {
	private static final long serialVersionUID = 0;

	public static final short NORMAL = FontStyle.FONT_WEIGHT_400;

	public static final short BOLD = FontStyle.FONT_WEIGHT_700;

	public static final short BOLDER = 1;

	public static final short LIGHTER = 2;

	public static final FontWeightValue W100_VALUE = new FontWeightValue(FontStyle.FONT_WEIGHT_100);

	public static final FontWeightValue W200_VALUE = new FontWeightValue(FontStyle.FONT_WEIGHT_200);

	public static final FontWeightValue W300_VALUE = new FontWeightValue(FontStyle.FONT_WEIGHT_300);

	public static final FontWeightValue W400_VALUE = new FontWeightValue(FontStyle.FONT_WEIGHT_400);

	public static final FontWeightValue W500_VALUE = new FontWeightValue(FontStyle.FONT_WEIGHT_500);

	public static final FontWeightValue W600_VALUE = new FontWeightValue(FontStyle.FONT_WEIGHT_600);

	public static final FontWeightValue W700_VALUE = new FontWeightValue(FontStyle.FONT_WEIGHT_700);

	public static final FontWeightValue W800_VALUE = new FontWeightValue(FontStyle.FONT_WEIGHT_800);

	public static final FontWeightValue W900_VALUE = new FontWeightValue(FontStyle.FONT_WEIGHT_900);

	public static final FontWeightValue NORMAL_VALUE = new FontWeightValue(NORMAL);

	public static final FontWeightValue BOLD_VALUE = new FontWeightValue(BOLD);

	public static final FontWeightValue BOLDER_VALUE = new FontWeightValue(BOLDER);

	public static final FontWeightValue LIGHTER_VALUE = new FontWeightValue(LIGHTER);

	private static final FontWeightValue[] TABLE = { W100_VALUE, W200_VALUE, W300_VALUE, W400_VALUE, W500_VALUE,
			W600_VALUE, W700_VALUE, W800_VALUE, W900_VALUE };

	public static FontWeightValue create(int fontWeight) throws IllegalArgumentException {
		switch (fontWeight) {
		case 100:
			return FontWeightValue.W100_VALUE;
		case 200:
			return FontWeightValue.W200_VALUE;
		case 300:
			return FontWeightValue.W300_VALUE;
		case 400:
			return FontWeightValue.W400_VALUE;
		case 500:
			return FontWeightValue.W500_VALUE;
		case 600:
			return FontWeightValue.W600_VALUE;
		case 700:
			return FontWeightValue.W700_VALUE;
		case 800:
			return FontWeightValue.W800_VALUE;
		case 900:
			return FontWeightValue.W900_VALUE;
		default:
			throw new IllegalArgumentException();
		}
	}

	private final short fontWeight;

	private FontWeightValue(short fontWeight) {
		this.fontWeight = fontWeight;
	}

	public short getValueType() {
		return TYPE_FONT_WEIGHT;
	}

	/**
	 * スタイルコードを返します。
	 * 
	 * @return
	 */
	public short getFontWeight() {
		assert (this.fontWeight != BOLDER && this.fontWeight != LIGHTER);
		return this.fontWeight;
	}

	public FontWeightValue bolder() {
		assert (this.fontWeight != BOLDER && this.fontWeight != LIGHTER);
		return TABLE[Math.min(8, this.fontWeight / 100)];
	}

	public FontWeightValue lighter() {
		assert (this.fontWeight != BOLDER && this.fontWeight != LIGHTER);
		return TABLE[Math.max(0, this.fontWeight / 100 - 2)];
	}

	public String toString() {
		return String.valueOf(this.fontWeight);
	}
}