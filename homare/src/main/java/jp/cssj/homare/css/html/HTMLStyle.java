package jp.cssj.homare.css.html;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.cssj.homare.css.CSSElement;
import jp.cssj.homare.css.CSSStyle;
import jp.cssj.homare.css.util.ColorValueUtils;
import jp.cssj.homare.css.util.LengthUtils;
import jp.cssj.homare.css.util.ValueUtils;
import jp.cssj.homare.css.value.AbsoluteLengthValue;
import jp.cssj.homare.css.value.AutoValue;
import jp.cssj.homare.css.value.BackgroundAttachmentValue;
import jp.cssj.homare.css.value.BorderCollapseValue;
import jp.cssj.homare.css.value.BorderStyleValue;
import jp.cssj.homare.css.value.CaptionSideValue;
import jp.cssj.homare.css.value.ClearValue;
import jp.cssj.homare.css.value.ColorValue;
import jp.cssj.homare.css.value.DirectionValue;
import jp.cssj.homare.css.value.DisplayValue;
import jp.cssj.homare.css.value.EmLengthValue;
import jp.cssj.homare.css.value.ExLengthValue;
import jp.cssj.homare.css.value.FontFamilyValue;
import jp.cssj.homare.css.value.FontStyleValue;
import jp.cssj.homare.css.value.FontWeightValue;
import jp.cssj.homare.css.value.LengthValue;
import jp.cssj.homare.css.value.ListStyleTypeValue;
import jp.cssj.homare.css.value.OverflowValue;
import jp.cssj.homare.css.value.PageBreakInsideValue;
import jp.cssj.homare.css.value.PageBreakValue;
import jp.cssj.homare.css.value.PercentageValue;
import jp.cssj.homare.css.value.PositionValue;
import jp.cssj.homare.css.value.QuantityValue;
import jp.cssj.homare.css.value.QuoteValue;
import jp.cssj.homare.css.value.RealValue;
import jp.cssj.homare.css.value.RelativeSizeValue;
import jp.cssj.homare.css.value.StringValue;
import jp.cssj.homare.css.value.TextAlignValue;
import jp.cssj.homare.css.value.TextDecorationValue;
import jp.cssj.homare.css.value.UnicodeBidiValue;
import jp.cssj.homare.css.value.Value;
import jp.cssj.homare.css.value.ValueListValue;
import jp.cssj.homare.css.value.VerticalAlignValue;
import jp.cssj.homare.css.value.WhiteSpaceValue;
import jp.cssj.homare.css.value.ext.CSSJDirectionModeValue;
import jp.cssj.homare.css.value.ext.CSSJRubyValue;
import jp.cssj.homare.css.value.internal.CSSJHtmlAlignValue;
import jp.cssj.homare.css.value.internal.CSSJHtmlTableBorderValue;
import jp.cssj.homare.impl.css.part.BrokenImage;
import jp.cssj.homare.impl.css.part.CheckBoxImage;
import jp.cssj.homare.impl.css.part.NullImage;
import jp.cssj.homare.impl.css.part.RadioButtonImage;
import jp.cssj.homare.impl.css.part.SelectImage;
import jp.cssj.homare.impl.css.part.UnprintBrokenImage;
import jp.cssj.homare.impl.css.property.BackgroundAttachment;
import jp.cssj.homare.impl.css.property.BackgroundColor;
import jp.cssj.homare.impl.css.property.BorderBottomColor;
import jp.cssj.homare.impl.css.property.BorderBottomStyle;
import jp.cssj.homare.impl.css.property.BorderBottomWidth;
import jp.cssj.homare.impl.css.property.BorderCollapse;
import jp.cssj.homare.impl.css.property.BorderLeftColor;
import jp.cssj.homare.impl.css.property.BorderLeftStyle;
import jp.cssj.homare.impl.css.property.BorderLeftWidth;
import jp.cssj.homare.impl.css.property.BorderRightColor;
import jp.cssj.homare.impl.css.property.BorderRightStyle;
import jp.cssj.homare.impl.css.property.BorderRightWidth;
import jp.cssj.homare.impl.css.property.BorderSpacing;
import jp.cssj.homare.impl.css.property.BorderTopColor;
import jp.cssj.homare.impl.css.property.BorderTopStyle;
import jp.cssj.homare.impl.css.property.BorderTopWidth;
import jp.cssj.homare.impl.css.property.CSSColor;
import jp.cssj.homare.impl.css.property.CSSFontFamily;
import jp.cssj.homare.impl.css.property.CSSFontStyle;
import jp.cssj.homare.impl.css.property.CSSPosition;
import jp.cssj.homare.impl.css.property.CaptionSide;
import jp.cssj.homare.impl.css.property.Clear;
import jp.cssj.homare.impl.css.property.Content;
import jp.cssj.homare.impl.css.property.Direction;
import jp.cssj.homare.impl.css.property.Display;
import jp.cssj.homare.impl.css.property.FontSize;
import jp.cssj.homare.impl.css.property.FontWeight;
import jp.cssj.homare.impl.css.property.Height;
import jp.cssj.homare.impl.css.property.LineHeight;
import jp.cssj.homare.impl.css.property.ListStyleType;
import jp.cssj.homare.impl.css.property.MarginBottom;
import jp.cssj.homare.impl.css.property.MarginLeft;
import jp.cssj.homare.impl.css.property.MarginRight;
import jp.cssj.homare.impl.css.property.MarginTop;
import jp.cssj.homare.impl.css.property.MinHeight;
import jp.cssj.homare.impl.css.property.Overflow;
import jp.cssj.homare.impl.css.property.PaddingBottom;
import jp.cssj.homare.impl.css.property.PaddingLeft;
import jp.cssj.homare.impl.css.property.PaddingRight;
import jp.cssj.homare.impl.css.property.PaddingTop;
import jp.cssj.homare.impl.css.property.PageBreakAfter;
import jp.cssj.homare.impl.css.property.PageBreakBefore;
import jp.cssj.homare.impl.css.property.PageBreakInside;
import jp.cssj.homare.impl.css.property.Right;
import jp.cssj.homare.impl.css.property.TextAlign;
import jp.cssj.homare.impl.css.property.TextDecoration;
import jp.cssj.homare.impl.css.property.TextIndent;
import jp.cssj.homare.impl.css.property.Top;
import jp.cssj.homare.impl.css.property.UnicodeBidi;
import jp.cssj.homare.impl.css.property.VerticalAlign;
import jp.cssj.homare.impl.css.property.WhiteSpace;
import jp.cssj.homare.impl.css.property.Width;
import jp.cssj.homare.impl.css.property.css3.BlockFlow;
import jp.cssj.homare.impl.css.property.ext.CSSJDirectionMode;
import jp.cssj.homare.impl.css.property.ext.CSSJRuby;
import jp.cssj.homare.impl.css.property.internal.CSSJAutoWidth;
import jp.cssj.homare.impl.css.property.internal.CSSJHtmlAlign;
import jp.cssj.homare.impl.css.property.internal.CSSJHtmlCellPadding;
import jp.cssj.homare.impl.css.property.internal.CSSJHtmlTableBorder;
import jp.cssj.homare.impl.css.property.internal.CSSJInternalImage;
import jp.cssj.homare.message.MessageCodes;
import jp.cssj.homare.style.util.StyleUtils;
import jp.cssj.homare.ua.DocumentContext;
import jp.cssj.homare.ua.ImageMap;
import jp.cssj.homare.ua.ImageMap.Area;
import jp.cssj.homare.ua.UserAgent;
import jp.cssj.homare.ua.props.OutputBrokenImage;
import jp.cssj.homare.ua.props.OutputPdfVersion;
import jp.cssj.homare.ua.props.UAProps;
import jp.cssj.resolver.Source;
import jp.cssj.resolver.helpers.SourceWrapper;
import jp.cssj.resolver.helpers.URIHelper;
import jp.cssj.sakae.gc.image.Image;
import jp.cssj.sakae.util.NumberUtils;

public class HTMLStyle {
	private static final Logger LOG = Logger.getLogger(HTMLStyle.class.getName());

	private static final ExLengthValue EX_20 = ExLengthValue.create(20);
	private static final EmLengthValue EM_4 = EmLengthValue.create(4);
	private static final EmLengthValue EM_1_12 = EmLengthValue.create(1.12);
	private static final EmLengthValue EM_1 = EmLengthValue.create(1);
	private static final EmLengthValue EM__5 = EmLengthValue.create(.5);
	private static final EmLengthValue _EM_1 = EmLengthValue.create(-1);
	private static final EmLengthValue _EM__9 = EmLengthValue.create(-.9);
	private static final RealValue REAL_1_618 = RealValue.create(1.618);
	private static final RealValue REAL_1_414 = RealValue.create(1.414);
	private static final ValueListValue WBR = new ValueListValue(new Value[] { new StringValue("\u200B") });
	private static final ValueListValue OPEN_QUOTE = new ValueListValue(new Value[] { QuoteValue.OPEN_QUOTE_VALUE });
	private static final ValueListValue CLOSE_QUOTE = new ValueListValue(new Value[] { QuoteValue.CLOSE_QUOTE_VALUE });
	private static final ValueListValue EMPTY = new ValueListValue(new Value[] { new StringValue("") });

	public static void applyAfterStyle(CSSStyle style) {
		// :after
		assert style.getCSSElement() == CSSElement.AFTER;
		CSSElement parentCe = style.getParentStyle().getCSSElement();
		short code = HTMLCodes.code(parentCe);
		switch (code) {
		case HTMLCodes.INPUT:
			// <INPUT>
			byte type = HTMLStyleUtils.getInputType(parentCe.atts.getValue("type"));
			switch (type) {
			case HTMLStyleUtils.INPUT_PASSWORD: {
				String value = parentCe.atts.getValue("value");
				if (value != null) {
					char[] chars = new char[value.length()];
					for (int i = 0; i < chars.length; ++i) {
						chars[i] = '*';
					}
					style.set(Content.INFO, new ValueListValue(new Value[] { new StringValue(new String(chars)+"\u200B") }));
				} else {
					style.set(Content.INFO, WBR);
				}
			}
				break;

			case HTMLStyleUtils.INPUT_FILE: {
				HTMLStyle.applyButton(style, parentCe.atts.getValue("disabled") != null);
				style.set(Content.INFO, new ValueListValue(new Value[] { new StringValue("選択...") }));
			}
				break;

			case HTMLStyleUtils.INPUT_TEXT:
			case HTMLStyleUtils.INPUT_BUTTON:
			case HTMLStyleUtils.INPUT_SUBMIT:
			case HTMLStyleUtils.INPUT_RESET: {
				String value = parentCe.atts.getValue("value");
				if (value != null) {
					style.set(Content.INFO, new ValueListValue(new Value[] { new StringValue(value+"\u200B") }));
				} else {
					style.set(Content.INFO, WBR);
				}
			}
				break;
			}
			break;
		case HTMLCodes.ISINDEX:
			// <ISINDEX>
			HTMLStyle.applyTextField(style, false, null);
			style.set(Content.INFO, WBR);
			break;
		case HTMLCodes.Q: {
			// <Q>
			style.set(Content.INFO, CLOSE_QUOTE);
		}
			break;
		case HTMLCodes.WBR:
			// <WBR>
			style.set(Content.INFO, WBR);
			style.set(WhiteSpace.INFO, WhiteSpaceValue.NORMAL_VALUE);
			break;
		case HTMLCodes.SELECT: {
			// <SELECT>
			UserAgent ua = style.getUserAgent();
			CSSStyle parent = style.getParentStyle();
			double size = LengthUtils.convert(ua, Height.getLength(parent).getLength(), LengthValue.UNIT_PT,
					LengthValue.UNIT_PX);
			style.set(CSSPosition.INFO, PositionValue.ABSOLUTE_VALUE);
			double border = BorderTopWidth.get(parent);
			style.set(Top.INFO, AbsoluteLengthValue.create(ua, -border * 2));
			style.set(Right.INFO, AbsoluteLengthValue.create(ua, -Height.getLength(parent).getLength() - border));
			CSSJInternalImage.setImage(style, new SelectImage(parentCe.atts.getValue("disabled") != null, size));
			style.set(Content.INFO, EMPTY);
		}
			break;
		}
	}

