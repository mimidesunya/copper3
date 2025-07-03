package jp.cssj.homare.impl.css.property;

import java.net.URI;

import jp.cssj.homare.css.CSSStyle;
import jp.cssj.homare.css.property.AbstractPrimitivePropertyInfo;
import jp.cssj.homare.css.property.PrimitivePropertyInfo;
import jp.cssj.homare.css.property.PropertyException;
import jp.cssj.homare.css.util.FontValueUtils;
import jp.cssj.homare.css.value.AbsoluteLengthValue;
import jp.cssj.homare.css.value.EmLengthValue;
import jp.cssj.homare.css.value.ExLengthValue;
import jp.cssj.homare.css.value.PercentageValue;
import jp.cssj.homare.css.value.RelativeSizeValue;
import jp.cssj.homare.css.value.Value;
import jp.cssj.homare.css.value.css3.CSS3Value;
import jp.cssj.homare.css.value.css3.ChLengthValue;
import jp.cssj.homare.css.value.css3.RemLengthValue;
import jp.cssj.homare.ua.UserAgent;
import jp.cssj.sakae.sac.css.LexicalUnit;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: FontSize.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public class FontSize extends AbstractPrimitivePropertyInfo {
	public static final PrimitivePropertyInfo INFO = new FontSize();

	public static double get(CSSStyle style) {
		return ((AbsoluteLengthValue) style.get(INFO)).getLength();
	}

	protected FontSize() {
		super("font-size");
	}

	public Value getDefault(CSSStyle style) {
		UserAgent ua = style.getUserAgent();
		return AbsoluteLengthValue.create(ua, ua.getFontSize(UserAgent.FONT_SIZE_MEDIUM));
	}

	public boolean isInherited() {
		return true;
	}

	public Value getComputedValue(Value value, CSSStyle style) {
		switch (value.getValueType()) {
		case Value.TYPE_PERCENTAGE: {
			CSSStyle parentStyle = style.getParentStyle();
			if (parentStyle == null) {
				parentStyle = style;
			}
			double fontSize = FontSize.get(parentStyle);
			PercentageValue percentage = (PercentageValue) value;
			value = AbsoluteLengthValue.create(parentStyle.getUserAgent(), percentage.getRatio() * fontSize);
		}
			break;
		case Value.TYPE_RELATIVE_SIZE: {
			CSSStyle parentStyle = style.getParentStyle();
			if (parentStyle == null) {
				parentStyle = style;
			}
			UserAgent ua = parentStyle.getUserAgent();
			double fontSize = FontSize.get(parentStyle);
			RelativeSizeValue relativeSize = (RelativeSizeValue) value;
			switch (relativeSize.getRelativeSize()) {
			case RelativeSizeValue.LARGER:
				value = AbsoluteLengthValue.create(ua, ua.getLargerFontSize(fontSize));
				break;
			case RelativeSizeValue.SMALLER:
				value = AbsoluteLengthValue.create(ua, ua.getSmallerFontSize(fontSize));
				break;
			default:
				throw new IllegalStateException();
			}
		}
			break;
		case Value.TYPE_EM_LENGTH: {
			CSSStyle parentStyle = style.getParentStyle();
			if (parentStyle == null) {
				parentStyle = style;
			}
			EmLengthValue emLength = (EmLengthValue) value;
			value = emLength.toAbsoluteLength(parentStyle);
		}
			break;
		case Value.TYPE_EX_LENGTH: {
			CSSStyle parentStyle = style.getParentStyle();
			if (parentStyle == null) {
				parentStyle = style;
			}
			ExLengthValue exLength = (ExLengthValue) value;
			value = exLength.toAbsoluteLength(parentStyle);
		}
			break;
		case CSS3Value.TYPE_REM_LENGTH: {
			CSSStyle parentStyle = style.getParentStyle();
			if (parentStyle == null) {
				parentStyle = style;
			}
			RemLengthValue remLength = (RemLengthValue) value;
			value = remLength.toAbsoluteLength(parentStyle);
		}
			break;
		case CSS3Value.TYPE_CH_LENGTH: {
			CSSStyle parentStyle = style.getParentStyle();
			if (parentStyle == null) {
				parentStyle = style;
			}
			ChLengthValue chLength = (ChLengthValue) value;
			value = chLength.toAbsoluteLength(parentStyle);
		}
			break;
		}
		return value;
	}

	public Value parseProperty(LexicalUnit lu, UserAgent ua, URI uri) throws PropertyException {
		final Value value = FontValueUtils.toFontSize(ua, lu);
		if (value == null) {
			throw new PropertyException();
		}
		return value;
	}

}