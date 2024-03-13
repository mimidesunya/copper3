package jp.cssj.homare.css.util;

import java.util.ArrayList;
import java.util.List;

import jp.cssj.homare.css.value.AbsoluteLengthValue;
import jp.cssj.homare.css.value.EmLengthValue;
import jp.cssj.homare.css.value.ExLengthValue;
import jp.cssj.homare.css.value.FontFamilyValue;
import jp.cssj.homare.css.value.FontStyleValue;
import jp.cssj.homare.css.value.FontVariantValue;
import jp.cssj.homare.css.value.FontWeightValue;
import jp.cssj.homare.css.value.LengthValue;
import jp.cssj.homare.css.value.PercentageValue;
import jp.cssj.homare.css.value.RelativeSizeValue;
import jp.cssj.homare.css.value.Value;
import jp.cssj.homare.css.value.css3.ChLengthValue;
import jp.cssj.homare.css.value.css3.RemLengthValue;
import jp.cssj.homare.css.value.ext.CSSJFontPolicyValue;
import jp.cssj.homare.style.util.ByteList;
import jp.cssj.homare.ua.UserAgent;
import jp.cssj.sakae.gc.font.FontFamily;
import jp.cssj.sakae.gc.font.FontPolicyList;
import jp.cssj.sakae.sac.css.LexicalUnit;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: FontValueUtils.java 1554 2018-04-26 03:34:02Z miyabe $
 */
public final class FontValueUtils {
	private FontValueUtils() {
		// unused
	}

	/**
	 * font-familyを値に変換します。SPEC CSS2.1 15.3
	 * 
	 * @param lu
	 * @return
	 */
	public static FontFamilyValue toFontFamily(UserAgent ua, LexicalUnit lu) {
		List<FontFamily> list = new ArrayList<FontFamily>();
		do {
			switch (lu.getLexicalUnitType()) {
			case LexicalUnit.SAC_IDENT:
				String ident = lu.getStringValue().toLowerCase();
				if (ident.equalsIgnoreCase("cursive")) {
					list.add(FontFamily.CURSIVE_VALUE);
					break;
				} else if (ident.equalsIgnoreCase("fantasy")) {
					list.add(FontFamily.FANTASY_VALUE);
					break;
				} else if (ident.equalsIgnoreCase("monospace")) {
					list.add(FontFamily.MONOSPACE_VALUE);
					break;
				} else if (ident.equalsIgnoreCase("sans-serif")) {
					list.add(FontFamily.SANS_SERIF_VALUE);
					break;
				} else if (ident.equalsIgnoreCase("serif")) {
					list.add(FontFamily.SERIF_VALUE);
					break;
				}

			case LexicalUnit.SAC_STRING_VALUE:
				list.add(new FontFamily(lu.getStringValue()));
				break;

			default:
				break;
			}
			while ((lu = lu.getNextLexicalUnit()) != null
					&& lu.getLexicalUnitType() == LexicalUnit.SAC_OPERATOR_COMMA) {
				// do nothing
			}
		} while (lu != null);
		if (list.isEmpty()) {
			return null;
		}
		FontFamilyValue defaultFamily = ua.getDefaultFontFamily();
		for (int i = 0; i < defaultFamily.getLength(); ++i) {
			list.add(defaultFamily.get(i));
		}
		return new FontFamilyValue((FontFamily[]) list.toArray(new FontFamily[list.size()]));
	}

	/**
	 * font-styleを値に変換します。SPEC CSS2.1 15.4
	 * 
	 * @param lu
	 * @return
	 */
	public static FontStyleValue toFontStyle(LexicalUnit lu) {
		short luType = lu.getLexicalUnitType();
		if (luType != LexicalUnit.SAC_IDENT) {
			return null;
		}
		String ident = lu.getStringValue().toLowerCase();
		if (ident.equals("normal")) {
			return FontStyleValue.NORMAL_VALUE;
		} else if (ident.equals("italic")) {
			return FontStyleValue.ITALIC_VALUE;
		} else if (ident.equals("oblique")) {
			return FontStyleValue.OBLIQUE_VALUE;
		}
		return null;
	}

