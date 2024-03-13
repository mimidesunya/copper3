package jp.cssj.homare.impl.css.lang;

import jp.cssj.homare.css.CSSStyle;
import jp.cssj.homare.css.lang.LanguageProfile;
import jp.cssj.homare.css.value.QuotesValue;
import jp.cssj.homare.css.value.TextTransformValue;
import jp.cssj.homare.css.value.Value;
import jp.cssj.homare.css.value.ValueListValue;
import jp.cssj.homare.css.value.css3.WordBreakValue;
import jp.cssj.homare.css.value.ext.CSSJBreakRuleValue;
import jp.cssj.homare.impl.css.property.css3.WordBreak;
import jp.cssj.homare.impl.css.property.ext.CSSJBreakCharacters;
import jp.cssj.homare.impl.css.property.ext.CSSJNoBreakCharacters;
import jp.cssj.homare.style.box.params.AbstractTextParams;
import jp.cssj.sakae.gc.text.hyphenation.Hyphenation;
import jp.cssj.sakae.gc.text.hyphenation.impl.JapaneseHyphenation;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: LanguageProfile_ja.java 1574 2018-10-26 02:44:00Z miyabe $
 */
public class LanguageProfile_ja implements LanguageProfile {
	private static final ValueListValue QUOTES = new ValueListValue(
			new Value[] { new QuotesValue("「", "」"), new QuotesValue("『", "』"), });

	private final Hyphenation normalHyph = new JapaneseHyphenation();

	private final Hyphenation breakAllHyph = new BreakAllHyphenation();

	private final Hyphenation keepAllHyph = new JapaneseKeepAllHyphenation();

	public String getLanguage() {
		return "ja";
	}

	public boolean isWhitespace(char c) {
		if (c == '　' || c == 0xA0) {
			return false;
		}

		return Character.isWhitespace(c);
	}

	public int countFirstLetter(char[] ch, int off, int len) {
		int i = 0;

		// 空白文字は飛ばす
		for (; i < len; ++i) {
			if (!isWhitespace(ch[off + i])) {
				break;
			}
		}

		// 括弧と次の文字、数字は分割されないようにする
		short state = 0;
		for (; i < len; ++i) {
			int type = Character.getType(ch[off + i]);
			switch (state) {
			case 0: {// 初期状態
				switch (type) {
				case Character.START_PUNCTUATION:
				case Character.END_PUNCTUATION:
				case Character.OTHER_PUNCTUATION: {
					// 括弧
					state = 0;
				}
					break;

				case Character.DECIMAL_DIGIT_NUMBER:
				case Character.LETTER_NUMBER:
				case Character.OTHER_NUMBER: {
					// 数字
					state = 1;
				}
					break;

				default: {
					return i + 1;
				}
				}
			}
				break;

			case 1: {// 数字が見つかった
				switch (type) {
				case Character.DECIMAL_DIGIT_NUMBER:
				case Character.LETTER_NUMBER:
				case Character.OTHER_NUMBER:
					break;

				default: {
					return i;
				}
				}
			}
				break;
			}
		}
		return len;
	}

	public ValueListValue getQuotes() {
		return QUOTES;
	}

	public void transform(TextTransformValue transform, char[] ch, int off, int len) {
		switch (transform.getTextTransform()) {
		case AbstractTextParams.TEXT_TRANSFORM_CAPITALIZE: {
			boolean spaceBefore = true;
			for (int i = 0; i < len; ++i) {
				char c = ch[i + off];
				if (Character.isLetter(c)) {
					if (spaceBefore) {
						ch[i + off] = Character.toUpperCase(c);
					}
					spaceBefore = false;
				} else {
					spaceBefore = true;
				}
			}
		}
			break;

		case AbstractTextParams.TEXT_TRANSFORM_LOWERCASE: {
			for (int i = 0; i < len; ++i) {
				ch[i + off] = Character.toLowerCase(ch[i + off]);
			}
		}
			break;

		case AbstractTextParams.TEXT_TRANSFORM_UPPERCASE: {
			for (int i = 0; i < len; ++i) {
				ch[i + off] = Character.toUpperCase(ch[i + off]);
			}
		}
			break;

		case AbstractTextParams.TEXT_TRANSFORM_NONE:
			break;

		default:
			throw new IllegalStateException();
		}
	}

	public Hyphenation getHyphenation(final CSSStyle style) {
		// 禁則処理
		switch (WordBreak.get(style)) {
		case WordBreakValue.NORMAL:
			final CSSJBreakRuleValue include = CSSJNoBreakCharacters.get(style);
			final CSSJBreakRuleValue exclude = CSSJBreakCharacters.get(style);
			if (include != CSSJBreakRuleValue.NONE_VALUE || exclude != CSSJBreakRuleValue.NONE_VALUE) {
				return new CSSJHyphenation(include, exclude);
			}

			return this.normalHyph;

		case WordBreakValue.KEEP_ALL:
			return this.keepAllHyph;

		case WordBreakValue.BREAK_ALL:
			return this.breakAllHyph;
		default:
			throw new IllegalStateException();
		}

	}
}