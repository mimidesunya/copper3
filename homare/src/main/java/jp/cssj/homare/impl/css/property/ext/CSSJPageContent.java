package jp.cssj.homare.impl.css.property.ext;

import java.net.URI;

import jp.cssj.homare.css.CSSElement;
import jp.cssj.homare.css.CSSStyle;
import jp.cssj.homare.css.property.AbstractCompositePrimitivePropertyInfo;
import jp.cssj.homare.css.property.CompositeProperty.Entry;
import jp.cssj.homare.css.property.PrimitivePropertyInfo;
import jp.cssj.homare.css.property.PropertyException;
import jp.cssj.homare.css.value.InheritValue;
import jp.cssj.homare.css.value.NoneValue;
import jp.cssj.homare.css.value.StringValue;
import jp.cssj.homare.css.value.Value;
import jp.cssj.homare.css.value.ext.CSSJPageContentValue;
import jp.cssj.homare.style.util.ByteList;
import jp.cssj.homare.ua.UserAgent;
import jp.cssj.sakae.sac.css.LexicalUnit;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: CSSJPageContent.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public class CSSJPageContent extends AbstractCompositePrimitivePropertyInfo {
	public static final PrimitivePropertyInfo INFO_NAME = new CSSJPageContent();
	public static final PrimitivePropertyInfo INFO_PAGE = new CSSJPageContent();

	private static final PrimitivePropertyInfo[] PRIMITIVES = { INFO_NAME, INFO_PAGE };

	public static String getName(CSSStyle style) {
		Value value = style.get(INFO_NAME);
		if (value.getValueType() == Value.TYPE_NONE) {
			return null;
		}
		return ((StringValue) value).getString();
	}

	public static byte[] getPages(CSSStyle style) {
		Value value = style.get(INFO_PAGE);
		if (value.getValueType() == Value.TYPE_NONE) {
			return null;
		}
		return ((CSSJPageContentValue) value).getPages();
	}

	private CSSJPageContent() {
		super("-cssj-page-content");
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

	protected PrimitivePropertyInfo[] getPrimitives() {
		return PRIMITIVES;
	}

	protected Entry[] parseProperty(LexicalUnit lu, UserAgent ua, URI uri) throws PropertyException {
		if (lu.getLexicalUnitType() == LexicalUnit.SAC_INHERIT) {
			return new Entry[] { new Entry(CSSJPageContent.INFO_NAME, InheritValue.INHERIT_VALUE),
					new Entry(CSSJPageContent.INFO_PAGE, InheritValue.INHERIT_VALUE) };
		}
		final Value name, page;
		{
			short luType = lu.getLexicalUnitType();
			switch (luType) {
			case LexicalUnit.SAC_IDENT:
				String ident = lu.getStringValue().toLowerCase();
				if (ident.equals("none")) {
					name = NoneValue.NONE_VALUE;
				} else {
					name = new StringValue(lu.getStringValue());
				}
				break;

			case LexicalUnit.SAC_STRING_VALUE:
				name = new StringValue(lu.getStringValue());
				break;

			default:
				throw new PropertyException();
			}
		}
		{
			lu = lu.getNextLexicalUnit();
			if (lu == null) {
				page = NoneValue.NONE_VALUE;
			} else {
				ByteList list = new ByteList();
				do {
					short luType = lu.getLexicalUnitType();
					switch (luType) {
					case LexicalUnit.SAC_IDENT:
						String ident = lu.getStringValue().toLowerCase();
						if (ident.equals("first")) {
							list.add(CSSElement.PC_FIRST);
							break;
						} else if (ident.equals("right")) {
							list.add(CSSElement.PC_RIGHT);
							break;
						} else if (ident.equals("left")) {
							list.add(CSSElement.PC_LEFT);
							break;
						} else if (ident.equals("single")) {
							list.add((byte) 0);
							break;
						}

					default:
						throw new PropertyException();
					}
					lu = lu.getNextLexicalUnit();
				} while (lu != null);
				page = new CSSJPageContentValue(list.toArray());
			}
		}
		return new Entry[] { new Entry(CSSJPageContent.INFO_NAME, name), new Entry(CSSJPageContent.INFO_PAGE, page) };
	}
}