	/**
	 * -cssj-font-policyを値に変換します。
	 * 
	 * @param lu
	 * @return
	 */
	public static CSSJFontPolicyValue toFontPolicy(LexicalUnit lu) {
		short luType = lu.getLexicalUnitType();
		if (luType != LexicalUnit.SAC_IDENT) {
			return null;
		}
		if (lu.getNextLexicalUnit() == null) {
			String ident = lu.getStringValue().toLowerCase();
			if (ident.equals("generic") || ident.equals("cid-keyed")) {
				return CSSJFontPolicyValue.CORE_CID_KEYED_VALUE;
			} else if (ident.equals("external") || ident.equals("cid-identity")) {
				return CSSJFontPolicyValue.CORE_CID_IDENTITY_VALUE;
			} else if (ident.equals("embed") || ident.equals("embedded")) {
				return CSSJFontPolicyValue.CORE_EMBEDDED_VALUE;
			} else if (ident.equals("outlines")) {
				return CSSJFontPolicyValue.OUTLINES_VALUE;
			} else {
				return null;
			}
		}
		ByteList codes = new ByteList();
		codes.add(FontPolicyList.FONT_POLICY_CORE);
		for (;;) {
			String ident = lu.getStringValue().toLowerCase();
			if (ident.equals("generic") || ident.equals("cid-keyed")) {
				codes.add(FontPolicyList.FONT_POLICY_CID_KEYED);
			} else if (ident.equals("external") || ident.equals("cid-identity")) {
				codes.add(FontPolicyList.FONT_POLICY_CID_IDENTITY);
			} else if (ident.equals("embed") || ident.equals("embedded")) {
				codes.add(FontPolicyList.FONT_POLICY_EMBEDDED);
			} else if (ident.equals("-core")) {
				codes.remove(FontPolicyList.FONT_POLICY_CORE);
			} else if (ident.equals("core")) {
				codes.add(FontPolicyList.FONT_POLICY_CORE);
			} else if (ident.equals("outlines")) {
				codes.add(FontPolicyList.FONT_POLICY_OUTLINES);
			} else {
				return null;
			}
			lu = lu.getNextLexicalUnit();
			if (luType != LexicalUnit.SAC_IDENT) {
				return null;
			}
			if (lu == null) {
				break;
			}
		}
		return new CSSJFontPolicyValue(codes.toArray());
	}

	public static CSSJFontPolicyValue toFontPolicy(String str) {
		String[] types = str.split("[\\s]+");
		if (types.length == 1) {
			String ident = types[0].toLowerCase();
			if (ident.equals("generic") || ident.equals("cid-keyed")) {
				return CSSJFontPolicyValue.CORE_CID_KEYED_VALUE;
			} else if (ident.equals("external") || ident.equals("cid-identity")) {
				return CSSJFontPolicyValue.CORE_CID_IDENTITY_VALUE;
			} else if (ident.equals("embed") || ident.equals("embedded")) {
				return CSSJFontPolicyValue.CORE_EMBEDDED_VALUE;
			} else if (ident.equals("outlines")) {
				return CSSJFontPolicyValue.OUTLINES_VALUE;
			} else {
				return null;
			}
		}
		ByteList codes = new ByteList();
		codes.add(FontPolicyList.FONT_POLICY_CORE);
		for (int i = 0; i < types.length; ++i) {
			String ident = types[i].toLowerCase();
			if (ident.equals("generic") || ident.equals("cid-keyed")) {
				codes.add(FontPolicyList.FONT_POLICY_CID_KEYED);
			} else if (ident.equals("external") || ident.equals("cid-identity")) {
				codes.add(FontPolicyList.FONT_POLICY_CID_IDENTITY);
			} else if (ident.equals("embed") || ident.equals("embedded")) {
				codes.add(FontPolicyList.FONT_POLICY_EMBEDDED);
			} else if (ident.equals("-core")) {
				codes.remove(FontPolicyList.FONT_POLICY_CORE);
			} else if (ident.equals("core")) {
				codes.add(FontPolicyList.FONT_POLICY_CORE);
			} else if (ident.equals("outlines")) {
				codes.add(FontPolicyList.FONT_POLICY_OUTLINES);
			} else {
				return null;
			}
		}
		CSSJFontPolicyValue result = new CSSJFontPolicyValue(codes.toArray());
		// System.err.println(str+"/"+result);
		return result;
	}

