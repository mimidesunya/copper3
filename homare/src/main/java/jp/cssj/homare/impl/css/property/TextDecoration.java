package jp.cssj.homare.impl.css.property;

import java.net.URI;

import jp.cssj.homare.css.CSSStyle;
import jp.cssj.homare.css.property.AbstractPrimitivePropertyInfo;
import jp.cssj.homare.css.property.PrimitivePropertyInfo;
import jp.cssj.homare.css.property.PropertyException;
import jp.cssj.homare.css.value.TextDecorationValue;
import jp.cssj.homare.css.value.Value;
import jp.cssj.homare.ua.UserAgent;
import jp.cssj.sakae.sac.css.LexicalUnit;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: TextDecoration.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public class TextDecoration extends AbstractPrimitivePropertyInfo {
	public static final PrimitivePropertyInfo INFO = new TextDecoration();

	public static byte get(CSSStyle style) {
		TextDecorationValue value = (TextDecorationValue) style.get(INFO);
		return value.getFlags();
	}

	protected TextDecoration() {
		super("text-decoration");
	}

	public Value getDefault(CSSStyle style) {
		return TextDecorationValue.NONE_DECORATION;
	}

	public boolean isInherited() {
		return false;
	}

	public Value getComputedValue(Value value, CSSStyle style) {
		return value;
	}

	public Value parseProperty(LexicalUnit lu, UserAgent ua, URI uri) throws PropertyException {
		TextDecorationValue value;
		if (lu.getLexicalUnitType() == LexicalUnit.SAC_IDENT) {
			String ident = lu.getStringValue().toLowerCase();
			if (ident.equals("none")) {
				value = TextDecorationValue.NONE_DECORATION;
			} else {
				byte flags = 0;
				for (;;) {
					if (ident.equals("underline")) {
						flags |= TextDecorationValue.UNDERLINE;
					} else if (ident.equals("overline")) {
						flags |= TextDecorationValue.OVERLINE;
					} else if (ident.equals("line-through")) {
						flags |= TextDecorationValue.LINE_THROUGH;
					} else if (ident.equals("blink")) {
						flags |= TextDecorationValue.BLINK;
					} else {
						throw new PropertyException();
					}

					lu = lu.getNextLexicalUnit();
					if (lu == null) {
						break;
					}
					if (lu.getLexicalUnitType() != LexicalUnit.SAC_IDENT) {
						throw new PropertyException();
					}
					ident = lu.getStringValue().toLowerCase();
				}
				value = TextDecorationValue.create(flags);
			}
			return value;
		}
		throw new PropertyException();
	}

}