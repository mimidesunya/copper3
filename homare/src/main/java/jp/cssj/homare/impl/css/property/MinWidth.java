package jp.cssj.homare.impl.css.property;

import java.net.URI;

import jp.cssj.homare.css.CSSStyle;
import jp.cssj.homare.css.property.AbstractPrimitivePropertyInfo;
import jp.cssj.homare.css.property.PrimitivePropertyInfo;
import jp.cssj.homare.css.property.PropertyException;
import jp.cssj.homare.css.util.BoxValueUtils;
import jp.cssj.homare.css.util.ValueUtils;
import jp.cssj.homare.css.value.AbsoluteLengthValue;
import jp.cssj.homare.css.value.Value;
import jp.cssj.homare.css.value.ext.CSSJDirectionModeValue;
import jp.cssj.homare.impl.css.property.css3.BlockFlow;
import jp.cssj.homare.impl.css.property.ext.CSSJDirectionMode;
import jp.cssj.homare.impl.css.property.internal.CSSJInternalImage;
import jp.cssj.homare.style.box.params.AbstractTextParams;
import jp.cssj.homare.style.box.params.Length;
import jp.cssj.homare.ua.UserAgent;
import jp.cssj.sakae.sac.css.LexicalUnit;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: MinWidth.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public class MinWidth extends AbstractPrimitivePropertyInfo {
	public static final PrimitivePropertyInfo INFO = new MinWidth();

	public static Value get(CSSStyle style) {
		PrimitivePropertyInfo info;
		if (CSSJInternalImage.getImage(style) != null) {
			// 画像には回転を適用しない
			info = INFO;
		} else {
			// 回転
			switch (CSSJDirectionMode.get(style)) {
			case CSSJDirectionModeValue.PHYSICAL:
				info = INFO;
				break;
			case CSSJDirectionModeValue.HORIZONTAL_TB:
				switch (BlockFlow.get(style)) {
				case AbstractTextParams.FLOW_RL:
				case AbstractTextParams.FLOW_LR:
					info = MinHeight.INFO;
					break;
				default:
					info = INFO;
					break;
				}
				break;
			case CSSJDirectionModeValue.VERTICAL_RL:
				switch (BlockFlow.get(style)) {
				case AbstractTextParams.FLOW_TB:
					info = MinHeight.INFO;
					break;
				default:
					info = INFO;
					break;
				}
				break;
			default:
				throw new IllegalStateException();
			}
		}
		return style.get(info);
	}

	public static Length getLength(CSSStyle style) {
		return BoxValueUtils.toLength(MinWidth.get(style));
	}

	private MinWidth() {
		super("min-width");
	}

	public Value getDefault(CSSStyle style) {
		return AbsoluteLengthValue.ZERO;
	}

	public boolean isInherited() {
		return false;
	}

	public Value getComputedValue(Value value, CSSStyle style) {
		return ValueUtils.emExToAbsoluteLength(value, style);
	}

	public Value parseProperty(LexicalUnit lu, UserAgent ua, URI uri) throws PropertyException {
		Value value = BoxValueUtils.toPositiveLength(ua, lu);
		if (value == null) {
			throw new PropertyException();
		}
		return value;
	}

	public int getPriority() {
		return 1;
	}
}