	public static CSSJFontPolicyValue toFontPolicyA1(String str) {
		String[] types = str.split("[\\s]+");
		if (types.length == 1) {
			String ident = types[0].toLowerCase();
			if (ident.equals("embed") || ident.equals("embedded")) {
				return CSSJFontPolicyValue.PDFA1_VALUE;
			} else if (ident.equals("outlines")) {
				return CSSJFontPolicyValue.OUTLINES_VALUE;
			} else {
				return null;
			}
		}
		ByteList codes = new ByteList();
		codes.add(FontPolicyList.FONT_POLICY_CORE);
		for (int i = 0; i < types.length; ++i) {
			String ident = types[i].toLowerCase();
			if (ident.equals("embed") || ident.equals("embedded")) {
				codes.add(FontPolicyList.FONT_POLICY_EMBEDDED);
			} else if (ident.equals("outlines")) {
				codes.add(FontPolicyList.FONT_POLICY_OUTLINES);
			} else {
				return null;
			}
		}
		CSSJFontPolicyValue result = new CSSJFontPolicyValue(codes.toArray());
		// System.err.println(str+"/"+result);
		return result;
	}

	/**
	 * font-variantを値に変換します。SPEC CSS2.1 15.5
	 * 
	 * @param lu
	 * @return
	 */
	public static FontVariantValue toFontVariant(LexicalUnit lu) {
		short luType = lu.getLexicalUnitType();
		if (luType != LexicalUnit.SAC_IDENT) {
			return null;
		}
		String ident = lu.getStringValue().toLowerCase();
		if (ident.equals("normal")) {
			return FontVariantValue.NORMAL_VALUE;
		} else if (ident.equals("small-caps")) {
			return FontVariantValue.SMALL_CAPS_VALUE;
		}
		return null;
	}

	/**
	 * font-weightを値に変換します。SPEC CSS2.1 15.6
	 * 
	 * @param lu
	 * @return
	 */
	public static FontWeightValue toFontWeight(LexicalUnit lu) {
		short luType = lu.getLexicalUnitType();
		switch (luType) {
		case LexicalUnit.SAC_IDENT:
			String ident = lu.getStringValue().toLowerCase();
			if (ident.equals("normal")) {
				return FontWeightValue.NORMAL_VALUE;
			} else if (ident.equals("bold")) {
				return FontWeightValue.BOLD_VALUE;
			} else if (ident.equals("bolder")) {
				return FontWeightValue.BOLDER_VALUE;
			} else if (ident.equals("lighter")) {
				return FontWeightValue.LIGHTER_VALUE;
			}
			break;

		case LexicalUnit.SAC_INTEGER:
			int a = lu.getIntegerValue();
			try {
				return FontWeightValue.create(a);
			} catch (IllegalArgumentException e) {
				return null;
			}
		}
		return null;
	}

