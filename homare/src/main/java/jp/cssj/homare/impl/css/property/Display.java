package jp.cssj.homare.impl.css.property;

import java.net.URI;

import jp.cssj.homare.css.CSSStyle;
import jp.cssj.homare.css.property.AbstractPrimitivePropertyInfo;
import jp.cssj.homare.css.property.PrimitivePropertyInfo;
import jp.cssj.homare.css.property.PropertyException;
import jp.cssj.homare.css.value.CSSFloatValue;
import jp.cssj.homare.css.value.DisplayValue;
import jp.cssj.homare.css.value.PositionValue;
import jp.cssj.homare.css.value.Value;
import jp.cssj.homare.impl.css.property.css3.BlockFlow;
import jp.cssj.homare.impl.css.property.internal.CSSJInternalImage;
import jp.cssj.homare.style.util.StyleUtils;
import jp.cssj.homare.ua.UserAgent;
import jp.cssj.sakae.sac.css.LexicalUnit;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: Display.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public class Display extends AbstractPrimitivePropertyInfo {
	public static final PrimitivePropertyInfo INFO = new Display();

	public static byte get(CSSStyle style) {
		DisplayValue value = (DisplayValue) style.get(INFO);
		return value.getDisplay();
	}

	protected Display() {
		super("display");
	}

	public Value getComputedValue(Value value, CSSStyle style) {
		byte display = ((DisplayValue) value).getDisplay();

		// 浮動体のための変換
		switch (display) {
		case DisplayValue.INLINE_TABLE: {
			final byte position = CSSPosition.get(style);
			if (position == PositionValue.ABSOLUTE || position == PositionValue.FIXED
					|| position == PositionValue._CSSJ_CURRENT_PAGE) {
				break;
			}
			if (CSSFloat.get(style) != CSSFloatValue.NONE) {
				value = DisplayValue.TABLE_VALUE;
				display = DisplayValue.TABLE;
			}
		}
			break;
		case DisplayValue.INLINE: {
			final short position = CSSPosition.get(style);
			if (position == PositionValue.ABSOLUTE || position == PositionValue.FIXED
					|| position == PositionValue._CSSJ_CURRENT_PAGE) {
				value = DisplayValue.INLINE_BLOCK_VALUE;
				display = DisplayValue.INLINE_BLOCK;
				break;
			}
			if (CSSFloat.get(style) != CSSFloatValue.NONE) {
				value = DisplayValue.BLOCK_VALUE;
				display = DisplayValue.BLOCK;
			}
		}
			break;
		case DisplayValue.INLINE_BLOCK: {
			final short position = CSSPosition.get(style);
			if (position == PositionValue.ABSOLUTE || position == PositionValue.FIXED
					|| position == PositionValue._CSSJ_CURRENT_PAGE) {
				break;
			}
			if (CSSFloat.get(style) != CSSFloatValue.NONE) {
				value = DisplayValue.BLOCK_VALUE;
				display = DisplayValue.BLOCK;
			}
		}
			break;
		case DisplayValue.TABLE_CAPTION:
			// テーブル外のキャプションはブロック扱い
			final CSSStyle parentStyle = style.getParentStyle();
			if (parentStyle != null) {
				switch (Display.get(parentStyle)) {
				case DisplayValue.TABLE:
				case DisplayValue.TABLE_COLUMN_GROUP:
				case DisplayValue.TABLE_COLUMN:
				case DisplayValue.TABLE_ROW_GROUP:
				case DisplayValue.TABLE_HEADER_GROUP:
				case DisplayValue.TABLE_FOOTER_GROUP:
				case DisplayValue.TABLE_ROW:
					break;
				default:
					value = DisplayValue.BLOCK_VALUE;
					display = DisplayValue.BLOCK;
					break;
				}
			}
		case DisplayValue.RUN_IN:
		case DisplayValue.TABLE_ROW_GROUP:
		case DisplayValue.TABLE_COLUMN:
		case DisplayValue.TABLE_COLUMN_GROUP:
		case DisplayValue.TABLE_HEADER_GROUP:
		case DisplayValue.TABLE_FOOTER_GROUP:
		case DisplayValue.TABLE_ROW:
		case DisplayValue.TABLE_CELL: {
			final short position = CSSPosition.get(style);
			if (CSSFloat.get(style) != CSSFloatValue.NONE
					|| (position != PositionValue.STATIC && position != PositionValue.RELATIVE)) {
				value = DisplayValue.BLOCK_VALUE;
				display = DisplayValue.BLOCK;
			}
		}
			break;

		case DisplayValue.NONE:
		case DisplayValue.BLOCK:
		case DisplayValue.LIST_ITEM:
		case DisplayValue.TABLE:
			break;
		default:
			throw new IllegalStateException();
		}

		// 置換ボックスのための変換
		switch (display) {
		case DisplayValue.INLINE_TABLE:
			if (CSSJInternalImage.getImage(style) != null) {
				return DisplayValue.INLINE_VALUE;
			}
			break;

		case DisplayValue.RUN_IN:
		case DisplayValue.LIST_ITEM:
		case DisplayValue.TABLE:
		case DisplayValue.TABLE_ROW_GROUP:
		case DisplayValue.TABLE_HEADER_GROUP:
		case DisplayValue.TABLE_FOOTER_GROUP:
		case DisplayValue.TABLE_ROW:
		case DisplayValue.TABLE_CELL:
		case DisplayValue.TABLE_CAPTION:
			if (CSSJInternalImage.getImage(style) != null) {
				return DisplayValue.BLOCK_VALUE;
			}
			break;

		case DisplayValue.TABLE_COLUMN:
		case DisplayValue.TABLE_COLUMN_GROUP:
			if (CSSJInternalImage.getImage(style) != null) {
				return DisplayValue.NONE_VALUE;
			}
			break;

		case DisplayValue.INLINE:
		case DisplayValue.NONE:
		case DisplayValue.BLOCK:
		case DisplayValue.INLINE_BLOCK:
			break;
		default:
			throw new IllegalStateException();
		}

		// 縦中横/横中縦のための変換
		if (display == DisplayValue.INLINE) {
			CSSStyle parentStyle = style.getParentStyle();
			if (parentStyle != null && StyleUtils.isVertical(BlockFlow.get(parentStyle)) != StyleUtils
					.isVertical(BlockFlow.get(style))) {
				return DisplayValue.INLINE_BLOCK_VALUE;
			}
		}

		return value;
	}

	public Value getDefault(CSSStyle style) {
		return DisplayValue.INLINE_VALUE;
	}

	public boolean isInherited() {
		return false;
	}

	public Value parseProperty(LexicalUnit lu, UserAgent ua, URI uri) throws PropertyException {
		short luType = lu.getLexicalUnitType();
		switch (luType) {
		case LexicalUnit.SAC_IDENT:
			String ident = lu.getStringValue().toLowerCase();
			if (ident.equals("none")) {
				return DisplayValue.NONE_VALUE;
			} else if (ident.equals("block")) {
				return DisplayValue.BLOCK_VALUE;
			} else if (ident.equals("inline")) {
				return DisplayValue.INLINE_VALUE;
			} else if (ident.equals("inline-block")) {
				return DisplayValue.INLINE_BLOCK_VALUE;
			} else if (ident.equals("list-item")) {
				return DisplayValue.LIST_ITEM_VALUE;
			} else if (ident.equals("run-in")) {
				return DisplayValue.RUN_IN_VALUE;
			} else {
				if (ident.equals("table")) {
					return DisplayValue.TABLE_VALUE;
				} else if (ident.equals("inline-table")) {
					return DisplayValue.INLINE_TABLE_VALUE;
				} else if (ident.equals("table-row-group")) {
					return DisplayValue.TABLE_ROW_GROUP_VALUE;
				} else if (ident.equals("table-column")) {
					return DisplayValue.TABLE_COLUMN_VALUE;
				} else if (ident.equals("table-column-group")) {
					return DisplayValue.TABLE_COLUMN_GROUP_VALUE;
				} else if (ident.equals("table-header-group")) {
					return DisplayValue.TABLE_HEADER_GROUP_VALUE;
				} else if (ident.equals("table-footer-group")) {
					return DisplayValue.TABLE_FOOTER_GROUP_VALUE;
				} else if (ident.equals("table-row")) {
					return DisplayValue.TABLE_ROW_VALUE;
				} else if (ident.equals("table-cell")) {
					return DisplayValue.TABLE_CELL_VALUE;
				} else if (ident.equals("table-caption")) {
					return DisplayValue.TABLE_CAPTION_VALUE;
				}
			}

		default:
			throw new PropertyException();
		}
	}

}