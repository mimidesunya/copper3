package jp.cssj.homare.impl.css.property;

import java.net.URI;

import jp.cssj.homare.css.CSSStyle;
import jp.cssj.homare.css.property.AbstractPrimitivePropertyInfo;
import jp.cssj.homare.css.property.PrimitivePropertyInfo;
import jp.cssj.homare.css.property.PropertyException;
import jp.cssj.homare.css.util.ColorValueUtils;
import jp.cssj.homare.css.value.ColorValue;
import jp.cssj.homare.css.value.DefaultValue;
import jp.cssj.homare.css.value.TransparentValue;
import jp.cssj.homare.css.value.Value;
import jp.cssj.homare.css.value.ext.CSSJDirectionModeValue;
import jp.cssj.homare.impl.css.property.css3.BlockFlow;
import jp.cssj.homare.impl.css.property.ext.CSSJDirectionMode;
import jp.cssj.homare.style.box.params.AbstractTextParams;
import jp.cssj.homare.ua.UserAgent;
import jp.cssj.sakae.sac.css.LexicalUnit;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: BorderLeftColor.java 1624 2022-05-02 08:59:55Z miyabe $
 */
public class BorderLeftColor extends AbstractPrimitivePropertyInfo {
	public static final PrimitivePropertyInfo INFO = new BorderLeftColor();

	public static jp.cssj.sakae.gc.paint.Color get(CSSStyle style) {
		PrimitivePropertyInfo info;
		// 回転
		switch (CSSJDirectionMode.get(style)) {
		case CSSJDirectionModeValue.PHYSICAL:
			info = INFO;
			break;
		case CSSJDirectionModeValue.HORIZONTAL_TB:
			switch (BlockFlow.get(style)) {
			case AbstractTextParams.FLOW_RL:
				info = BorderBottomColor.INFO;
				break;
			case AbstractTextParams.FLOW_LR:
				info = BorderTopColor.INFO;
				break;
			default:
				info = INFO;
				break;
			}
			break;
		case CSSJDirectionModeValue.VERTICAL_RL:
			switch (BlockFlow.get(style)) {
			case AbstractTextParams.FLOW_TB:
				info = BorderTopColor.INFO;
				break;
			default:
				info = INFO;
				break;
			}
			break;
		default:
			throw new IllegalStateException();
		}
		Value value = style.get(info);
		if (value.getValueType() == Value.TYPE_TRANSPARENT) {
			return null;
		}
		return ((ColorValue) value).getColor();
	}

	protected BorderLeftColor() {
		super("border-left-color");
	}

	public Value getDefault(CSSStyle style) {
		return DefaultValue.DEFAULT_VALUE;
	}

	public boolean isInherited() {
		return false;
	}

	public Value getComputedValue(Value value, CSSStyle style) {
		if (value == DefaultValue.DEFAULT_VALUE) {
			value = style.get(CSSColor.INFO);
		}
		return value;
	}

	public Value parseProperty(LexicalUnit lu, UserAgent ua, URI uri) throws PropertyException {
		if (ColorValueUtils.isTransparent(lu)) {
			return TransparentValue.TRANSPARENT_VALUE;
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