	/**
	 * フォントサイズ値に変換します。
	 * 
	 * @param lu
	 * @return
	 */
	public static Value toFontSize(UserAgent ua, LexicalUnit lu) {
		switch (lu.getLexicalUnitType()) {
		case LexicalUnit.SAC_IDENT:
			String ident = lu.getStringValue().toLowerCase();
			if (ident.equals("larger")) {
				return RelativeSizeValue.LARGER_VALUE;
			} else if (ident.equals("smaller")) {
				return RelativeSizeValue.SMALLER_VALUE;
			} else if (ident.equals("xx-small")) {
				return AbsoluteLengthValue.create(ua, ua.getFontSize(UserAgent.FONT_SIZE_XX_SMALL));
			} else if (ident.equals("x-small")) {
				return AbsoluteLengthValue.create(ua, ua.getFontSize(UserAgent.FONT_SIZE_X_SMALL));
			} else if (ident.equals("small")) {
				return AbsoluteLengthValue.create(ua, ua.getFontSize(UserAgent.FONT_SIZE_SMALL));
			} else if (ident.equals("medium")) {
				return AbsoluteLengthValue.create(ua, ua.getFontSize(UserAgent.FONT_SIZE_MEDIUM));
			} else if (ident.equals("large")) {
				return AbsoluteLengthValue.create(ua, ua.getFontSize(UserAgent.FONT_SIZE_LARGE));
			} else if (ident.equals("x-large")) {
				return AbsoluteLengthValue.create(ua, ua.getFontSize(UserAgent.FONT_SIZE_X_LARGE));
			} else if (ident.equals("xx-large")) {
				return AbsoluteLengthValue.create(ua, ua.getFontSize(UserAgent.FONT_SIZE_XX_LARGE));
			}
			return null;

		case LexicalUnit.SAC_PERCENTAGE:
			PercentageValue per = ValueUtils.toPercentage(lu);
			if (per != null && per.isNegative()) {
				return null;
			}
			return per;

		default:
			short luType = lu.getLexicalUnitType();
			switch (luType) {
			case LexicalUnit.SAC_EM:
				return EmLengthValue.create(lu.getFloatValue());

			case LexicalUnit.SAC_EX:
				return ExLengthValue.create(lu.getFloatValue());

			case LexicalUnit.SAC_REM:
				return RemLengthValue.create(lu.getFloatValue());

			case LexicalUnit.SAC_CH:
				return ChLengthValue.create(lu.getFloatValue());

			case LexicalUnit.SAC_INCH:
			case LexicalUnit.SAC_CENTIMETER:
			case LexicalUnit.SAC_MILLIMETER:
			case LexicalUnit.SAC_POINT:
			case LexicalUnit.SAC_PICA:
			case LexicalUnit.SAC_PIXEL:
				return AbsoluteLengthValue.create(ua, lu.getFloatValue() * ua.getFontMagnification(), luType);

			case LexicalUnit.SAC_INTEGER: {
				int val = lu.getIntegerValue();
				if (val == 0) {
					return AbsoluteLengthValue.create(ua, val * ua.getFontMagnification(), LengthValue.UNIT_PX);
				}
				return null;
			}

			case LexicalUnit.SAC_REAL: {
				double val = lu.getFloatValue();
				if (val == 0) {
					return AbsoluteLengthValue.create(ua, val * ua.getFontMagnification(), LengthValue.UNIT_PX);
				}
				return null;
			}

			default:
				return null;
			}
		}
	}

	/**
	 * font-familyを値に変換します。
	 * 
	 * @param str
	 * @return
	 */
	public static FontFamilyValue toFontFamily(String str) {
		List<FontFamily> list = new ArrayList<FontFamily>();
		int state = 0;
		StringBuffer buff = new StringBuffer();
		for (int i = 0; i <= str.length(); ++i) {
			String ident;
			if (i < str.length()) {
				char c = str.charAt(i);
				switch (state) {
				case 0:
					if (Character.isWhitespace(c)) {
						continue;
					}
					if (c == '\'') {
						state = 1;
						continue;
					}
					if (c == '\"') {
						state = 2;
						continue;
					}
					buff.append(c);
					state = 3;
					continue;
				case 1:
					if (c == '\'') {
						ident = buff.toString().trim();
						buff = new StringBuffer();
						state = 0;
						break;
					}
					buff.append(c);
					continue;
				case 2:
					if (c == '\"') {
						ident = buff.toString().trim();
						buff = new StringBuffer();
						state = 0;
						break;
					}
					buff.append(c);
					continue;
				case 3:
					if (Character.isWhitespace(c)) {
						ident = buff.toString().trim();
						buff = new StringBuffer();
						state = 0;
						break;
					}
					buff.append(c);
					continue;
				default:
					throw new IllegalStateException();
				}
			} else {
				ident = buff.toString().trim();
			}
			if (ident.length() > 0) {
				FontFamily family = FontFamily.create(ident);
				list.add(family);
			}
		}
		FontFamily[] families = (FontFamily[]) list.toArray(new FontFamily[list.size()]);
		return new FontFamilyValue(families);
	}
}