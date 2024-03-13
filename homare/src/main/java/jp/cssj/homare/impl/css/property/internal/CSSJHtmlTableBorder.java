package jp.cssj.homare.impl.css.property.internal;

import java.net.URI;

import jp.cssj.homare.css.CSSStyle;
import jp.cssj.homare.css.property.AbstractPrimitivePropertyInfo;
import jp.cssj.homare.css.property.PrimitivePropertyInfo;
import jp.cssj.homare.css.property.PropertyException;
import jp.cssj.homare.css.value.Value;
import jp.cssj.homare.css.value.internal.CSSJHtmlTableBorderValue;
import jp.cssj.homare.ua.UserAgent;
import jp.cssj.sakae.sac.css.LexicalUnit;

/**
 * HTMLのテーブルborderに相当する内部特性です。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: CSSJHtmlTableBorder.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public class CSSJHtmlTableBorder extends AbstractPrimitivePropertyInfo {
	public static final PrimitivePropertyInfo INFO = new CSSJHtmlTableBorder();

	public static CSSJHtmlTableBorderValue get(CSSStyle style) {
		CSSJHtmlTableBorderValue value = (CSSJHtmlTableBorderValue) style.get(INFO);
		return value;
	}

	public static void set(CSSStyle style, CSSJHtmlTableBorderValue value) {
		style.set(INFO, value);
	}

	public CSSJHtmlTableBorder() {
		super("-cssj-html-table-border");
	}

	public Value getComputedValue(Value value, CSSStyle style) {
		return value;
	}

	public Value getDefault(CSSStyle style) {
		return CSSJHtmlTableBorderValue.NULL_BORDER;
	}

	public boolean isInherited() {
		return true;
	}

	public Value parseProperty(LexicalUnit lu, UserAgent ua, URI uri) throws PropertyException {
		throw new UnsupportedOperationException();
	}
}