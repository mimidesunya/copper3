package jp.cssj.homare.impl.css.property;

import java.net.URI;

import jp.cssj.homare.css.CSSStyle;
import jp.cssj.homare.css.property.AbstractPrimitivePropertyInfo;
import jp.cssj.homare.css.property.PrimitivePropertyInfo;
import jp.cssj.homare.css.property.PropertyException;
import jp.cssj.homare.css.util.BorderValueUtils;
import jp.cssj.homare.css.value.BorderStyleValue;
import jp.cssj.homare.css.value.Value;
import jp.cssj.homare.css.value.ext.CSSJDirectionModeValue;
import jp.cssj.homare.impl.css.property.css3.BlockFlow;
import jp.cssj.homare.impl.css.property.ext.CSSJDirectionMode;
import jp.cssj.homare.style.box.params.AbstractTextParams;
import jp.cssj.homare.ua.UserAgent;
import jp.cssj.sakae.sac.css.LexicalUnit;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: BorderRightStyle.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public class BorderRightStyle extends AbstractPrimitivePropertyInfo {
	public static final PrimitivePropertyInfo INFO = new BorderRightStyle();

	public static short get(CSSStyle style) {
		PrimitivePropertyInfo info;
		// 回転
		switch (CSSJDirectionMode.get(style)) {
		case CSSJDirectionModeValue.PHYSICAL:
			info = INFO;
			break;
		case CSSJDirectionModeValue.HORIZONTAL_TB:
			switch (BlockFlow.get(style)) {
			case AbstractTextParams.FLOW_RL:
				info = BorderTopStyle.INFO;
				break;
			case AbstractTextParams.FLOW_LR:
				info = BorderBottomStyle.INFO;
				break;
			default:
				info = INFO;
				break;
			}
			break;
		case CSSJDirectionModeValue.VERTICAL_RL:
			switch (BlockFlow.get(style)) {
			case AbstractTextParams.FLOW_TB:
				info = BorderBottomStyle.INFO;
				break;
			default:
				info = INFO;
				break;
			}
			break;
		default:
			throw new IllegalStateException();
		}
		BorderStyleValue value = (BorderStyleValue) style.get(info);
		return value.getBorderStyle();
	}

	protected BorderRightStyle() {
		super("border-right-style");
	}

	public Value getDefault(CSSStyle style) {
		return BorderStyleValue.NONE_VALUE;
	}

	public boolean isInherited() {
		return false;
	}

	public Value getComputedValue(Value value, CSSStyle style) {
		return value;
	}

	public Value parseProperty(LexicalUnit lu, UserAgent ua, URI uri) throws PropertyException {
		Value value = BorderValueUtils.toBorderStyle(lu);
		if (value == null) {
			throw new PropertyException();
		}
		return value;
	}

	public int getPriority() {
		return 1;
	}
}