	public static void applyBeforeStyle(CSSStyle style) {
		// :before
		assert style.getCSSElement() == CSSElement.BEFORE;
		CSSElement parentCe = style.getParentStyle().getCSSElement();
		short code = HTMLCodes.code(parentCe);
		switch (code) {
		case HTMLCodes.BUTTON:
			// <BUTTON>
			style.set(Content.INFO, WBR);
			break;
		case HTMLCodes.INPUT:
			// <INPUT>
			byte type = HTMLStyleUtils.getInputType(parentCe.atts.getValue("type"));
			if (type == HTMLStyleUtils.INPUT_FILE) {
				HTMLStyle.applyTextField(style, parentCe.atts.getValue("disabled") != null,
						parentCe.atts.getValue("size"));
				style.set(Content.INFO, WBR);
			}
			break;
		case HTMLCodes.ISINDEX: {
			// <ISINDEX>
			String prompt = parentCe.atts.getValue("prompt");
			if (prompt != null) {
				style.set(Content.INFO, new ValueListValue(new Value[] { new StringValue(prompt) }));
			}
		}
			break;
		case HTMLCodes.Q: {
			// <Q>
			style.set(Content.INFO, OPEN_QUOTE);
		}
			break;
		}
	}

	private static void applyBrokenImage(CSSStyle style, String alt) {
		UserAgent ua = style.getUserAgent();
		int brokenimage = UAProps.OUTPUT_BROKEN_IMAGE.getCode(ua);
		if (brokenimage == OutputBrokenImage.ANNOTATION
				&& UAProps.OUTPUT_PDF_VERSION.getCode(ua) == OutputPdfVersion.V1_4X1) {
			ua.message(MessageCodes.WARN_UNSUPPORTED_PDF_CAPABILITY, UAProps.OUTPUT_BROKEN_IMAGE.name, "annotation",
					"PDF/X-1a");
			brokenimage = OutputBrokenImage.CROSS;
		}

		switch (brokenimage) {
		case OutputBrokenImage.ANNOTATION:
			CSSJInternalImage.setImage(style, new UnprintBrokenImage(ua, alt));
			return;
		case OutputBrokenImage.CROSS:
			CSSJInternalImage.setImage(style, new BrokenImage(ua, alt));
			return;
		case OutputBrokenImage.HIDDEN:
			CSSJInternalImage.setImage(style, new NullImage(alt));
			return;
		case OutputBrokenImage.NONE:
			if (alt != null) {
				CSSJInternalImage.setText(style, alt);
			}
			return;
		default:
			throw new IllegalStateException();
		}
	}

	private static void applyButton(CSSStyle style, boolean disabled) {
		UserAgent ua = style.getUserAgent();
		style.set(Display.INFO, DisplayValue.INLINE_BLOCK_VALUE);
		style.set(Height.INFO, EM_1);
		if (disabled) {
			style.set(CSSColor.INFO, ColorValueUtils.DIMGRAY);
		}
		style.set(TextAlign.INFO, TextAlignValue.CENTER_VALUE);
		style.set(BackgroundColor.INFO, ColorValueUtils.LIGHTGRAY);
		AbsoluteLengthValue thin = ua.getBorderWidth(UserAgent.BORDER_WIDTH_THIN);
		style.set(BorderTopStyle.INFO, BorderStyleValue.OUTSET_VALUE);
		style.set(BorderTopWidth.INFO, thin);
		style.set(BorderLeftStyle.INFO, BorderStyleValue.OUTSET_VALUE);
		style.set(BorderLeftWidth.INFO, thin);
		style.set(BorderBottomStyle.INFO, BorderStyleValue.OUTSET_VALUE);
		style.set(BorderBottomWidth.INFO, thin);
		style.set(BorderRightStyle.INFO, BorderStyleValue.OUTSET_VALUE);
		style.set(BorderRightWidth.INFO, thin);
		style.set(PaddingTop.INFO, thin);
		style.set(PaddingBottom.INFO, thin);
		style.set(PaddingLeft.INFO, thin);
		style.set(PaddingRight.INFO, thin);
		style.set(WhiteSpace.INFO, WhiteSpaceValue.NOWRAP_VALUE);
	}

	private static void applyImage(CSSStyle style, String src, final String type, String alt) {
		if (src != null) {
			final UserAgent ua = style.getUserAgent();
			final URI uri;
			try {
				uri = URIHelper.resolve(ua.getDocumentContext().getEncoding(), ua.getDocumentContext().getBaseURI(),
						src);
				final Source source = ua.resolve(uri);
				try {
					Source wrappedSource = new SourceWrapper(source) {
						public String getMimeType() throws IOException {
							return type == null ? super.getMimeType() : type;
						}
					};
					final Image image = ua.getImage(wrappedSource);
					if (image != null) {
						CSSJInternalImage.setImage(style, image);
						return;
					}
				} finally {
					ua.release(source);
				}
			} catch (Exception e) {
				LOG.log(Level.FINE, "Missing image", e);
				ua.message(MessageCodes.WARN_MISSING_IMAGE, src);
			}
			HTMLStyle.applyBrokenImage(style, alt);
		}
		if (alt != null) {
			style.set(Content.INFO, new ValueListValue(new Value[] { new StringValue(alt) }));
		}
	}

	/**
	 * 段落の前後のマージンを設定します。
	 * 
	 * @param style
	 * @param length
	 */
	private static void applyParagraphMargins(CSSStyle style, LengthValue length, short code) {
		if (style.getParentStyle() == null) {
			return;
		}

		final CSSStyle pStyle = style.getParentStyle();
		if (pStyle != null && ((CSSJDirectionMode.get(pStyle) == CSSJDirectionModeValue.PHYSICAL
				&& StyleUtils.isVertical(BlockFlow.get(pStyle)))
				|| CSSJDirectionMode.get(pStyle) == CSSJDirectionModeValue.VERTICAL_RL)) {
			// 縦書き
			style.set(MarginLeft.INFO, length);
			style.set(MarginRight.INFO, length);
		} else {
			// 横書き
			style.set(MarginTop.INFO, length);
			style.set(MarginBottom.INFO, length);
		}
	}

	/**
	 * テーブルセルのレイアウトを指定します。
	 * 
	 * @param style
	 */
	private static void applyTableCell(String elem, CSSStyle style) {
		UserAgent ua = style.getUserAgent();
		CSSElement ce = style.getCSSElement();
		style.set(Display.INFO, DisplayValue.TABLE_CELL_VALUE);
		style.set(PageBreakInside.INFO, PageBreakInsideValue.AVOID_VALUE);

		style.set(VerticalAlign.INFO, VerticalAlignValue.MIDDLE_VALUE);
		{
			String str = ce.atts.getValue("valign");
			if (str != null) {
				HTMLStyleUtils.applyVAlign(elem, style, str);
			} else {
				CSSStyle parentStyle = style.getParentStyle();
				LOOP: while (parentStyle != null) {
					CSSElement parentCe = parentStyle.getCSSElement();
					switch (HTMLCodes.code(parentCe)) {
					case HTMLCodes.TR:
					case HTMLCodes.THEAD:
					case HTMLCodes.TBODY:
					case HTMLCodes.TFOOT:
					case HTMLCodes.TABLE:
						str = parentCe.atts.getValue("valign");
						if (str == null) {
							break;
						}
						HTMLStyleUtils.applyVAlign(elem, style, str);
						break LOOP;
					}
					parentStyle = parentStyle.getParentStyle();
				}
			}
		}
		HTMLStyleUtils.applyWidthHeight(elem, style);
		HTMLStyleUtils.applyBGColor(elem, style);
		HTMLStyleUtils.applyBackground(elem, style);
		if (ce.atts.getValue("nowrap") != null) {
			style.set(WhiteSpace.INFO, WhiteSpaceValue.NOWRAP_VALUE);
		}
		LengthValue cellpadding = CSSJHtmlCellPadding.get(style);
		style.set(PaddingTop.INFO, cellpadding, CSSStyle.MODE_WEAK);
		style.set(PaddingRight.INFO, cellpadding, CSSStyle.MODE_WEAK);
		style.set(PaddingBottom.INFO, cellpadding, CSSStyle.MODE_WEAK);
		style.set(PaddingLeft.INFO, cellpadding, CSSStyle.MODE_WEAK);
		CSSJHtmlTableBorderValue border = CSSJHtmlTableBorder.get(style);
		if (!border.getWidth().isZero()) {
			ColorValue borderColor = border.getColor();
			BorderStyleValue borderStyle;
			if (borderColor == null) {
				borderStyle = BorderStyleValue.INSET_VALUE;
			} else {
				borderStyle = BorderStyleValue.SOLID_VALUE;
				style.set(BorderTopColor.INFO, borderColor);
				style.set(BorderRightColor.INFO, borderColor);
				style.set(BorderBottomColor.INFO, borderColor);
				style.set(BorderLeftColor.INFO, borderColor);
			}
			LengthValue thin = ua.getBorderWidth(UserAgent.BORDER_WIDTH_THIN);
			style.set(BorderTopStyle.INFO, borderStyle);
			style.set(BorderTopWidth.INFO, thin);
			style.set(BorderRightStyle.INFO, borderStyle);
			style.set(BorderRightWidth.INFO, thin);
			style.set(BorderBottomStyle.INFO, borderStyle);
			style.set(BorderBottomWidth.INFO, thin);
			style.set(BorderLeftStyle.INFO, borderStyle);
			style.set(BorderLeftWidth.INFO, thin);
		}
		HTMLStyleUtils.applyBlockAlign(elem, style);
		CSSStyle parent = style.getParentStyle();
		for (; parent != null; parent = parent.getParentStyle()) {
			CSSElement parentCe = parent.getCSSElement();
			if (HTMLCodes.code(parentCe) == HTMLCodes.TABLE) {
				String rules = parentCe.atts.getValue("rules");
				if (rules != null) {
					if (rules.equalsIgnoreCase("all")) {
						style.set(BorderRightStyle.INFO, BorderStyleValue.SOLID_VALUE);
						style.set(BorderRightWidth.INFO, ua.getBorderWidth(UserAgent.BORDER_WIDTH_THIN));
						style.set(BorderLeftStyle.INFO, BorderStyleValue.SOLID_VALUE);
						style.set(BorderLeftWidth.INFO, ua.getBorderWidth(UserAgent.BORDER_WIDTH_THIN));
						style.set(BorderTopStyle.INFO, BorderStyleValue.SOLID_VALUE);
						style.set(BorderTopWidth.INFO, ua.getBorderWidth(UserAgent.BORDER_WIDTH_THIN));
						style.set(BorderBottomStyle.INFO, BorderStyleValue.SOLID_VALUE);
						style.set(BorderBottomWidth.INFO, ua.getBorderWidth(UserAgent.BORDER_WIDTH_THIN));
					} else if (rules.equalsIgnoreCase("cols")) {
						style.set(BorderRightStyle.INFO, BorderStyleValue.SOLID_VALUE);
						style.set(BorderRightWidth.INFO, ua.getBorderWidth(UserAgent.BORDER_WIDTH_THIN));
						style.set(BorderLeftStyle.INFO, BorderStyleValue.SOLID_VALUE);
						style.set(BorderLeftWidth.INFO, ua.getBorderWidth(UserAgent.BORDER_WIDTH_THIN));
						style.set(BorderTopStyle.INFO, BorderStyleValue.NONE_VALUE);
						style.set(BorderBottomStyle.INFO, BorderStyleValue.NONE_VALUE);
					} else {
						style.set(BorderTopStyle.INFO, BorderStyleValue.NONE_VALUE);
						style.set(BorderBottomStyle.INFO, BorderStyleValue.NONE_VALUE);
						style.set(BorderRightStyle.INFO, BorderStyleValue.NONE_VALUE);
						style.set(BorderLeftStyle.INFO, BorderStyleValue.NONE_VALUE);
					}
				}
				break;
			}
		}
	}

