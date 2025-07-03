package jp.cssj.homare.impl.css.property;

import java.net.URI;
import java.util.ArrayList;

import jp.cssj.homare.css.CSSStyle;
import jp.cssj.homare.css.lang.LanguageProfile;
import jp.cssj.homare.css.lang.LanguageProfileBundle;
import jp.cssj.homare.css.property.AbstractPrimitivePropertyInfo;
import jp.cssj.homare.css.property.PrimitivePropertyInfo;
import jp.cssj.homare.css.property.PropertyException;
import jp.cssj.homare.css.util.ValueUtils;
import jp.cssj.homare.css.value.NoneValue;
import jp.cssj.homare.css.value.QuotesValue;
import jp.cssj.homare.css.value.Value;
import jp.cssj.homare.css.value.ValueListValue;
import jp.cssj.homare.ua.UserAgent;
import jp.cssj.sakae.sac.css.LexicalUnit;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: Quotes.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public class Quotes extends AbstractPrimitivePropertyInfo {
	public static final PrimitivePropertyInfo INFO = new Quotes();

	public static Value[] get(CSSStyle style) {
		Value value = style.get(INFO);
		if (value.getValueType() == Value.TYPE_NONE) {
			return null;
		}
		return ((ValueListValue) value).getValues();
	}

	protected Quotes() {
		super("quotes");
	}

	public Value getComputedValue(Value value, CSSStyle style) {
		return value;
	}

	public Value getDefault(CSSStyle style) {
		LanguageProfile lang = LanguageProfileBundle.getLanguageProfile(style.getCSSElement().lang);
		return lang.getQuotes();
	}

	public boolean isInherited() {
		return true;
	}

	public Value parseProperty(LexicalUnit lu, UserAgent ua, URI uri) throws PropertyException {
		switch (lu.getLexicalUnitType()) {
		case LexicalUnit.SAC_IDENT: {// none
			if (ValueUtils.isNone(lu)) {
				return NoneValue.NONE_VALUE;
			}
			throw new PropertyException();
		}
		}

		ArrayList<QuotesValue> values = new ArrayList<QuotesValue>();

		for (; lu != null; lu = lu.getNextLexicalUnit()) {
			switch (lu.getLexicalUnitType()) {
			case LexicalUnit.SAC_STRING_VALUE: {// <string>
				String open = lu.getStringValue();
				lu = lu.getNextLexicalUnit();
				if (lu != null && lu.getLexicalUnitType() == LexicalUnit.SAC_STRING_VALUE) {
					values.add(new QuotesValue(open, lu.getStringValue()));
				} else {
					throw new PropertyException();
				}
			}
				break;

			default:
				throw new PropertyException();
			}
		}
		if (values.isEmpty()) {
			return NoneValue.NONE_VALUE;
		}
		final ValueListValue fvalues = new ValueListValue((Value[]) values.toArray(new Value[values.size()]));
		return fvalues;
	}

}