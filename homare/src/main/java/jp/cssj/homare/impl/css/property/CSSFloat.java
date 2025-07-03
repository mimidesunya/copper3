package jp.cssj.homare.impl.css.property;

import java.net.URI;

import jp.cssj.homare.css.CSSStyle;
import jp.cssj.homare.css.property.AbstractPrimitivePropertyInfo;
import jp.cssj.homare.css.property.PrimitivePropertyInfo;
import jp.cssj.homare.css.property.PropertyException;
import jp.cssj.homare.css.value.CSSFloatValue;
import jp.cssj.homare.css.value.Value;
import jp.cssj.homare.ua.UserAgent;
import jp.cssj.sakae.sac.css.LexicalUnit;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: CSSFloat.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public class CSSFloat extends AbstractPrimitivePropertyInfo {
	public static final PrimitivePropertyInfo INFO = new CSSFloat();

	public static byte get(CSSStyle style) {
		CSSFloatValue value = (CSSFloatValue) style.get(INFO);
		return value.getFloat();
	}

	private CSSFloat() {
		super("float");
	}

	public Value getDefault(CSSStyle style) {
		return CSSFloatValue.NONE_VALUE;
	}

	public boolean isInherited() {
		return false;
	}

	public Value getComputedValue(Value value, CSSStyle style) {
		return value;
	}

	public Value parseProperty(LexicalUnit lu, UserAgent ua, URI uri) throws PropertyException {
		short luType = lu.getLexicalUnitType();
		switch (luType) {

		case LexicalUnit.SAC_IDENT:
			String ident = lu.getStringValue().toLowerCase();
			if (ident.equals("none")) {
				return CSSFloatValue.NONE_VALUE;
			} else if (ident.equals("left")) {
				return CSSFloatValue.LEFT_VALUE;
			} else if (ident.equals("right")) {
				return CSSFloatValue.RIGHT_VALUE;
			} else if (ident.equals("start")) {
				return CSSFloatValue.START_VALUE;
			} else if (ident.equals("end")) {
				return CSSFloatValue.END_VALUE;
			}

		default:
			throw new PropertyException();
		}
	}

}