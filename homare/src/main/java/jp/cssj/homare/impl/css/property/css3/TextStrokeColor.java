package jp.cssj.homare.impl.css.property.css3;

import java.net.URI;

import jp.cssj.homare.css.CSSStyle;
import jp.cssj.homare.css.property.AbstractPrimitivePropertyInfo;
import jp.cssj.homare.css.property.PrimitivePropertyInfo;
import jp.cssj.homare.css.property.PropertyException;
import jp.cssj.homare.css.util.ColorValueUtils;
import jp.cssj.homare.css.value.ColorValue;
import jp.cssj.homare.css.value.DefaultValue;
import jp.cssj.homare.css.value.Value;
import jp.cssj.homare.impl.css.property.CSSColor;
import jp.cssj.homare.ua.UserAgent;
import jp.cssj.sakae.sac.css.LexicalUnit;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: TextStrokeColor.java 1630 2022-05-12 07:40:11Z miyabe $
 */
public class TextStrokeColor extends AbstractPrimitivePropertyInfo {
	public static final PrimitivePropertyInfo INFO = new TextStrokeColor();

	public static jp.cssj.sakae.gc.paint.Color get(CSSStyle style) {
		Value value = style.get(TextStrokeColor.INFO);
		if (value == DefaultValue.DEFAULT_VALUE) {
			return CSSColor.get(style);
		}
		return ((ColorValue) value).getColor();
	}

	protected TextStrokeColor() {
		super("-cssj-text-stroke-color");
	}

	public Value getDefault(CSSStyle style) {
		return DefaultValue.DEFAULT_VALUE;
	}

	public boolean isInherited() {
		return true;
	}

	public Value getComputedValue(Value value, CSSStyle style) {
		return value;
	}

	public Value parseProperty(LexicalUnit lu, UserAgent ua, URI uri) throws PropertyException {
		if (lu.getLexicalUnitType() == LexicalUnit.SAC_IDENT && lu.getStringValue().equalsIgnoreCase("currentcolor")) {
			return DefaultValue.DEFAULT_VALUE;
		}
		Value value = ColorValueUtils.toColor(ua, lu);
		if (value == null) {
			throw new PropertyException();
		}
		return value;
	}

	public int getPriority() {
		return 1;
	}
}