	private static void applyTableColumn(String elem, CSSStyle style) {
		CSSElement ce = style.getCSSElement();
		UserAgent ua = style.getUserAgent();
		HTMLStyleUtils.applyBGColor(elem, style);
		String width = ce.atts.getValue("width");
		if (width != null) {
			try {
				QuantityValue length = HTMLStyleUtils.parseLength(ua, width);
				if (length.isNegative()) {
					throw new NumberFormatException();
				}
				style.set(Width.INFO, length);
			} catch (Exception e) {
				ua.message(MessageCodes.WARN_BAD_HTML_ATTRIBUTE, elem, "width", width);
			}
		}
		HTMLStyleUtils.applyBlockAlign(elem, style);
		HTMLStyleUtils.applyVAlign(elem, style, ce.atts.getValue("valign"));

		LengthValue cellpadding = CSSJHtmlCellPadding.get(style);
		style.set(PaddingTop.INFO, cellpadding, CSSStyle.MODE_WEAK);
		style.set(PaddingRight.INFO, cellpadding, CSSStyle.MODE_WEAK);
		style.set(PaddingBottom.INFO, cellpadding, CSSStyle.MODE_WEAK);
		style.set(PaddingLeft.INFO, cellpadding, CSSStyle.MODE_WEAK);
	}

	private static void applyTableRows(String elem, CSSStyle style) {
		style.set(CSSJHtmlAlign.INFO, CSSJHtmlAlignValue.START_VALUE);
		UserAgent ua = style.getUserAgent();
		HTMLStyleUtils.applyBlockAlign(elem, style);
		HTMLStyleUtils.applyBGColor(elem, style);
		CSSJHtmlTableBorderValue border = CSSJHtmlTableBorder.get(style);
		if (!border.getWidth().isZero()) {
			CSSStyle parent = style.getParentStyle();
			for (; parent != null; parent = parent.getParentStyle()) {
				CSSElement parentCe = parent.getCSSElement();
				if (HTMLCodes.code(parentCe) == HTMLCodes.TABLE) {
					if ("groups".equalsIgnoreCase(parentCe.atts.getValue("rules"))) {
						style.set(BorderTopStyle.INFO, BorderStyleValue.SOLID_VALUE);
						style.set(BorderTopWidth.INFO, ua.getBorderWidth(UserAgent.BORDER_WIDTH_THIN));
						style.set(BorderBottomStyle.INFO, BorderStyleValue.SOLID_VALUE);
						style.set(BorderBottomWidth.INFO, ua.getBorderWidth(UserAgent.BORDER_WIDTH_THIN));
					}
					break;
				}
			}
		}
	}

	private static void applyTextField(CSSStyle style, boolean disabled, String size) {
		UserAgent ua = style.getUserAgent();
		style.set(Display.INFO, DisplayValue.INLINE_BLOCK_VALUE);
		style.set(CSSJAutoWidth.INFO, EX_20);
		if (size != null) {
			try {
				style.set(CSSJAutoWidth.INFO, ExLengthValue.create(NumberUtils.parseDouble(size)));
			} catch (NumberFormatException e) {
				ua.message(MessageCodes.WARN_BAD_HTML_ATTRIBUTE, "INPUT", "size", size);
			}
		}

		style.set(Height.INFO, AutoValue.AUTO_VALUE);
		if (disabled) {
			style.set(CSSColor.INFO, ColorValueUtils.DIMGRAY);
			style.set(BackgroundColor.INFO, ColorValueUtils.LIGHTGRAY);
		} else {
			style.set(BackgroundColor.INFO, ColorValueUtils.WHITE);
		}
		LengthValue thin = ua.getBorderWidth(UserAgent.BORDER_WIDTH_THIN);
		style.set(BorderTopStyle.INFO, BorderStyleValue.INSET_VALUE);
		style.set(BorderTopWidth.INFO, thin);
		style.set(BorderLeftStyle.INFO, BorderStyleValue.INSET_VALUE);
		style.set(BorderLeftWidth.INFO, thin);
		style.set(BorderBottomStyle.INFO, BorderStyleValue.INSET_VALUE);
		style.set(BorderBottomWidth.INFO, thin);
		style.set(BorderRightStyle.INFO, BorderStyleValue.INSET_VALUE);
		style.set(BorderRightWidth.INFO, thin);
		style.set(PaddingTop.INFO, thin);
		style.set(PaddingBottom.INFO, thin);
		style.set(PaddingLeft.INFO, thin);
		style.set(PaddingRight.INFO, thin);
		style.set(WhiteSpace.INFO, WhiteSpaceValue.NOWRAP_VALUE);
	}

	public static boolean hasAfterContent(CSSElement ce) {
		short code = HTMLCodes.code(ce);
		switch (code) {
		case HTMLCodes.INPUT:
		case HTMLCodes.ISINDEX:
		case HTMLCodes.Q:
		case HTMLCodes.WBR:
		case HTMLCodes.SELECT:
			return true;
		}
		return false;
	}

	public static boolean hasBeforeContent(CSSElement ce) {
		short code = HTMLCodes.code(ce);
		switch (code) {
		case HTMLCodes.BUTTON:
		case HTMLCodes.INPUT:
		case HTMLCodes.ISINDEX:
		case HTMLCodes.Q:
			return true;
		}
		return false;
	}

	private ColorValue linkColor = null;

	private ImageMap imageMap = null;

