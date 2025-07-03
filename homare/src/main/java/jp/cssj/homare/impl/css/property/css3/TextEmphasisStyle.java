package jp.cssj.homare.impl.css.property.css3;

import java.net.URI;

import jp.cssj.homare.css.CSSStyle;
import jp.cssj.homare.css.property.AbstractPrimitivePropertyInfo;
import jp.cssj.homare.css.property.PrimitivePropertyInfo;
import jp.cssj.homare.css.property.PropertyException;
import jp.cssj.homare.css.util.ValueUtils;
import jp.cssj.homare.css.value.NoneValue;
import jp.cssj.homare.css.value.StringValue;
import jp.cssj.homare.css.value.Value;
import jp.cssj.homare.style.util.StyleUtils;
import jp.cssj.homare.ua.UserAgent;
import jp.cssj.sakae.sac.css.LexicalUnit;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: TextEmphasisStyle.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public class TextEmphasisStyle extends AbstractPrimitivePropertyInfo {

	public static final PrimitivePropertyInfo INFO = new TextEmphasisStyle();

	protected static final Value AUTO_FILLED = new StringValue("filled");
	protected static final Value AUTO_OPEN = new StringValue("open");
	protected static final Value FILLED_DOT = new StringValue("\u2022");
	protected static final Value OPEN_DOT = new StringValue("\u25E6");
	protected static final Value FILLED_CIRCLE = new StringValue("\u25CF");
	protected static final Value OPEN_CIRCLE = new StringValue("\u25CB");
	protected static final Value FILLED_DOUBLE_CIRCLE = new StringValue("\u25C9");
	protected static final Value OPEN_DOUBLE_CIRCLE = new StringValue("\u25CE");
	protected static final Value FILLED_TRIANGLE = new StringValue("\u25B2");
	protected static final Value OPEN_TRIANGLE = new StringValue("\u25B3");
	protected static final Value FILLED_SESAME = new StringValue("\uFE45");
	protected static final Value OPEN_SESAME = new StringValue("\uFE46");

	public static String get(CSSStyle style) {
		Value value = (Value) style.get(INFO);
		if (value.getValueType() == Value.TYPE_NONE) {
			return null;
		}
		return ((StringValue) value).getString();
	}

	protected TextEmphasisStyle() {
		super("-cssj-text-emphasis-style");
	}

	public Value getDefault(CSSStyle style) {
		return NoneValue.NONE_VALUE;
	}

	public boolean isInherited() {
		return true;
	}

	public Value getComputedValue(Value value, CSSStyle style) {
		if (value == AUTO_FILLED) {
			if (StyleUtils.isVertical(BlockFlow.get(style))) {
				value = FILLED_SESAME;
			} else {
				value = FILLED_CIRCLE;
			}
		} else if (value == AUTO_OPEN) {
			if (StyleUtils.isVertical(BlockFlow.get(style))) {
				value = OPEN_SESAME;
			} else {
				value = OPEN_CIRCLE;
			}
		}
		return value;
	}

	public Value parseProperty(LexicalUnit lu, UserAgent ua, URI uri) throws PropertyException {
		byte fill = 0, type = 0;
		do {
			switch (lu.getLexicalUnitType()) {
			case LexicalUnit.SAC_IDENT:
				if (ValueUtils.isNone(lu)) {
					if (lu.getNextLexicalUnit() != null) {
						throw new PropertyException();
					}
					return NoneValue.NONE_VALUE;
				}
				String ident = lu.getStringValue().toLowerCase();
				if (ident.equals("filled")) {
					if (fill != 0) {
						throw new PropertyException();
					}
					fill = 1;
				} else if (ident.equals("open")) {
					if (fill != 0) {
						throw new PropertyException();
					}
					fill = 2;
				} else if (ident.equals("dot")) {
					if (type != 0) {
						throw new PropertyException();
					}
					type = 1;
				} else if (ident.equals("circle")) {
					if (type != 0) {
						throw new PropertyException();
					}
					type = 2;
				} else if (ident.equals("double-circle")) {
					if (type != 0) {
						throw new PropertyException();
					}
					type = 3;
				} else if (ident.equals("triangle")) {
					if (type != 0) {
						throw new PropertyException();
					}
					type = 4;
				} else if (ident.equals("sesame")) {
					if (type != 0) {
						throw new PropertyException();
					}
					type = 5;
				} else {
					throw new PropertyException();
				}
				break;
			case LexicalUnit.SAC_STRING_VALUE:
				if (fill != 0 || lu.getNextLexicalUnit() != null) {
					throw new PropertyException();
				}
				return new StringValue(lu.getStringValue());

			default:
				throw new PropertyException();
			}
			lu = lu.getNextLexicalUnit();
		} while (lu != null);
		if (type == 0) {
			type = -1;
		}
		Value str;
		switch (type) {
		case -1:
			if (fill != 2) {
				str = AUTO_FILLED;
			} else {
				str = AUTO_OPEN;
			}
			break;
		case 1:
			if (fill != 2) {
				str = FILLED_DOT;
			} else {
				str = OPEN_DOT;
			}
			break;
		case 2:
			if (fill != 2) {
				str = FILLED_CIRCLE;
			} else {
				str = OPEN_CIRCLE;
			}
			break;
		case 3:
			if (fill != 2) {
				str = FILLED_DOUBLE_CIRCLE;
			} else {
				str = OPEN_DOUBLE_CIRCLE;
			}
			break;
		case 4:
			if (fill != 2) {
				str = FILLED_TRIANGLE;
			} else {
				str = OPEN_TRIANGLE;
			}
			break;
		case 5:
			if (fill != 2) {
				str = FILLED_SESAME;
			} else {
				str = OPEN_SESAME;
			}
			break;
		default:
			throw new PropertyException();
		}
		return str;
	}

}