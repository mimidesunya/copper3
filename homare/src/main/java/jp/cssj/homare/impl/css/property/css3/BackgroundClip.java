package jp.cssj.homare.impl.css.property.css3;

import java.net.URI;

import jp.cssj.homare.css.CSSStyle;
import jp.cssj.homare.css.property.AbstractPrimitivePropertyInfo;
import jp.cssj.homare.css.property.PrimitivePropertyInfo;
import jp.cssj.homare.css.property.PropertyException;
import jp.cssj.homare.css.util.ColorValueUtils;
import jp.cssj.homare.css.value.Value;
import jp.cssj.homare.css.value.css3.BackgroundClipValue;
import jp.cssj.homare.ua.UserAgent;
import jp.cssj.sakae.sac.css.LexicalUnit;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: ColumnFill.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public class BackgroundClip extends AbstractPrimitivePropertyInfo {

	public static final PrimitivePropertyInfo INFO = new BackgroundClip();

	public static byte get(CSSStyle style) {
		BackgroundClipValue value = (BackgroundClipValue) style.get(INFO);
		return value.getBackgroundClip();
	}

	protected BackgroundClip() {
		super("background-clip");
	}

	public Value getDefault(CSSStyle style) {
		return BackgroundClipValue.BORDER_BOX_VALUE;
	}

	public boolean isInherited() {
		return false;
	}

	public Value getComputedValue(Value value, CSSStyle style) {
		return value;
	}

	public Value parseProperty(LexicalUnit lu, UserAgent ua, URI uri) throws PropertyException {
		BackgroundClipValue value = ColorValueUtils.toBackgroundClip(lu);
		if (value != null) {
			return value;
		}
		throw new PropertyException();
	}

}