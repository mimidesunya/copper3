package jp.cssj.homare.impl.css.property.css3;

import java.net.URI;

import jp.cssj.homare.css.CSSStyle;
import jp.cssj.homare.css.property.AbstractPrimitivePropertyInfo;
import jp.cssj.homare.css.property.PrimitivePropertyInfo;
import jp.cssj.homare.css.property.PropertyException;
import jp.cssj.homare.css.util.BorderValueUtils;
import jp.cssj.homare.css.util.ValueUtils;
import jp.cssj.homare.css.value.AbsoluteLengthValue;
import jp.cssj.homare.css.value.Value;
import jp.cssj.homare.css.value.css3.BorderRadiusValue;
import jp.cssj.homare.css.value.ext.CSSJDirectionModeValue;
import jp.cssj.homare.impl.css.property.ext.CSSJDirectionMode;
import jp.cssj.homare.style.box.params.AbstractTextParams;
import jp.cssj.homare.style.box.params.RectBorder.Radius;
import jp.cssj.homare.ua.UserAgent;
import jp.cssj.sakae.sac.css.LexicalUnit;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: BorderBottomLeftRadius.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public class BorderBottomLeftRadius extends AbstractPrimitivePropertyInfo {
	public static final PrimitivePropertyInfo INFO = new BorderBottomLeftRadius();

	public static Radius get(CSSStyle style) {
		final PrimitivePropertyInfo info;
		// 回転
		switch (CSSJDirectionMode.get(style)) {
		case CSSJDirectionModeValue.PHYSICAL:
			info = INFO;
			break;
		case CSSJDirectionModeValue.HORIZONTAL_TB:
			switch (BlockFlow.get(style)) {
			case AbstractTextParams.FLOW_RL:
				info = BorderBottomRightRadius.INFO;
				break;
			case AbstractTextParams.FLOW_LR:
				info = BorderBottomLeftRadius.INFO;
				break;
			default:
				info = INFO;
				break;
			}
			break;
		case CSSJDirectionModeValue.VERTICAL_RL:
			switch (BlockFlow.get(style)) {
			case AbstractTextParams.FLOW_TB:
				info = BorderTopLeftRadius.INFO;
				break;
			default:
				info = INFO;
				break;
			}
			break;
		default:
			throw new IllegalStateException();
		}
		final BorderRadiusValue r = (BorderRadiusValue) style.get(info);
		return Radius.create(((AbsoluteLengthValue) r.hr).getLength(), ((AbsoluteLengthValue) r.vr).getLength());
	}

	protected BorderBottomLeftRadius() {
		super("border-bottom-left-radius");
	}

	public Value getDefault(CSSStyle style) {
		return BorderRadiusValue.ZERO_RADIUS;
	}

	public boolean isInherited() {
		return false;
	}

	public Value getComputedValue(Value value, CSSStyle style) {
		final BorderRadiusValue r = (BorderRadiusValue) value;
		final AbsoluteLengthValue hr = (AbsoluteLengthValue) ValueUtils.emExToAbsoluteLength(r.hr, style);
		final AbsoluteLengthValue vr = (AbsoluteLengthValue) ValueUtils.emExToAbsoluteLength(r.vr, style);
		return BorderRadiusValue.create(hr, vr);
	}

	public Value parseProperty(LexicalUnit lu, UserAgent ua, URI uri) throws PropertyException {
		final BorderRadiusValue value = BorderValueUtils.toBorderRadius(ua, lu);
		if (value == null) {
			throw new PropertyException();
		}
		return value;
	}
}