	public void applyStyle(CSSStyle style) {
		UserAgent ua = style.getUserAgent();
		CSSElement ce = style.getCSSElement();
		assert ce != CSSElement.BEFORE && ce != CSSElement.AFTER;

		// @dir
		{
			String dir = ce.atts.getValue("dir");
			if (dir != null) {
				if (dir.equalsIgnoreCase("ltr")) {
					style.set(UnicodeBidi.INFO, UnicodeBidiValue.EMBED_VALUE);
					style.set(Direction.INFO, DirectionValue.LTR_VALUE);
				} else if (dir.equalsIgnoreCase("rtl")) {
					style.set(UnicodeBidi.INFO, UnicodeBidiValue.EMBED_VALUE);
					style.set(Direction.INFO, DirectionValue.RTL_VALUE);
				} else {
					ua.message(MessageCodes.WARN_BAD_HTML_ATTRIBUTE, "*", "dir", dir);
				}
			}
		}

		short code = HTMLCodes.code(ce);
		switch (code) {
		case HTMLCodes.A: {
			// <A>
			if (ce.isPseudoClass(CSSElement.PC_LINK)) {
				// A:link
				style.set(TextDecoration.INFO, TextDecorationValue.create(TextDecorationValue.UNDERLINE));
				if (this.linkColor == null) {
					this.linkColor = ColorValueUtils.BLUE;
				}
				style.set(CSSColor.INFO, linkColor);
			}
		}
			break;
		case HTMLCodes.ABBR: {
			// <ABBR> ignore
		}
			break;
		case HTMLCodes.ACRONYM: {
			// <ACRONYM> ignore
		}
			break;
		case HTMLCodes.ADDRESS: {
			// <ADDRESS>
			style.set(Display.INFO, DisplayValue.BLOCK_VALUE);
			style.set(CSSFontStyle.INFO, FontStyleValue.ITALIC_VALUE);
		}
			break;
		case HTMLCodes.APPLET: {
			// <APPLET width height hspace vspace alt align>
			HTMLStyleUtils.applyWidthHeight("APPLET", style);
			HTMLStyleUtils.applyHSpaceVSpace("APPLET", style);
			HTMLStyleUtils.applyImageAlign("APPLET", style);
			style.set(Display.INFO, DisplayValue.INLINE_BLOCK_VALUE);
			HTMLStyle.applyBrokenImage(style, ce.atts.getValue("alt"));
		}
			break;
		case HTMLCodes.AREA: {
			// <AREA href shape coords>
			style.set(Display.INFO, DisplayValue.INLINE_VALUE);
			if (this.imageMap == null) {
				break;
			}
			String href = ce.atts.getValue("href");
			if (href == null) {
				break;
			}
			String shape = ce.atts.getValue("shape");
			String coords = ce.atts.getValue("coords");
			Shape realShape = null;
			if (shape == null || shape.equalsIgnoreCase("default") || coords == null) {
				realShape = null;
			} else {
				shape = shape.toLowerCase();
				String[] coordsArray = coords.split(",");
				double[] realCoords = new double[coordsArray.length];
				for (int i = 0; i < realCoords.length; ++i) {
					try {
						realCoords[i] = Double.parseDouble(coordsArray[i].trim());
					} catch (NumberFormatException e) {
						ua.message(MessageCodes.WARN_BAD_HTML_ATTRIBUTE, "AREA", "coords", coords);
						realCoords[i] = 0;
					}
				}
				try {
					if (shape.startsWith("circ")) {
						realShape = new Ellipse2D.Double(realCoords[0] - realCoords[2] / 2,
								realCoords[1] - realCoords[2] / 2, realCoords[2] * 2, realCoords[2] * 2);
					} else if (shape.startsWith("rect")) {
						realShape = new Rectangle2D.Double(realCoords[0], realCoords[1], realCoords[2] - realCoords[0],
								realCoords[3] - realCoords[1]);
					} else if (shape.startsWith("poly")) {
						Path2D.Double path = new Path2D.Double();
						path.moveTo(realCoords[0], realCoords[1]);
						for (int i = 2; i < realCoords.length; i += 2) {
							path.lineTo(realCoords[i], realCoords[i + 1]);
						}
						path.closePath();
						realShape = path;
					} else {
						ua.message(MessageCodes.WARN_BAD_HTML_ATTRIBUTE, "AREA", "shape", shape);
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					ua.message(MessageCodes.WARN_BAD_HTML_ATTRIBUTE, "AREA", "coords", coords);
				}
			}
			try {
				Area area = new Area(realShape, URIHelper.resolve(ua.getDocumentContext().getEncoding(),
						ua.getDocumentContext().getBaseURI(), href));
				this.imageMap.add(area);
			} catch (URISyntaxException e) {
				ua.message(MessageCodes.WARN_BAD_HTML_ATTRIBUTE, "AREA", "href", shape);
			}
		}
			break;
		case HTMLCodes.B: {
			// <B>
			style.set(FontWeight.INFO, FontWeightValue.BOLDER_VALUE);
		}
			break;
		case HTMLCodes.BASE: {
			// <BASE>
			style.set(Display.INFO, DisplayValue.NONE_VALUE);
		}
			break;
		case HTMLCodes.BASEFONT: {
			// <BASEFONT size color face>
			HTMLStyleUtils.applyFontSize("BASEFONT", style);
			HTMLStyleUtils.applyFontFace(style);
			HTMLStyleUtils.applyFontColor("BASEFONT", style);
		}
			break;
		case HTMLCodes.BGSOUND: {
			// <BGSOUND>
			style.set(Display.INFO, DisplayValue.NONE_VALUE);
		}
			break;
		case HTMLCodes.BDO: {
			// <BDO dir>
			String dir = ce.atts.getValue("dir");
			if (dir != null) {
				if (dir.equalsIgnoreCase("ltr")) {
					style.set(UnicodeBidi.INFO, UnicodeBidiValue.BIDI_OVERRIDE_VALUE);
					style.set(Direction.INFO, DirectionValue.LTR_VALUE);
				} else if (dir.equalsIgnoreCase("rtl")) {
					style.set(UnicodeBidi.INFO, UnicodeBidiValue.BIDI_OVERRIDE_VALUE);
					style.set(Direction.INFO, DirectionValue.RTL_VALUE);
				} else {
					ua.message(MessageCodes.WARN_BAD_HTML_ATTRIBUTE, "BDO", "dir", dir);
				}
			}
		}
			break;
		case HTMLCodes.BIG: {
			// <BIG>
			style.set(FontSize.INFO, RelativeSizeValue.LARGER_VALUE);
		}
			break;
		case HTMLCodes.BLINK: {
			// <BLINK>
			style.set(TextDecoration.INFO, TextDecorationValue.create(TextDecorationValue.BLINK));
		}
			break;
		case HTMLCodes.BLOCKQUOTE: {
			// <BLOCKQUOTE>
			style.set(Display.INFO, DisplayValue.BLOCK_VALUE);
			HTMLStyle.applyParagraphMargins(style, EM_1_12, code);
			HTMLStyleUtils.applyQuoteMargins(style, AbsoluteLengthValue.create(ua, 40, AbsoluteLengthValue.UNIT_PX));
		}
			break;
		case HTMLCodes.BODY: {
			// <BODY
			// marginwidth marginheight
			// topmargin leftmargin rightmargin bottommargin
			// bgcolor background bgproperties -scroll
			// text link -vlink -alink>
			style.set(Display.INFO, DisplayValue.BLOCK_VALUE);
			AbsoluteLengthValue px8 = AbsoluteLengthValue.create(ua, 8, AbsoluteLengthValue.UNIT_PX);
			style.set(MarginTop.INFO, px8);
			style.set(MarginRight.INFO, px8);
			style.set(MarginBottom.INFO, px8);
			style.set(MarginLeft.INFO, px8);
			HTMLStyleUtils.applyMarginWidthMarginHeight("BODY", style);
			{
				String str = ce.atts.getValue("topmargin");
				if (str != null) {
					try {
						Value length = HTMLStyleUtils.parseLength(ua, str);
						style.set(MarginTop.INFO, length);
					} catch (Exception e) {
						ua.message(MessageCodes.WARN_BAD_HTML_ATTRIBUTE, "BODY", "topmargin", str);
					}
				}
			}
			{
				String str = ce.atts.getValue("rightmargin");
				if (str != null) {
					try {
						Value length = HTMLStyleUtils.parseLength(ua, str);
						style.set(MarginRight.INFO, length);
					} catch (Exception e) {
						ua.message(MessageCodes.WARN_BAD_HTML_ATTRIBUTE, "BODY", "rightmargin", str);
					}
				}
			}
			{
				String str = ce.atts.getValue("leftmargin");
				if (str != null) {
					try {
						Value length = HTMLStyleUtils.parseLength(ua, str);
						style.set(MarginLeft.INFO, length);
					} catch (Exception e) {
						ua.message(MessageCodes.WARN_BAD_HTML_ATTRIBUTE, "BODY", "leftmargin", str);
					}
				}
			}
			{
				String str = ce.atts.getValue("bottommargin");
				if (str != null) {
					try {
						Value length = HTMLStyleUtils.parseLength(ua, str);
						style.set(MarginBottom.INFO, length);
					} catch (Exception e) {
						ua.message(MessageCodes.WARN_BAD_HTML_ATTRIBUTE, "BODY", "bottommargin", str);
					}
				}
			}
			HTMLStyleUtils.applyBGColor("BODY", style);
			HTMLStyleUtils.applyBackground("BODY", style);
			{
				String str = ce.atts.getValue("bgproperties");
				if (str != null && str.equalsIgnoreCase("fixed")) {
					style.set(BackgroundAttachment.INFO, BackgroundAttachmentValue.FIXED_VALUE);
				}
			}
			{
				String str = ce.atts.getValue("link");
				if (str != null) {
					this.linkColor = HTMLStyleUtils.parseColor(str);
					if (this.linkColor == null) {
						ua.message(MessageCodes.WARN_BAD_HTML_ATTRIBUTE, "BODY", "link", str);
					}
				}
			}
			{
				String str = ce.atts.getValue("text");
				if (str != null) {
					ColorValue color = HTMLStyleUtils.parseColor(str);
					style.set(CSSColor.INFO, color);
					if (color == null) {
						ua.message(MessageCodes.WARN_BAD_HTML_ATTRIBUTE, "BODY", "text" + str);
					}
				}
			}
		}
			break;
		case HTMLCodes.BR: {
			// <BR clear>
			String clear = ce.atts.getValue("clear");
			if (clear != null) {
				if (clear.equalsIgnoreCase("all") || clear.equalsIgnoreCase("both")) {
					style.set(Clear.INFO, ClearValue.BOTH_VALUE);
				} else if (clear.equalsIgnoreCase("left")) {
					style.set(Clear.INFO, ClearValue.LEFT_VALUE);
				} else if (clear.equalsIgnoreCase("right")) {
					style.set(Clear.INFO, ClearValue.RIGHT_VALUE);
				}
			}
		}
			break;
		case HTMLCodes.BUTTON: {
			// <BUTTON disabled>
			style.set(FontSize.INFO, AbsoluteLengthValue.create(ua, ua.getFontSize(UserAgent.FONT_SIZE_MEDIUM)));
			HTMLStyle.applyButton(style, ce.atts.getValue("disabled") != null);
		}
			break;
		case HTMLCodes.CAPTION: {
			// <CAPTION align valign>
			style.set(Display.INFO, DisplayValue.TABLE_CAPTION_VALUE);
			style.set(TextAlign.INFO, TextAlignValue.CENTER_VALUE);
			String align = ce.atts.getValue("align");
			if (align == null) {
				align = ce.atts.getValue("valign");
			}
			if (align != null && align.equals("bottom")) {
				style.set(CaptionSide.INFO, CaptionSideValue.BOTTOM_VALUE);
				style.set(PageBreakBefore.INFO, PageBreakValue.AVOID_VALUE);
			} else {
				style.set(PageBreakAfter.INFO, PageBreakValue.AVOID_VALUE);
			}
		}
			break;
		case HTMLCodes.CENTER: {
			// <CENTER>
			style.set(Display.INFO, DisplayValue.BLOCK_VALUE);
			style.set(TextAlign.INFO, TextAlignValue.CENTER_VALUE);
			style.set(CSSJHtmlAlign.INFO, CSSJHtmlAlignValue.CENTER_VALUE);
		}
			break;
		case HTMLCodes.CITE: {
			// <CITE>
			style.set(CSSFontStyle.INFO, FontStyleValue.ITALIC_VALUE);
		}
			break;
		case HTMLCodes.CODE: {
			// <CODE>
			style.set(CSSFontFamily.INFO, FontFamilyValue.MONOSPACE);
		}
			break;
		case HTMLCodes.COLGROUP: {
			// <COLGROUP align bgcolor -charoff span valign width>
			CSSStyle parent = style.getParentStyle();
			for (; parent != null; parent = parent.getParentStyle()) {
				CSSElement parentCe = parent.getCSSElement();
				CSSJHtmlTableBorderValue border = CSSJHtmlTableBorder.get(style);
				if (!border.getWidth().isZero()) {

					if (HTMLCodes.code(parentCe) == HTMLCodes.TABLE) {
						if ("groups".equalsIgnoreCase(parentCe.atts.getValue("rules"))) {
							style.set(BorderRightStyle.INFO, BorderStyleValue.SOLID_VALUE);
							style.set(BorderRightWidth.INFO, ua.getBorderWidth(UserAgent.BORDER_WIDTH_THIN));
							style.set(BorderLeftStyle.INFO, BorderStyleValue.SOLID_VALUE);
							style.set(BorderLeftWidth.INFO, ua.getBorderWidth(UserAgent.BORDER_WIDTH_THIN));
						}
						break;
					}
				}
			}
			style.set(Display.INFO, DisplayValue.TABLE_COLUMN_GROUP_VALUE);
			applyTableColumn("COLGROUP", style);
		}
			break;
		case HTMLCodes.COL: {
			// <COL align bgcolor -charoff span valign width>
			style.set(Display.INFO, DisplayValue.TABLE_COLUMN_VALUE);
			applyTableColumn("COL", style);
		}
			break;
		case HTMLCodes.COMMENT: {
			// <COMMENT>
			style.set(Display.INFO, DisplayValue.NONE_VALUE);
		}
			break;
		case HTMLCodes.DD: {
			// <DD>
			style.set(Display.INFO, DisplayValue.BLOCK_VALUE);
			style.set(PageBreakBefore.INFO, PageBreakValue.AVOID_VALUE);
			HTMLStyleUtils.applyListMargins(style, AbsoluteLengthValue.create(ua, 40, AbsoluteLengthValue.UNIT_PX));
		}
			break;
		case HTMLCodes.DEL: {
			// <DEL>
			style.set(TextDecoration.INFO, TextDecorationValue.create(TextDecorationValue.LINE_THROUGH));
		}
			break;
		case HTMLCodes.DFN: {
			// <DFN>
		}
			break;
		case HTMLCodes.DIR: {
			// <DIR type -compact>
			style.set(Display.INFO, DisplayValue.BLOCK_VALUE);
			HTMLStyle.applyParagraphMargins(style, EM_1_12, code);
			HTMLStyleUtils.applyListMargins(style, AbsoluteLengthValue.create(ua, 40, AbsoluteLengthValue.UNIT_PX));
			String type = ce.atts.getValue("type");
			if (type != null) {
				Value value = HTMLStyleUtils.toListStyleType(type);
				if (value != null) {
					style.set(ListStyleType.INFO, value);
				} else {
					ua.message(MessageCodes.WARN_BAD_HTML_ATTRIBUTE, "DIR", "type", type);
				}
			}
		}
			break;
		case HTMLCodes.DIV: {
			// <DIV align>
			style.set(Display.INFO, DisplayValue.BLOCK_VALUE);
			HTMLStyleUtils.applyBlockAlign("DIV", style);
		}
			break;
		case HTMLCodes.DL: {
			// <DL -compact>
			style.set(Display.INFO, DisplayValue.BLOCK_VALUE);
			HTMLStyle.applyParagraphMargins(style, EM_1_12, code);
			style.set(PageBreakBefore.INFO, PageBreakValue.AVOID_VALUE);
		}
			break;
		case HTMLCodes.DT: {
			// <DT>
			style.set(Display.INFO, DisplayValue.BLOCK_VALUE);
			style.set(PageBreakAfter.INFO, PageBreakValue.AVOID_VALUE);
		}
			break;
		case HTMLCodes.EM: {
			// <EM>
			style.set(CSSFontStyle.INFO, FontStyleValue.ITALIC_VALUE);
		}
			break;
		case HTMLCodes.EMBED: {
			// <EMBED border
			// width height type
			// hspace vspace
			// alt -hidden -frameborder -units>
			HTMLStyleUtils.applyWidthHeight("EMBED", style);
			HTMLStyleUtils.applyHSpaceVSpace("EMBED", style);
			HTMLStyleUtils.applyImageBorder("EMBED", style);
			String src = ce.atts.getValue("src");
			String type = ce.atts.getValue("type");
			String alt = ce.atts.getValue("alt");
			HTMLStyle.applyImage(style, src, type, alt);
		}
			break;
		case HTMLCodes.FIELDSET: {
			// <FIELDSET align>
			style.set(Display.INFO, DisplayValue.BLOCK_VALUE);
			HTMLStyle.applyParagraphMargins(style, EM_1_12, code);
			style.set(PaddingTop.INFO, EM__5);
			style.set(PaddingRight.INFO, EM__5);
			style.set(PaddingBottom.INFO, EM__5);
			style.set(PaddingLeft.INFO, EM__5);
			HTMLStyleUtils.applyBlockAlign("FIELDSET", style);
			LengthValue thin = ua.getBorderWidth(UserAgent.BORDER_WIDTH_THIN);
			style.set(BorderTopStyle.INFO, BorderStyleValue.GROOVE_VALUE);
			style.set(BorderTopWidth.INFO, thin);
			style.set(BorderRightStyle.INFO, BorderStyleValue.GROOVE_VALUE);
			style.set(BorderRightWidth.INFO, thin);
			style.set(BorderBottomStyle.INFO, BorderStyleValue.GROOVE_VALUE);
			style.set(BorderBottomWidth.INFO, thin);
			style.set(BorderLeftStyle.INFO, BorderStyleValue.GROOVE_VALUE);
			style.set(BorderLeftWidth.INFO, thin);
		}
			break;
		case HTMLCodes.FONT: {
			// <FONT size color face font-weight point-size>
			HTMLStyleUtils.applyFontSize("FONT", style);
			HTMLStyleUtils.applyFontColor("FONT", style);
			HTMLStyleUtils.applyFontFace(style);
			{
				String str = ce.atts.getValue("font-weight");
				if (str != null) {
					try {
						int fontWeight = Integer.parseInt(str);
						fontWeight = Math.max(100, fontWeight);
						fontWeight = Math.min(900, fontWeight);
						style.set(FontWeight.INFO, FontWeightValue.create(fontWeight));
					} catch (NumberFormatException e) {
						ua.message(MessageCodes.WARN_BAD_HTML_ATTRIBUTE, "FONT", "font-weight", str);

					}
				}
			}
		}
			break;
		case HTMLCodes.FORM: {
			// <FORM>
			style.set(Display.INFO, DisplayValue.BLOCK_VALUE);
		}
			break;
		case HTMLCodes.FRAME: {
			// <FRAME>
			style.set(Display.INFO, DisplayValue.NONE_VALUE);
		}
			break;
		case HTMLCodes.FRAMESET: {
			// <FRAMESET>
			style.set(Display.INFO, DisplayValue.NONE_VALUE);
		}
			break;
		case HTMLCodes.H1: {
			// <H1 align>
			style.set(Display.INFO, DisplayValue.BLOCK_VALUE);
			style.set(FontWeight.INFO, FontWeightValue.BOLDER_VALUE);
			style.set(FontSize.INFO, EmLengthValue.create(2));
			style.set(PageBreakAfter.INFO, PageBreakValue.AVOID_VALUE);
			HTMLStyle.applyParagraphMargins(style, EmLengthValue.create(0.67), code);
			HTMLStyleUtils.applyBlockAlign("H1", style);
		}
			break;
		case HTMLCodes.H2: {
			// <H2 align>
			style.set(Display.INFO, DisplayValue.BLOCK_VALUE);
			style.set(FontWeight.INFO, FontWeightValue.BOLDER_VALUE);
			style.set(FontSize.INFO, EmLengthValue.create(1.5));
			style.set(PageBreakAfter.INFO, PageBreakValue.AVOID_VALUE);
			HTMLStyle.applyParagraphMargins(style, EmLengthValue.create(0.75), code);
			HTMLStyleUtils.applyBlockAlign("H2", style);
		}
			break;
		case HTMLCodes.H3: {
			// <H3 align>
			style.set(Display.INFO, DisplayValue.BLOCK_VALUE);
			style.set(FontWeight.INFO, FontWeightValue.BOLDER_VALUE);
			style.set(FontSize.INFO, EmLengthValue.create(1.17));
			style.set(PageBreakAfter.INFO, PageBreakValue.AVOID_VALUE);
			HTMLStyle.applyParagraphMargins(style, EmLengthValue.create(0.83), code);
			HTMLStyleUtils.applyBlockAlign("H3", style);
		}
			break;
		case HTMLCodes.H4: {
			// <H4 align>
			style.set(Display.INFO, DisplayValue.BLOCK_VALUE);
			style.set(FontWeight.INFO, FontWeightValue.BOLDER_VALUE);
			style.set(PageBreakAfter.INFO, PageBreakValue.AVOID_VALUE);
			HTMLStyle.applyParagraphMargins(style, EM_1_12, code);
			HTMLStyleUtils.applyBlockAlign("H4", style);
		}
			break;
		case HTMLCodes.H5: {
			// <H5 align>
			style.set(Display.INFO, DisplayValue.BLOCK_VALUE);
			style.set(FontWeight.INFO, FontWeightValue.BOLDER_VALUE);
			style.set(PageBreakAfter.INFO, PageBreakValue.AVOID_VALUE);
			style.set(FontSize.INFO, EmLengthValue.create(0.83));
			HTMLStyle.applyParagraphMargins(style, EmLengthValue.create(1.5), code);
			HTMLStyleUtils.applyBlockAlign("H5", style);
		}
			break;
		case HTMLCodes.H6: {
			// <H6 align>
			style.set(Display.INFO, DisplayValue.BLOCK_VALUE);
			style.set(FontWeight.INFO, FontWeightValue.BOLDER_VALUE);
			style.set(FontSize.INFO, EmLengthValue.create(0.75));
			style.set(PageBreakAfter.INFO, PageBreakValue.AVOID_VALUE);
			HTMLStyle.applyParagraphMargins(style, EmLengthValue.create(1.67), code);
			HTMLStyleUtils.applyBlockAlign("H6", style);
		}
			break;
		case HTMLCodes.HEAD: {
			// <HEAD>
			style.set(Display.INFO, DisplayValue.NONE_VALUE);
		}
			break;
		case HTMLCodes.HR: {
			// <HR align color noshade size width>
			style.set(Display.INFO, DisplayValue.BLOCK_VALUE);
			LengthValue margin = EmLengthValue.create(.5);
			HTMLStyle.applyParagraphMargins(style, margin, code);

			ColorValue color = null;
			{
				String str = ce.atts.getValue("color");
				if (str != null) {
					color = HTMLStyleUtils.parseColor(str);
					if (color == null) {
						ua.message(MessageCodes.WARN_BAD_HTML_ATTRIBUTE, "HR", "color", str);
					}
				}
			}

			LengthValue size = null;
			{
				String str = ce.atts.getValue("size");
				if (str != null) {
					size = ValueUtils.toLength(ua, true, str);
					if (size == null) {
						ua.message(MessageCodes.WARN_BAD_HTML_ATTRIBUTE, "HR", "size", str);
					}
				}
			}

			QuantityValue width = null;
			{
				String str = ce.atts.getValue("width");
				if (str != null) {
					try {
						width = HTMLStyleUtils.parseLength(ua, str);
						if (width.isNegative()) {
							throw new NumberFormatException();
						}
					} catch (Exception e) {
						ua.message(MessageCodes.WARN_BAD_HTML_ATTRIBUTE, "HR", "width", str);
					}
				}
			}

			final CSSStyle pStyle = style.getParentStyle();
			if (ce.atts.getValue("noshade") == null && color == null) {
				LengthValue border = AbsoluteLengthValue.create(ua, 1, LengthValue.UNIT_PX);
				style.set(BorderTopStyle.INFO, BorderStyleValue.INSET_VALUE);
				style.set(BorderTopWidth.INFO, border);
				style.set(BorderRightStyle.INFO, BorderStyleValue.INSET_VALUE);
				style.set(BorderRightWidth.INFO, border);
				style.set(BorderBottomStyle.INFO, BorderStyleValue.INSET_VALUE);
				style.set(BorderBottomWidth.INFO, border);
				style.set(BorderLeftStyle.INFO, BorderStyleValue.INSET_VALUE);
				style.set(BorderLeftWidth.INFO, border);

				if (pStyle != null && ((CSSJDirectionMode.get(pStyle) == CSSJDirectionModeValue.PHYSICAL
						&& StyleUtils.isVertical(BlockFlow.get(pStyle)))
						|| CSSJDirectionMode.get(pStyle) == CSSJDirectionModeValue.VERTICAL_RL)) {
					// 縦書き
					if (size != null) {
						style.set(Width.INFO, size);
					}
					if (width != null) {
						style.set(Height.INFO, width);
					}
				} else {
					// 横書き
					if (size != null) {
						style.set(Height.INFO, size);
					}
					if (width != null) {
						style.set(Width.INFO, width);
					}
				}
			} else {
				if (pStyle != null && ((CSSJDirectionMode.get(pStyle) == CSSJDirectionModeValue.PHYSICAL
						&& StyleUtils.isVertical(BlockFlow.get(pStyle)))
						|| CSSJDirectionMode.get(pStyle) == CSSJDirectionModeValue.VERTICAL_RL)) {
					// 縦書き
					style.set(BorderLeftStyle.INFO, BorderStyleValue.SOLID_VALUE);
					if (size != null) {
						style.set(BorderLeftWidth.INFO, size);
					}
					if (color != null) {
						style.set(BorderLeftColor.INFO, color);
					}
					if (width != null) {
						style.set(Height.INFO, width);
					}
				} else {
					// 横書き
					style.set(BorderBottomStyle.INFO, BorderStyleValue.SOLID_VALUE);
					if (size != null) {
						style.set(BorderBottomWidth.INFO, size);
					}
					if (color != null) {
						style.set(BorderBottomColor.INFO, color);
					}
					if (width != null) {
						style.set(Width.INFO, width);
					}
				}
			}

			String align = ce.atts.getValue("align");
			if ("right".equalsIgnoreCase(align)) {
				style.set(MarginLeft.INFO, AutoValue.AUTO_VALUE);
			} else if ("left".equalsIgnoreCase(align)) {
				style.set(MarginRight.INFO, AutoValue.AUTO_VALUE);
			} else {
				style.set(MarginLeft.INFO, AutoValue.AUTO_VALUE);
				style.set(MarginRight.INFO, AutoValue.AUTO_VALUE);
			}
		}
			break;
		case HTMLCodes.HTML: {
			// <HTML>
			style.set(Display.INFO, DisplayValue.BLOCK_VALUE);
		}
			break;
		case HTMLCodes.I: {
			// <I>
			style.set(CSSFontStyle.INFO, FontStyleValue.ITALIC_VALUE);
		}
			break;
		case HTMLCodes.IFRAME: {
			// <IFRAME>
			style.set(Display.INFO, DisplayValue.INLINE_BLOCK_VALUE);
			HTMLStyleUtils.applyWidthHeight("IFRAME", style);
			HTMLStyleUtils.applyHSpaceVSpace("IFRAME", style);
			HTMLStyleUtils.applyImageAlign("IFRAME", style);
			HTMLStyleUtils.applyMarginWidthMarginHeight("IFRAME", style);
			boolean border = true;
			{
				String str = ce.atts.getValue("frameborder");
				if (str != null) {
					str = str.trim().toLowerCase();
					try {
						if (str.equals("1") || str.equals("yes")) {
							border = true;
						} else if (str.equals("0") || str.equals("no")) {
							border = false;
						}
					} catch (Exception e) {
						ua.message(MessageCodes.WARN_BAD_HTML_ATTRIBUTE, "IFRAME", "frameborder", str);
					}
				}
			}
			if (border) {
				LengthValue medium = ua.getBorderWidth(UserAgent.BORDER_WIDTH_MEDIUM);
				style.set(BorderTopStyle.INFO, BorderStyleValue.INSET_VALUE);
				style.set(BorderTopWidth.INFO, medium);
				style.set(BorderLeftStyle.INFO, BorderStyleValue.INSET_VALUE);
				style.set(BorderLeftWidth.INFO, medium);
				style.set(BorderBottomStyle.INFO, BorderStyleValue.INSET_VALUE);
				style.set(BorderBottomWidth.INFO, medium);
				style.set(BorderRightStyle.INFO, BorderStyleValue.INSET_VALUE);
				style.set(BorderRightWidth.INFO, medium);
			}
		}
			break;
		case HTMLCodes.IMG: {
			// <IMG src alt border width height hspace vspace align usemap>
			style.set(Display.INFO, DisplayValue.INLINE_VALUE);
			HTMLStyleUtils.applyWidthHeight("IMG", style);
			HTMLStyleUtils.applyHSpaceVSpace("IMG", style);
			HTMLStyleUtils.applyImageAlign("IMG", style);
			String src = ce.atts.getValue("src");
			String alt = ce.atts.getValue("alt");
			HTMLStyle.applyImage(style, src, null, alt);
			HTMLStyleUtils.applyImageBorder("IMG", style);
		}
			break;
		case HTMLCodes.INPUT: {
			// <INPUT type disabled size src border width height align>
			style.set(FontSize.INFO, AbsoluteLengthValue.create(ua, ua.getFontSize(UserAgent.FONT_SIZE_MEDIUM)));
			byte type = HTMLStyleUtils.getInputType(ce.atts.getValue("type"));
			switch (type) {
			case HTMLStyleUtils.INPUT_BUTTON:
			case HTMLStyleUtils.INPUT_RESET:
			case HTMLStyleUtils.INPUT_SUBMIT: {
				HTMLStyle.applyButton(style, ce.atts.getValue("disabled") != null);
				HTMLStyleUtils.applyImageAlign("INPUT", style);
			}
				break;
			case HTMLStyleUtils.INPUT_IMAGE: {
				HTMLStyleUtils.applyWidthHeight("INPUT", style);
				String src = ce.atts.getValue("src");
				String alt = ce.atts.getValue("alt");
				HTMLStyle.applyImage(style, src, null, alt);
				HTMLStyleUtils.applyImageAlign("INPUT", style);
				HTMLStyleUtils.applyImageBorder("INPUT", style);
			}
				break;
			case HTMLStyleUtils.INPUT_HIDDEN: {
				style.set(Display.INFO, DisplayValue.NONE_VALUE);
			}
				break;
			case HTMLStyleUtils.INPUT_CHECKBOX: {
				CSSJInternalImage.setImage(style,
						new CheckBoxImage(ce.atts.getValue("checked") != null, ce.atts.getValue("disabled") != null));
				HTMLStyleUtils.applyImageAlign("INPUT", style);
			}
				break;
			case HTMLStyleUtils.INPUT_RADIO: {
				CSSJInternalImage.setImage(style, new RadioButtonImage(ce.atts.getValue("checked") != null,
						ce.atts.getValue("disabled") != null));
				HTMLStyleUtils.applyImageAlign("INPUT", style);
			}
				break;
			case HTMLStyleUtils.INPUT_TEXT:
			case HTMLStyleUtils.INPUT_PASSWORD: {
				HTMLStyle.applyTextField(style, ce.atts.getValue("disabled") != null, ce.atts.getValue("size"));
				HTMLStyleUtils.applyImageAlign("INPUT", style);
			}
				break;
			case HTMLStyleUtils.INPUT_FILE:
				break;
			default:
				throw new IllegalStateException();
			}
		}
			break;
		case HTMLCodes.INS: {
			// <INS>
			style.set(TextDecoration.INFO, TextDecorationValue.create(TextDecorationValue.UNDERLINE));
		}
			break;
		case HTMLCodes.ISINDEX: {
			// <ISINDEX prompt>
		}
			break;
		case HTMLCodes.KBD: {
			// <KBD>
			style.set(CSSFontFamily.INFO, FontFamilyValue.MONOSPACE);
		}
			break;
		case HTMLCodes.KEYGEN: {
			// <KEYGEN>
		}
			break;
		case HTMLCodes.LABEL: {
			// <LABEL>
		}
			break;
		case HTMLCodes.LEGEND: {
			// <LEGEND>
			style.set(CSSPosition.INFO, PositionValue.ABSOLUTE_VALUE);
			style.set(MarginTop.INFO, _EM_1);
			CSSStyle parent = style;
			for (;;) {
				Value color = parent.get(BackgroundColor.INFO);
				if (color.getValueType() != Value.TYPE_TRANSPARENT) {
					style.set(BackgroundColor.INFO, color);
					break;
				}
				parent = parent.getParentStyle();
				if (parent == null) {
					style.set(BackgroundColor.INFO, ua.getMatColor());
					break;
				}
			}
		}
			break;
		case HTMLCodes.LI: {
			// <LI type value>
			// valueはStyleBuilderで処理
			style.set(Display.INFO, DisplayValue.LIST_ITEM_VALUE);
			String type = ce.atts.getValue("type");
			if (type != null) {
				Value value = HTMLStyleUtils.toListStyleType(type);
				if (value != null) {
					style.set(ListStyleType.INFO, value);
				} else {
					ua.message(MessageCodes.WARN_BAD_HTML_ATTRIBUTE, "LI", "type", type);
				}
			}
		}
			break;
		case HTMLCodes.LISTING: {
			// <LISTING>
			style.set(Display.INFO, DisplayValue.BLOCK_VALUE);
			style.set(CSSFontFamily.INFO, FontFamilyValue.MONOSPACE);
			style.set(WhiteSpace.INFO, WhiteSpaceValue.PRE_VALUE);
			style.set(TextAlign.INFO, TextAlignValue.START_VALUE);
		}
			break;
		case HTMLCodes.LINK: {
			// <LINK>
			style.set(Display.INFO, DisplayValue.NONE_VALUE);
		}
			break;
		case HTMLCodes.MAP: {
			// <MAP name>
			style.set(Display.INFO, DisplayValue.INLINE_VALUE);

			Map<Object, ImageMap> imageMaps = style.getUserAgent().getUAContext().getImageMaps();
			String mapName = ce.atts.getValue("name");
			if (mapName != null && !imageMaps.containsKey(mapName)) {
				this.imageMap = new ImageMap();
				imageMaps.put(mapName, this.imageMap);
			} else {
				this.imageMap = null;
			}
		}
			break;
		case HTMLCodes.MARQUEE: {
			// <MARQUEE bgcolor width height hspace vspace>
			style.set(Display.INFO, DisplayValue.BLOCK_VALUE);
			HTMLStyleUtils.applyBGColor("MARQUEE", style);
			HTMLStyleUtils.applyWidthHeight("MARQUEE", style);
			HTMLStyleUtils.applyHSpaceVSpace("MARQUEE", style);
		}
			break;
		case HTMLCodes.MENU: {
			// <MENU type -compact>
			style.set(Display.INFO, DisplayValue.BLOCK_VALUE);
			HTMLStyle.applyParagraphMargins(style, EM_1_12, code);
			HTMLStyleUtils.applyListMargins(style, AbsoluteLengthValue.create(ua, 40, AbsoluteLengthValue.UNIT_PX));
			style.set(PageBreakBefore.INFO, PageBreakValue.AVOID_VALUE);
		}
			break;
		case HTMLCodes.META: {
			// <META>
			style.set(Display.INFO, DisplayValue.NONE_VALUE);
		}
			break;
		case HTMLCodes.NEXTID: {
			// <NEXTID>
			style.set(Display.INFO, DisplayValue.NONE_VALUE);
		}
			break;
		case HTMLCodes.NOBR: {
			// <NOBR>
			style.set(WhiteSpace.INFO, WhiteSpaceValue.NOWRAP_VALUE);
		}
			break;
		case HTMLCodes.NOEMBED:
			// <NOEMBED>
		case HTMLCodes.NOFRAMES:
			// <NOFRAMES>
		case HTMLCodes.NOLAYER:
			// <NOLAYER>
		case HTMLCodes.NOSCRIPT:
			// <NOSCRIPT>
			break;
		case HTMLCodes.OBJECT: {
			// <OBJECT border width height hspace vspace alt align usemap>
			HTMLStyleUtils.applyWidthHeight("OBJECT", style);
			HTMLStyleUtils.applyHSpaceVSpace("OBJECT", style);
			HTMLStyleUtils.applyImageAlign("OBJECT", style);
			String src = ce.atts.getValue("data");
			String type = ce.atts.getValue("type");
			String alt = ce.atts.getValue("alt");
			HTMLStyle.applyImage(style, src, type, alt);
			HTMLStyleUtils.applyImageBorder("OBJECT", style);
		}
			break;
		case HTMLCodes.OL: {
			// <OL type -compact start>
			// startはStyleBuilderで処理
			style.set(Display.INFO, DisplayValue.BLOCK_VALUE);
			HTMLStyle.applyParagraphMargins(style, EM_1_12, code);
			HTMLStyleUtils.applyListMargins(style, AbsoluteLengthValue.create(ua, 40, AbsoluteLengthValue.UNIT_PX));
			String type = ce.atts.getValue("type");
			if (type != null) {
				Value value = HTMLStyleUtils.toListStyleType(type);
				if (value != null) {
					style.set(ListStyleType.INFO, value);
				} else {
					ua.message(MessageCodes.WARN_BAD_HTML_ATTRIBUTE, "OL", "type" + type);
				}
			} else {
				style.set(ListStyleType.INFO, ListStyleTypeValue.DECIMAL_VALUE);
			}
			style.set(PageBreakBefore.INFO, PageBreakValue.AVOID_VALUE);
		}
			break;
		case HTMLCodes.OPTGROUP: {
			// <OPTGROUP>
			style.set(Display.INFO, DisplayValue.BLOCK_VALUE);
			style.set(MarginLeft.INFO, EM_1);
		}
			break;
		case HTMLCodes.OPTION: {
			// <OPTION>
			style.set(Display.INFO, DisplayValue.BLOCK_VALUE);
			style.set(Height.INFO, EM_1);
		}
			break;
		case HTMLCodes.P: {
			// <P align>
			style.set(Display.INFO, DisplayValue.BLOCK_VALUE);
			HTMLStyle.applyParagraphMargins(style, EM_1_12, code);
			HTMLStyleUtils.applyBlockAlign("P", style);
		}
			break;
		case HTMLCodes.PARAM: {
			// <PARAM>
			style.set(Display.INFO, DisplayValue.NONE_VALUE);
		}
			break;
		case HTMLCodes.PLAINTEXT: {
			// <PLAINTEXT>
			style.set(Display.INFO, DisplayValue.BLOCK_VALUE);
			style.set(CSSFontFamily.INFO, FontFamilyValue.MONOSPACE);
			style.set(WhiteSpace.INFO, WhiteSpaceValue.PRE_VALUE);
			style.set(TextAlign.INFO, TextAlignValue.START_VALUE);
		}
			break;
		case HTMLCodes.PRE: {
			// <PRE cols width wrap>
			style.set(Display.INFO, DisplayValue.BLOCK_VALUE);
			style.set(CSSFontFamily.INFO, FontFamilyValue.MONOSPACE);
			String wrap = ce.atts.getValue("wrap");
			if (wrap != null) {
				style.set(WhiteSpace.INFO, WhiteSpaceValue.PRE_WRAP_VALUE);
			} else {
				style.set(WhiteSpace.INFO, WhiteSpaceValue.PRE_VALUE);
			}
			style.set(TextAlign.INFO, TextAlignValue.START_VALUE);
			{
				String str = ce.atts.getValue("cols");
				if (str != null) {
					try {
						Value cols = EmLengthValue.create(NumberUtils.parseDouble(str));
						style.set(Width.INFO, cols);
					} catch (Exception e) {
						ua.message(MessageCodes.WARN_BAD_HTML_ATTRIBUTE, "PRE", "cols", str);
					}
				}
			}
			{
				String str = ce.atts.getValue("width");
				if (str != null) {
					try {
						QuantityValue length = HTMLStyleUtils.parseLength(ua, str);
						if (length.isNegative()) {
							throw new NumberFormatException();
						}
						style.set(Width.INFO, length);
					} catch (Exception e) {
						ua.message(MessageCodes.WARN_BAD_HTML_ATTRIBUTE, "PRE", "width", str);
					}
				}
			}
		}
			break;
		case HTMLCodes.Q: {
			// <Q>
		}
			break;
		case HTMLCodes.RUBY: {
			// <RUBY>
			style.set(Display.INFO, DisplayValue.INLINE_BLOCK_VALUE);
			style.set(CSSJRuby.INFO, CSSJRubyValue.RUBY_VALUE);
			style.set(TextIndent.INFO, AbsoluteLengthValue.ZERO);
			final CSSStyle pStyle = style.getParentStyle();
			if (pStyle != null && StyleUtils.isVertical(BlockFlow.get(pStyle))) {
				// 縦書き
				style.set(LineHeight.INFO, REAL_1_618);
			} else {
				style.set(LineHeight.INFO, REAL_1_414);
			}
		}
			break;
		case HTMLCodes.RB: {
			// <RB> XHTML5では非標準
			style.set(Display.INFO, DisplayValue.BLOCK_VALUE);
			style.set(CSSJRuby.INFO, CSSJRubyValue.RB_VALUE);
			style.set(LineHeight.INFO, RealValue.ONE);
			style.set(TextAlign.INFO, TextAlignValue.X_JUSTIFY_CENTER_VALUE);
			final CSSStyle pStyle = style.getParentStyle();
			if (pStyle != null && ((CSSJDirectionMode.get(pStyle) == CSSJDirectionModeValue.PHYSICAL
					&& StyleUtils.isVertical(BlockFlow.get(pStyle)))
					|| CSSJDirectionMode.get(pStyle) == CSSJDirectionModeValue.VERTICAL_RL)) {
				// 縦書き
				style.set(Width.INFO, AbsoluteLengthValue.ZERO);
			} else {
				// 横書き
				style.set(Height.INFO, AbsoluteLengthValue.ZERO);
			}
		}
			break;
		case HTMLCodes.RT: {
			// <RT>
			style.set(Display.INFO, DisplayValue.BLOCK_VALUE);
			style.set(CSSJRuby.INFO, CSSJRubyValue.RT_VALUE);
			style.set(LineHeight.INFO, RealValue.ONE);
			style.set(TextAlign.INFO, TextAlignValue.X_JUSTIFY_CENTER_VALUE);
			style.set(FontSize.INFO, PercentageValue.HALF);
			final CSSStyle pStyle = style.getParentStyle();
			if (pStyle != null && ((CSSJDirectionMode.get(pStyle) == CSSJDirectionModeValue.PHYSICAL
					&& StyleUtils.isVertical(BlockFlow.get(pStyle)))
					|| CSSJDirectionMode.get(pStyle) == CSSJDirectionModeValue.VERTICAL_RL)) {
				// 縦書き
				style.set(MarginRight.INFO, _EM__9);
				style.set(Width.INFO, AbsoluteLengthValue.ZERO);
			} else {
				// 横書き
				style.set(MarginTop.INFO, _EM__9);
				style.set(Height.INFO, AbsoluteLengthValue.ZERO);
			}
		}
			break;
		case HTMLCodes.RP: {
			// <RP>
			style.set(Display.INFO, DisplayValue.NONE_VALUE);
		}
			break;
		case HTMLCodes.S: {
			// <S>
			style.set(TextDecoration.INFO, TextDecorationValue.create(TextDecorationValue.LINE_THROUGH));
		}
			break;
		case HTMLCodes.SAMP: {
			// <SAMP>
			style.set(CSSFontFamily.INFO, FontFamilyValue.MONOSPACE);
		}
			break;
		case HTMLCodes.SCRIPT: {
			// <SCRIPT>
			style.set(Display.INFO, DisplayValue.NONE_VALUE);
		}
			break;
		case HTMLCodes.SELECT: {
			// <SELECT size>
			style.set(Display.INFO, DisplayValue.INLINE_BLOCK_VALUE);
			style.set(CSSPosition.INFO, PositionValue.RELATIVE_VALUE);
			style.set(FontSize.INFO, AbsoluteLengthValue.create(ua, ua.getFontSize(UserAgent.FONT_SIZE_MEDIUM)));
			style.set(Height.INFO, EM_1);
			{
				String str = ce.atts.getValue("size");
				if (str != null) {
					try {
						style.set(Height.INFO, EmLengthValue.create(NumberUtils.parseDouble(str)));
					} catch (NumberFormatException e) {
						ua.message(MessageCodes.WARN_BAD_HTML_ATTRIBUTE, "SELECT", "size", str);
					}
				}
			}
			style.set(Overflow.INFO, OverflowValue.HIDDEN_VALUE);
			style.set(LineHeight.INFO, RealValue.create(1));

			if (ce.atts.getValue("disabled") != null) {
				style.set(CSSColor.INFO, ColorValueUtils.DIMGRAY);
				style.set(BackgroundColor.INFO, ColorValueUtils.LIGHTGRAY);
			} else {
				style.set(BackgroundColor.INFO, ColorValueUtils.WHITE);
			}
			LengthValue thin = ua.getBorderWidth(UserAgent.BORDER_WIDTH_THIN);
			style.set(BorderTopStyle.INFO, BorderStyleValue.INSET_VALUE);
			style.set(BorderTopWidth.INFO, thin);
			style.set(BorderLeftStyle.INFO, BorderStyleValue.INSET_VALUE);
			style.set(BorderLeftWidth.INFO, thin);
			style.set(BorderBottomStyle.INFO, BorderStyleValue.INSET_VALUE);
			style.set(BorderBottomWidth.INFO, thin);
			style.set(BorderRightStyle.INFO, BorderStyleValue.INSET_VALUE);
			style.set(BorderRightWidth.INFO, thin);
			style.set(PaddingTop.INFO, thin, CSSStyle.MODE_IMPORTANT);
			style.set(PaddingRight.INFO, AbsoluteLengthValue.create(ua, Height.getLength(style).getLength()),
					CSSStyle.MODE_IMPORTANT);
			style.set(PaddingBottom.INFO, thin, CSSStyle.MODE_IMPORTANT);
			style.set(PaddingLeft.INFO, thin, CSSStyle.MODE_IMPORTANT);
			style.set(WhiteSpace.INFO, WhiteSpaceValue.NOWRAP_VALUE);
		}
			break;
		case HTMLCodes.SERVER: {
			// <SERVER>
			style.set(Display.INFO, DisplayValue.NONE_VALUE);
		}
			break;
		case HTMLCodes.SMALL: {
			// <SMALL>
			style.set(FontSize.INFO, EmLengthValue.create(0.83));
		}
			break;
		case HTMLCodes.SPAN: {
			// <SPAN>
		}
			break;
		case HTMLCodes.STRIKE: {
			// <STRIKE>
			style.set(TextDecoration.INFO, TextDecorationValue.create(TextDecorationValue.LINE_THROUGH));
		}
			break;
		case HTMLCodes.STRONG: {
			// <STRONG>
			style.set(FontWeight.INFO, FontWeightValue.BOLDER_VALUE);
		}
			break;
		case HTMLCodes.STYLE: {
			// <STYLE>
			style.set(Display.INFO, DisplayValue.NONE_VALUE);
		}
			break;
		case HTMLCodes.SUB: {
			// <SUB>
			style.set(FontSize.INFO, EmLengthValue.create(0.83));
			style.set(VerticalAlign.INFO, VerticalAlignValue.SUB_VALUE);
		}
			break;
		case HTMLCodes.SUP: {
			// <SUP>
			style.set(FontSize.INFO, EmLengthValue.create(0.83));
			style.set(VerticalAlign.INFO, VerticalAlignValue.SUPER_VALUE);
		}
			break;
		case HTMLCodes.TABLE: {
			// <TABLE width height
			// bgcolor background align
			// hspace vspace
			// border frame
			// rules cellspacing cellpadding
			// bordercolor
			// -bordercolordark
			// -bordercolorlight
			// -cols -summary>
			style.set(Display.INFO, DisplayValue.TABLE_VALUE);

			Value cellspacing = null;
			{
				String str = ce.atts.getValue("cellspacing");
				if (str != null) {
					cellspacing = ValueUtils.toLength(ua, true, str);
					if (cellspacing == null) {
						ua.message(MessageCodes.WARN_BAD_HTML_ATTRIBUTE, "TABLE", "cellspacing" + str);
					}
				}
			}
			if (cellspacing == null) {
				cellspacing = AbsoluteLengthValue.create(ua, 2, AbsoluteLengthValue.UNIT_PX);
			}
			style.set(BorderSpacing.INFO_H, cellspacing);
			style.set(BorderSpacing.INFO_V, cellspacing);
			LengthValue borderWidth = AbsoluteLengthValue.ZERO;
			{
				String str = ce.atts.getValue("border");
				if (str != null) {
					if (str.length() == 0) {
						borderWidth = ua.getBorderWidth(UserAgent.BORDER_WIDTH_THIN);
					} else {
						borderWidth = ValueUtils.toLength(ua, true, str);
						if (borderWidth == null) {
							ua.message(MessageCodes.WARN_BAD_HTML_ATTRIBUTE, "TABLE", "border", str);
							borderWidth = ua.getBorderWidth(UserAgent.BORDER_WIDTH_THIN);
						}
					}
				}
			}

			HTMLStyleUtils.applyWidthHeight("TABLE", style);
			HTMLStyleUtils.applyHSpaceVSpace("TABLE", style);
			HTMLStyleUtils.applyBGColor("TABLE", style);
			HTMLStyleUtils.applyBackground("TABLE", style);
			HTMLStyleUtils.applyTableAlign("TABLE", style);
			{
				String str = ce.atts.getValue("frame");
				if (str != null) {
					if (str.equalsIgnoreCase("void")) {
						style.set(BorderTopStyle.INFO, BorderStyleValue.NONE_VALUE);
						style.set(BorderRightStyle.INFO, BorderStyleValue.NONE_VALUE);
						style.set(BorderBottomStyle.INFO, BorderStyleValue.NONE_VALUE);
						style.set(BorderLeftStyle.INFO, BorderStyleValue.NONE_VALUE);
					} else if (str.equalsIgnoreCase("above")) {
						style.set(BorderTopStyle.INFO, BorderStyleValue.OUTSET_VALUE);
						style.set(BorderRightStyle.INFO, BorderStyleValue.NONE_VALUE);
						style.set(BorderBottomStyle.INFO, BorderStyleValue.NONE_VALUE);
						style.set(BorderLeftStyle.INFO, BorderStyleValue.NONE_VALUE);
					} else if (str.equalsIgnoreCase("below")) {
						style.set(BorderTopStyle.INFO, BorderStyleValue.NONE_VALUE);
						style.set(BorderRightStyle.INFO, BorderStyleValue.NONE_VALUE);
						style.set(BorderBottomStyle.INFO, BorderStyleValue.OUTSET_VALUE);
						style.set(BorderLeftStyle.INFO, BorderStyleValue.NONE_VALUE);
					} else if (str.equalsIgnoreCase("hsides")) {
						style.set(BorderTopStyle.INFO, BorderStyleValue.NONE_VALUE);
						style.set(BorderRightStyle.INFO, BorderStyleValue.OUTSET_VALUE);
						style.set(BorderBottomStyle.INFO, BorderStyleValue.NONE_VALUE);
						style.set(BorderLeftStyle.INFO, BorderStyleValue.OUTSET_VALUE);
					} else if (str.equalsIgnoreCase("vsides")) {
						style.set(BorderTopStyle.INFO, BorderStyleValue.OUTSET_VALUE);
						style.set(BorderRightStyle.INFO, BorderStyleValue.NONE_VALUE);
						style.set(BorderBottomStyle.INFO, BorderStyleValue.OUTSET_VALUE);
						style.set(BorderLeftStyle.INFO, BorderStyleValue.NONE_VALUE);
					} else if (str.equalsIgnoreCase("lhs")) {
						style.set(BorderTopStyle.INFO, BorderStyleValue.NONE_VALUE);
						style.set(BorderRightStyle.INFO, BorderStyleValue.NONE_VALUE);
						style.set(BorderBottomStyle.INFO, BorderStyleValue.NONE_VALUE);
						style.set(BorderLeftStyle.INFO, BorderStyleValue.OUTSET_VALUE);
					} else if (str.equalsIgnoreCase("rhs")) {
						style.set(BorderTopStyle.INFO, BorderStyleValue.NONE_VALUE);
						style.set(BorderRightStyle.INFO, BorderStyleValue.OUTSET_VALUE);
						style.set(BorderBottomStyle.INFO, BorderStyleValue.NONE_VALUE);
						style.set(BorderLeftStyle.INFO, BorderStyleValue.NONE_VALUE);
					}
				}
			}
			style.set(TextAlign.INFO, TextAlignValue.LEFT_VALUE);
			if (style.getUserAgent().getDocumentContext().getCompatibleMode() >= DocumentContext.CM_NORMAL) {
				style.set(FontSize.INFO, AbsoluteLengthValue.create(ua, ua.getFontSize(UserAgent.FONT_SIZE_MEDIUM)));
			}
			style.set(TextIndent.INFO, AbsoluteLengthValue.ZERO);
			LengthValue cellpadding = null;
			{
				String str = ce.atts.getValue("cellpadding");
				if (str != null) {
					cellpadding = ValueUtils.toLength(ua, true, str);
					if (cellpadding == null) {
						ua.message(MessageCodes.WARN_BAD_HTML_ATTRIBUTE, "TABLE", "cellpadding", str);
					}
				}
			}
			if (cellpadding == null) {
				cellpadding = AbsoluteLengthValue.create(ua, 1, AbsoluteLengthValue.UNIT_PX);
			}
			CSSJHtmlCellPadding.set(style, cellpadding);
			ColorValue borderColor = null;
			{
				String str = ce.atts.getValue("bordercolor");
				if (str != null) {
					borderColor = HTMLStyleUtils.parseColor(str);
					if (borderColor == null) {
						ua.message(MessageCodes.WARN_BAD_HTML_ATTRIBUTE, "TABLE", "bordercolor", str);
					}
				}
			}
			{
				BorderStyleValue borderStyle;
				if (borderColor == null) {
					borderStyle = BorderStyleValue.OUTSET_VALUE;
				} else {
					borderStyle = BorderStyleValue.SOLID_VALUE;
					style.set(BorderTopColor.INFO, borderColor);
					style.set(BorderRightColor.INFO, borderColor);
					style.set(BorderBottomColor.INFO, borderColor);
					style.set(BorderLeftColor.INFO, borderColor);
				}
				CSSJHtmlTableBorder.set(style, new CSSJHtmlTableBorderValue(borderWidth, borderColor));

				style.set(BorderTopStyle.INFO, borderStyle);
				style.set(BorderTopWidth.INFO, borderWidth);
				style.set(BorderRightStyle.INFO, borderStyle);
				style.set(BorderRightWidth.INFO, borderWidth);
				style.set(BorderBottomStyle.INFO, borderStyle);
				style.set(BorderBottomWidth.INFO, borderWidth);
				style.set(BorderLeftStyle.INFO, borderStyle);
				style.set(BorderLeftWidth.INFO, borderWidth);
			}

			{
				String str = ce.atts.getValue("rules");
				if (str != null) {
					if (str.equalsIgnoreCase("all") || str.equalsIgnoreCase("groups") || str.equalsIgnoreCase("rows")
							|| str.equalsIgnoreCase("cols") || str.equalsIgnoreCase("none")) {
						style.set(BorderCollapse.INFO, BorderCollapseValue.COLLAPSE_VALUE);
					}
				}
			}
		}
			break;
		case HTMLCodes.TBODY: {
			// <TBODY align bgcolor valign -charoff>
			style.set(Display.INFO, DisplayValue.TABLE_ROW_GROUP_VALUE);
			HTMLStyle.applyTableRows("TBODY", style);
		}
			break;
		case HTMLCodes.TD: {
			// <TD bordercolor background bgcolor
			// align valign height width nowrap colspan rowspan
			// -charoff,-bordercolordark,-bordercolorlight>
			style.set(CSSJHtmlAlign.INFO, CSSJHtmlAlignValue.START_VALUE);
			HTMLStyle.applyTableCell("TD", style);
		}
			break;
		case HTMLCodes.TH: {
			// <TH bordercolor background bgcolor
			// align valign height width nowrap colspan rowspan
			// -charoff,-bordercolordark,-bordercolorlight>
			style.set(FontWeight.INFO, FontWeightValue.BOLD_VALUE);
			style.set(TextAlign.INFO, TextAlignValue.CENTER_VALUE);
			style.set(CSSJHtmlAlign.INFO, CSSJHtmlAlignValue.START_VALUE);
			HTMLStyle.applyTableCell("TH", style);
		}
			break;
		case HTMLCodes.TFOOT: {
			// <TFOOT align bgcolor valign -charoff>
			style.set(Display.INFO, DisplayValue.TABLE_FOOTER_GROUP_VALUE);
			HTMLStyle.applyTableRows("TFOOT", style);
		}
			break;
		case HTMLCodes.THEAD: {
			// <THEAD align bgcolor valign -charoff>
			style.set(Display.INFO, DisplayValue.TABLE_HEADER_GROUP_VALUE);
			HTMLStyle.applyTableRows("THEAD", style);
		}
			break;
		case HTMLCodes.TR: {
			// <TR bordercolor background bgcolor align valign height
			// -charoff,-bordercolordark,-bordercolorlight>
			style.set(Display.INFO, DisplayValue.TABLE_ROW_VALUE);
			style.set(CSSJHtmlAlign.INFO, CSSJHtmlAlignValue.START_VALUE);
			HTMLStyleUtils.applyBlockAlign("TR", style);
			HTMLStyleUtils.applyBackground("TR", style);
			HTMLStyleUtils.applyBGColor("TR", style);
			{
				String str = ce.atts.getValue("height");
				if (str != null) {
					try {
						Value height = HTMLStyleUtils.parseLength(ua, str);
						style.set(Height.INFO, height);
					} catch (Exception e) {
						ua.message(MessageCodes.WARN_BAD_HTML_ATTRIBUTE, "TR", "height", str);
					}
				}
			}
			CSSStyle parent = style.getParentStyle();
			for (; parent != null; parent = parent.getParentStyle()) {
				CSSElement parentCe = parent.getCSSElement();
				if (HTMLCodes.code(parentCe) == HTMLCodes.TABLE) {
					if ("rows".equalsIgnoreCase(parentCe.atts.getValue("rules"))) {
						style.set(BorderTopStyle.INFO, BorderStyleValue.SOLID_VALUE);
						style.set(BorderTopWidth.INFO, ua.getBorderWidth(UserAgent.BORDER_WIDTH_THIN));
						style.set(BorderBottomStyle.INFO, BorderStyleValue.SOLID_VALUE);
						style.set(BorderBottomWidth.INFO, ua.getBorderWidth(UserAgent.BORDER_WIDTH_THIN));
					}
					break;
				}
			}
		}
			break;
		case HTMLCodes.TT: {
			// <TT>
			style.set(CSSFontFamily.INFO, FontFamilyValue.MONOSPACE);
		}
			break;
		case HTMLCodes.TEXTAREA: {
			// <TEXTAREA cols rows disabled wrap>
			style.set(Display.INFO, DisplayValue.INLINE_BLOCK_VALUE);
			style.set(Width.INFO, EX_20);
			{
				String str = ce.atts.getValue("cols");
				if (str != null) {
					try {
						style.set(Width.INFO, ExLengthValue.create(NumberUtils.parseDouble(str)));
					} catch (NumberFormatException e) {
						ua.message(MessageCodes.WARN_BAD_HTML_ATTRIBUTE, "TEXTAREA", "cols", str);
					}
				}
			}
			style.set(Height.INFO, EM_4);
			{
				String str = ce.atts.getValue("rows");
				if (str != null) {
					try {
						style.set(Height.INFO, EmLengthValue.create(NumberUtils.parseDouble(str)));
					} catch (NumberFormatException e) {
						ua.message(MessageCodes.WARN_BAD_HTML_ATTRIBUTE, "TEXTAREA", "rows", str);
					}
				}
			}
			if (ce.atts.getValue("disabled") != null) {
				style.set(CSSColor.INFO, ColorValueUtils.GRAY);
			}
			style.set(BackgroundColor.INFO, ColorValueUtils.WHITE);
			LengthValue thin = ua.getBorderWidth(UserAgent.BORDER_WIDTH_THIN);
			style.set(BorderTopStyle.INFO, BorderStyleValue.INSET_VALUE);
			style.set(BorderTopWidth.INFO, thin);
			style.set(BorderLeftStyle.INFO, BorderStyleValue.INSET_VALUE);
			style.set(BorderLeftWidth.INFO, thin);
			style.set(BorderBottomStyle.INFO, BorderStyleValue.INSET_VALUE);
			style.set(BorderBottomWidth.INFO, thin);
			style.set(BorderRightStyle.INFO, BorderStyleValue.INSET_VALUE);
			style.set(BorderRightWidth.INFO, thin);
			style.set(PaddingTop.INFO, thin);
			style.set(PaddingRight.INFO, thin);
			style.set(PaddingBottom.INFO, thin);
			style.set(PaddingLeft.INFO, thin);
			if (ce.atts.getValue("wrap") != null) {
				style.set(WhiteSpace.INFO, WhiteSpaceValue.PRE_WRAP_VALUE);
			} else {
				style.set(WhiteSpace.INFO, WhiteSpaceValue.NOWRAP_VALUE);
			}
		}
			break;
		case HTMLCodes.TITLE: {
			// <TITLE>
			style.set(Display.INFO, DisplayValue.NONE_VALUE);
		}
			break;
		case HTMLCodes.U: {
			// <U>
			style.set(TextDecoration.INFO, TextDecorationValue.create(TextDecorationValue.UNDERLINE));
		}
			break;
		case HTMLCodes.UL: {
			// <UL type -compact>
			int depth = 0;
			for (CSSStyle parent = style.getParentStyle(); parent != null; parent = parent.getParentStyle()) {
				if (HTMLCodes.code(parent.getCSSElement()) == HTMLCodes.UL) {
					++depth;
				}
			}

			style.set(Display.INFO, DisplayValue.BLOCK_VALUE);
			if (depth == 0) {
				HTMLStyle.applyParagraphMargins(style, EM_1_12, code);
			}
			HTMLStyleUtils.applyListMargins(style, AbsoluteLengthValue.create(ua, 40, AbsoluteLengthValue.UNIT_PX));
			String type = ce.atts.getValue("type");
			if (type != null) {
				Value value = HTMLStyleUtils.toListStyleType(type);
				if (value != null) {
					style.set(ListStyleType.INFO, value);
				} else {
					ua.message(MessageCodes.WARN_BAD_HTML_ATTRIBUTE, "UL", "type", type);
				}
			} else {
				final ListStyleTypeValue listStyle;
				switch (depth) {
				case 0:
					listStyle = ListStyleTypeValue.DISC_VALUE;
					break;
				case 1:
					listStyle = ListStyleTypeValue.CIRCLE_VALUE;
					break;
				default:
					listStyle = ListStyleTypeValue.SQUARE_VALUE;
					break;
				}
				style.set(ListStyleType.INFO, listStyle);
			}
			style.set(PageBreakBefore.INFO, PageBreakValue.AVOID_VALUE);
		}
			break;
		case HTMLCodes.VAR: {
			// <VAR>
			style.set(CSSFontStyle.INFO, FontStyleValue.ITALIC_VALUE);
		}
			break;
		case HTMLCodes.VIDEO: {
			// <VIDEO width height poster>
			HTMLStyleUtils.applyWidthHeight("VIDEO", style);
			style.set(Display.INFO, DisplayValue.INLINE_BLOCK_VALUE);
			String poster = ce.atts.getValue("poster");
			if (poster != null) {
				HTMLStyle.applyImage(style, poster, null, "");
			}
		}
			break;
		case HTMLCodes.WBR: {
			// <WBR>
		}
			break;
		case HTMLCodes.XMP: {
			// <XMP>
			style.set(Display.INFO, DisplayValue.BLOCK_VALUE);
			style.set(CSSFontFamily.INFO, FontFamilyValue.MONOSPACE);
			style.set(WhiteSpace.INFO, WhiteSpaceValue.PRE_VALUE);
			style.set(TextAlign.INFO, TextAlignValue.START_VALUE);
		}
			break;
		}

		// @hidden
		{
			String hidden = ce.atts.getValue("hidden");
			if (hidden != null) {
				style.set(Display.INFO, DisplayValue.NONE_VALUE);
			}
		}
	}
}
