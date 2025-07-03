package jp.cssj.homare.css.property;

import java.net.URI;

import jp.cssj.homare.css.CSSStyle;
import jp.cssj.homare.css.property.CompositeProperty.Entry;
import jp.cssj.homare.css.value.InheritValue;
import jp.cssj.homare.ua.UserAgent;
import jp.cssj.sakae.sac.css.LexicalUnit;

/**
 * 複合特性です。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: AbstractCompositePrimitivePropertyInfo.java 3806 2012-07-10
 *          07:03:19Z miyabe $
 */
public abstract class AbstractCompositePrimitivePropertyInfo extends AbstractPropertyInfo
		implements PrimitivePropertyInfo {

	protected AbstractCompositePrimitivePropertyInfo(String name) {
		super(name);
	}

	protected abstract PrimitivePropertyInfo[] getPrimitives();

	protected abstract Entry[] parseProperty(LexicalUnit lu, UserAgent ua, URI uri) throws PropertyException;

	public final Property parseProperty(LexicalUnit lu, UserAgent ua, URI uri, boolean important)
			throws PropertyException {
		Entry[] entries;
		if (lu.getLexicalUnitType() == LexicalUnit.SAC_INHERIT) {
			// 継承
			PrimitivePropertyInfo[] primitives = this.getPrimitives();
			entries = new Entry[primitives.length];
			for (int i = 0; i < entries.length; ++i) {
				entries[i] = new Entry(primitives[i], InheritValue.INHERIT_VALUE);
			}
		} else {
			entries = this.parseProperty(lu, ua, uri);
		}
		return new CompositeProperty(this.getName(), entries, uri, important);
	}

	public PrimitivePropertyInfo getEffectiveInfo(CSSStyle style) {
		return this;
	}
}