package jp.cssj.homare.impl.css.property;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import jp.cssj.homare.css.CSSStyle;
import jp.cssj.homare.css.property.AbstractPrimitivePropertyInfo;
import jp.cssj.homare.css.property.PrimitivePropertyInfo;
import jp.cssj.homare.css.property.PropertyException;
import jp.cssj.homare.css.util.ValueUtils;
import jp.cssj.homare.css.value.CounterSetValue;
import jp.cssj.homare.css.value.NoneValue;
import jp.cssj.homare.css.value.Value;
import jp.cssj.homare.css.value.ValueListValue;
import jp.cssj.homare.ua.UserAgent;
import jp.cssj.sakae.sac.css.LexicalUnit;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: CounterIncrement.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public class CounterIncrement extends AbstractPrimitivePropertyInfo {
	public static final PrimitivePropertyInfo INFO = new CounterIncrement();

	public static Value[] get(CSSStyle style) {
		Value value = style.get(INFO);
		if (value.getValueType() == Value.TYPE_NONE) {
			return null;
		}
		return ((ValueListValue) value).getValues();
	}

	protected CounterIncrement() {
		super("counter-increment");
	}

	public Value getComputedValue(Value value, CSSStyle style) {
		return value;
	}

	public Value getDefault(CSSStyle style) {
		return NoneValue.NONE_VALUE;
	}

	public boolean isInherited() {
		return false;
	}

	public Value parseProperty(LexicalUnit lu, UserAgent ua, URI uri) throws PropertyException {
		switch (lu.getLexicalUnitType()) {
		case LexicalUnit.SAC_IDENT: {// none
			if (ValueUtils.isNone(lu) && lu.getNextLexicalUnit() == null) {
				return NoneValue.NONE_VALUE;
			}
		}
			break;
		}

		List<CounterSetValue> values = new ArrayList<CounterSetValue>();

		while (lu != null) {
			String ident;
			int delta;
			if (lu.getLexicalUnitType() == LexicalUnit.SAC_IDENT) {
				ident = lu.getStringValue();
				lu = lu.getNextLexicalUnit();
			} else {
				throw new PropertyException();
			}
			if (lu != null && lu.getLexicalUnitType() == LexicalUnit.SAC_INTEGER) {
				delta = lu.getIntegerValue();
				lu = lu.getNextLexicalUnit();
			} else {
				delta = 1;
			}
			values.add(new CounterSetValue(ident, delta));
		}
		if (values.isEmpty()) {
			return NoneValue.NONE_VALUE;
		}
		final ValueListValue fvalues = new ValueListValue((Value[]) values.toArray(new Value[values.size()]));
		return fvalues;
	}

}