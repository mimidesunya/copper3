package jp.cssj.homare.style.util;

import java.awt.geom.AffineTransform;

import jp.cssj.homare.css.util.ValueUtils;
import jp.cssj.homare.css.value.AbsoluteLengthValue;
import jp.cssj.homare.message.MessageCodes;
import jp.cssj.homare.style.box.AbstractContainerBox;
import jp.cssj.homare.style.box.AbstractReplacedBox;
import jp.cssj.homare.style.box.IBox;
import jp.cssj.homare.style.box.impl.TableBox;
import jp.cssj.homare.style.box.params.AbstractTextParams;
import jp.cssj.homare.style.box.params.BlockParams;
import jp.cssj.homare.style.box.params.Dimension;
import jp.cssj.homare.style.box.params.Insets;
import jp.cssj.homare.style.box.params.Length;
import jp.cssj.homare.style.box.params.Offset;
import jp.cssj.homare.style.box.params.Pos;
import jp.cssj.homare.style.box.params.TableParams;
import jp.cssj.homare.style.builder.Builder;
import jp.cssj.homare.style.builder.impl.BlockBuilder;
import jp.cssj.homare.style.imposition.Imposition;
import jp.cssj.homare.style.part.AbsoluteInsets;
import jp.cssj.homare.ua.UserAgent;
import jp.cssj.homare.ua.props.OutputMarks;
import jp.cssj.homare.ua.props.UAProps;
import jp.cssj.sakae.gc.GC;
import jp.cssj.sakae.gc.GraphicsException;
import jp.cssj.sakae.gc.font.FontFamilyList;
import jp.cssj.sakae.gc.font.FontPolicyList;
import jp.cssj.sakae.gc.text.TextLayoutHandler;
import jp.cssj.sakae.gc.text.hyphenation.HyphenationBundle;
import jp.cssj.sakae.gc.text.layout.PageLayoutGlyphHandler;

/**
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: StyleUtils.java 1574 2018-10-26 02:44:00Z miyabe $
 */
public final class StyleUtils {
	private StyleUtils() {
		// unused
	}

	// magic number
	public static final double NONE = Double.MAX_VALUE * 0.958324758437;

	public static final boolean isNone(double v) {
		return v == NONE;
	}

	public static final double THRESHOLD = .5;

	/**
	 * a &lt; bなら負、a &gt; bなら正、a = bならゼロを返します。<br>
	 * 計算誤差による判定間違いを防ぐため、 行の折り返し、浮動ボックス、行の位置指定、改ページ制御のための比較はこれを使用します。
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static int compare(double a, double b) {
		// 0.5未満の差は同一と見なします
		// IEでは切り落とし、Firefoxは小数点以下1桁でまるめている模様
		// 注：まるめてから比較する実装では、差が同じでも判定が変わるため、
		// 内容がページをはみ出していると判定された場合でも
		// ボックスの分割の途中で判定が矛盾することがあった
		double diff = a - b;
		if (diff < THRESHOLD && diff > -THRESHOLD) {
			return 0;
		}
		return a < b ? -1 : 1;
	}

	/**
	 * 行方向が固定されていればtrueを返します。
	 * 
	 * @param containerBox
	 * @param blockBox
	 * @return
	 */
	public static final boolean isFixedLineAxis(AbstractContainerBox containerBox, AbstractContainerBox blockBox) {
		if (blockBox.getPos().getType() == Pos.TYPE_ABSOLUTE) {
			if (StyleUtils.isVertical(containerBox.getBlockParams().flow)) {
				// 縦書き
				return blockBox.getBlockParams().size.getHeightType() == Dimension.TYPE_ABSOLUTE;
			} else {
				// 横書き
				return blockBox.getBlockParams().size.getWidthType() == Dimension.TYPE_ABSOLUTE;
			}
		} else {
			if (StyleUtils.isVertical(containerBox.getBlockParams().flow)) {
				// 縦書き
				return blockBox.getBlockParams().size.getHeightType() != Dimension.TYPE_AUTO;
			} else {
				// 横書き
				return blockBox.getBlockParams().size.getWidthType() != Dimension.TYPE_AUTO;
			}
		}
	}

	/**
	 * 行方向が固定されていればtrueを返します。
	 * 
	 * @param containerBox
	 * @param replacedBox
	 * @return
	 */
	public static final boolean isFixedLineAxis(AbstractContainerBox containerBox, AbstractReplacedBox replacedBox) {
		if (replacedBox.getPos().getType() == Pos.TYPE_ABSOLUTE) {
			if (StyleUtils.isVertical(containerBox.getBlockParams().flow)) {
				// 縦書き
				return replacedBox.getReplacedParams().size.getHeightType() == Dimension.TYPE_ABSOLUTE;
			} else {
				// 横書き
				return replacedBox.getReplacedParams().size.getWidthType() == Dimension.TYPE_ABSOLUTE;
			}
		} else {
			if (StyleUtils.isVertical(containerBox.getBlockParams().flow)) {
				// 縦書き
				return replacedBox.getReplacedParams().size.getHeightType() != Dimension.TYPE_AUTO;
			} else {
				// 横書き
				return replacedBox.getReplacedParams().size.getWidthType() != Dimension.TYPE_AUTO;
			}
		}
	}

	/**
	 * テキストを描画します。
	 * 
	 * @param gc
	 * @param fontSize
	 * @param text
	 * @param x
	 * @param y
	 * @param width
	 */
	public static void drawText(GC gc, FontPolicyList fontPolicy, double fontSize, String text, double x, double y,
			double width) throws GraphicsException {
		assert !StyleUtils.isNone(x);
		assert !StyleUtils.isNone(y);
		assert !StyleUtils.isNone(width);
		gc.begin();
		gc.transform(AffineTransform.getTranslateInstance(x, y));

		PageLayoutGlyphHandler lineHandler = new PageLayoutGlyphHandler();
		lineHandler.setGC(gc);
		lineHandler.setLineAdvance(width);

		TextLayoutHandler tlf = new TextLayoutHandler(gc, HyphenationBundle.getHyphenation(null), lineHandler);
		tlf.setFontFamilies(FontFamilyList.SERIF);
		tlf.setFontPolicy(fontPolicy);
		tlf.setFontSize(fontSize);
		tlf.characters(text);
		tlf.flush();

		lineHandler.finish();
		gc.end();
	}

	/**
	 * 長さを計算します。
	 * 
	 * @param length
	 * @param ref
	 * @return
	 */
	public static double computeLength(Length length, double ref) {
		switch (length.getType()) {
		case Length.TYPE_RELATIVE:
			return length.getLength() * ref;
		case Length.TYPE_ABSOLUTE:
			return length.getLength();
		case Length.TYPE_AUTO:
			return StyleUtils.NONE;
		default:
			throw new IllegalStateException();
		}
	}

	/**
	 * AUTOをゼロとしてインセットを計算します。
	 * 
	 * @param ainsets
	 * @param insets
	 * @param refSize
	 */
	public static void computeMarginsAutoToZero(AbsoluteInsets ainsets, Insets insets, double refSize) {
		double top, right, bottom, left;
		switch (insets.getTopType()) {
		case Insets.TYPE_ABSOLUTE:
			top = insets.getTop();
			break;
		case Insets.TYPE_RELATIVE:
			top = insets.getTop() * refSize;
			break;
		case Insets.TYPE_AUTO:
			top = 0;
			break;
		default:
			throw new IllegalStateException();
		}

		switch (insets.getRightType()) {
		case Insets.TYPE_ABSOLUTE:
			right = insets.getRight();
			break;
		case Insets.TYPE_RELATIVE:
			right = insets.getRight() * refSize;
			break;
		case Insets.TYPE_AUTO:
			right = 0;
			break;
		default:
			throw new IllegalStateException();
		}

		switch (insets.getBottomType()) {
		case Insets.TYPE_ABSOLUTE:
			bottom = insets.getBottom();
			break;
		case Insets.TYPE_RELATIVE:
			bottom = insets.getBottom() * refSize;
			break;
		case Insets.TYPE_AUTO:
			bottom = 0;
			break;
		default:
			throw new IllegalStateException();
		}

		switch (insets.getLeftType()) {
		case Insets.TYPE_ABSOLUTE:
			left = insets.getLeft();
			break;
		case Insets.TYPE_RELATIVE:
			left = insets.getLeft() * refSize;
			break;
		case Insets.TYPE_AUTO:
			left = 0;
			break;
		default:
			throw new IllegalStateException();
		}
		ainsets.top = top;
		ainsets.right = right;
		ainsets.bottom = bottom;
		ainsets.left = left;
	}

	public static void computePaddings(AbsoluteInsets ainsets, Insets insets, double refSize) {
		double top, right, bottom, left;
		switch (insets.getTopType()) {
		case Insets.TYPE_ABSOLUTE:
			top = insets.getTop();
			break;
		case Insets.TYPE_RELATIVE:
			top = insets.getTop() * refSize;
			break;
		default:
			throw new IllegalStateException();
		}

		switch (insets.getRightType()) {
		case Insets.TYPE_ABSOLUTE:
			right = insets.getRight();
			break;
		case Insets.TYPE_RELATIVE:
			right = insets.getRight() * refSize;
			break;
		default:
			throw new IllegalStateException();
		}

		switch (insets.getBottomType()) {
		case Insets.TYPE_ABSOLUTE:
			bottom = insets.getBottom();
			break;
		case Insets.TYPE_RELATIVE:
			bottom = insets.getBottom() * refSize;
			break;
		default:
			throw new IllegalStateException();
		}

		switch (insets.getLeftType()) {
		case Insets.TYPE_ABSOLUTE:
			left = insets.getLeft();
			break;
		case Insets.TYPE_RELATIVE:
			left = insets.getLeft() * refSize;
			break;
		default:
			throw new IllegalStateException();
		}
		ainsets.top = top;
		ainsets.right = right;
		ainsets.bottom = bottom;
		ainsets.left = left;
	}

	/**
	 * Dimensionの幅を計算します。 AUTOの場合はNaNを返します。
	 * 
	 * @param size
	 * @param ref
	 * @return
	 */
	public static double computeDimensionWidth(Dimension size, double ref) {
		switch (size.getWidthType()) {
		case Dimension.TYPE_RELATIVE:
			if (ref == StyleUtils.NONE) {
				return StyleUtils.NONE;
			}
			return size.getWidth() * ref;
		case Dimension.TYPE_ABSOLUTE:
			return size.getWidth();
		case Dimension.TYPE_AUTO:
			return StyleUtils.NONE;
		default:
			throw new IllegalStateException();
		}
	}

	/**
	 * Dimensionの高さを計算します。 AUTOの場合はNaNを返します。
	 * 
	 * @param size
	 * @param ref
	 * @return
	 */
	public static double computeDimensionHeight(Dimension size, double ref) {
		switch (size.getHeightType()) {
		case Dimension.TYPE_RELATIVE:
			if (ref == StyleUtils.NONE) {
				return StyleUtils.NONE;
			}
			return size.getHeight() * ref;
		case Dimension.TYPE_ABSOLUTE:
			return size.getHeight();
		case Dimension.TYPE_AUTO:
			return StyleUtils.NONE;
		default:
			throw new IllegalStateException();
		}
	}

	public static double computeInsetsTop(Insets insets, double ref) {
		switch (insets.getTopType()) {
		case Insets.TYPE_ABSOLUTE:
			return insets.getTop();
		case Insets.TYPE_RELATIVE:
			return insets.getTop() * ref;
		case Insets.TYPE_AUTO:
			return StyleUtils.NONE;
		default:
			throw new IllegalStateException();
		}
	}

	public static double computeInsetsLeft(Insets insets, double ref) {
		switch (insets.getLeftType()) {
		case Insets.TYPE_ABSOLUTE:
			return insets.getLeft();
		case Insets.TYPE_RELATIVE:
			return insets.getLeft() * ref;
		case Insets.TYPE_AUTO:
			return StyleUtils.NONE;
		default:
			throw new IllegalStateException();
		}
	}

	public static double computeInsetsRight(Insets insets, double ref) {
		switch (insets.getRightType()) {
		case Insets.TYPE_ABSOLUTE:
			return insets.getRight();
		case Insets.TYPE_RELATIVE:
			return insets.getRight() * ref;
		case Insets.TYPE_AUTO:
			return StyleUtils.NONE;
		default:
			throw new IllegalStateException();
		}
	}

	public static double computeInsetsBottom(Insets insets, double ref) {
		switch (insets.getBottomType()) {
		case Insets.TYPE_ABSOLUTE:
			return insets.getBottom();
		case Insets.TYPE_RELATIVE:
			return insets.getBottom() * ref;
		case Insets.TYPE_AUTO:
			return StyleUtils.NONE;
		default:
			throw new IllegalStateException();
		}
	}

	public static boolean isTwoPassTable(TableBox box) {
		TableParams params = box.getTableParams();
		if (params.layout == TableParams.LAYOUT_AUTO) {
			// 自動レイアウトは2パス
			return true;
		}
		if (box.getBlockBox().getPos().getType() != Pos.TYPE_FLOW) {
			// 通常のフローにない場合は2パス
			return true;
		}
		boolean vertical = StyleUtils.isVertical(params.flow);
		if ((vertical ? params.size.getWidthType() : params.size.getHeightType()) != Dimension.TYPE_AUTO) {
			// 高さが指定された場合は2パス
			return true;
		}
		if ((vertical ? params.size.getHeightType() : params.size.getWidthType()) == Dimension.TYPE_AUTO) {
			// 自動幅の場合は2パス
			return true;
		}
		return false;
	}

	public static double computeOffsetX(Offset offset, IBox containerBox) {
		switch (offset.getXType()) {
		case Insets.TYPE_ABSOLUTE:
			return offset.getX();
		case Insets.TYPE_RELATIVE:
			// this.offsetX = pos.offset.getX() * container.getInnerWidth();
			// break;
		case Insets.TYPE_AUTO:
			return 0;
		default:
			throw new IllegalStateException();
		}
	}

	public static double computeOffsetY(Offset offset, IBox containerBox) {
		switch (offset.getYType()) {
		case Insets.TYPE_ABSOLUTE:
			return offset.getY();
		case Insets.TYPE_RELATIVE:
			// this.offsetY = pos.offset.getY() * container.getInnerWidth();
			// break;
		case Insets.TYPE_AUTO:
			return 0;
		default:
			throw new IllegalStateException();
		}
	}

	public static boolean isVertical(byte progression) {
		switch (progression) {
		case AbstractTextParams.FLOW_TB:
			// 横書き
			return false;
		case AbstractTextParams.FLOW_LR:
		case AbstractTextParams.FLOW_RL:
			// 縦書き
			return true;
		default:
			throw new IllegalStateException();
		}
	}

	public static void calclateReplacedSize(Builder builder, AbstractReplacedBox replacedBox) {
		//
		// ■ 幅と高さの計算
		//
		final double refWidth, refHeight, refMaxWidth, refMaxHeight;
		final AbstractContainerBox containerBox = builder.getFlowBox();
		final BlockParams params = containerBox.getBlockParams();
		final double lineSize = containerBox.getLineSize();
		replacedBox.calculateFrame(lineSize);
		if (StyleUtils.isVertical(params.flow)) {
			// 縦書き
			AbstractContainerBox box;
			if (containerBox == builder.getContextBox()) {
				box = builder.getFixedWidthContextBox();
			} else {
				box = builder.getFixedWidthFlowBox();
			}
			if (box == null) {
				if (builder.getContextBox().getType() == IBox.TYPE_TABLE_CELL && builder instanceof BlockBuilder) {
					// セル内でページ送りされた場合
					return;
				}
				refMaxWidth = refWidth = StyleUtils.NONE;
				refMaxHeight = refHeight = StyleUtils.NONE;
			} else {
				refWidth = box.getType()== IBox.TYPE_PAGE ? StyleUtils.NONE : box.getInnerWidth();
				refMaxWidth = box.getInnerWidth();
				// 通常のフローでないため行幅があてにならない時はフローを探す
				if (builder.isTwoPass()) {
					refMaxHeight =refHeight = StyleUtils.NONE;
				} else if (containerBox.getPos().getType() != Pos.TYPE_FLOW
						&& containerBox.getPos().getType() != Pos.TYPE_FLOAT
						&& containerBox.getPos().getType() != Pos.TYPE_TABLE_CELL) {
					if (containerBox == builder.getContextBox()) {
						box = builder.getFixedHeightContextBox();
					} else {
						box = builder.getFixedHeightFlowBox();
					}
					if (box == null) {
						refMaxHeight =refHeight = StyleUtils.NONE;
					} else {
						refMaxHeight =refHeight = box.getLineSize();
					}
				} else {
					refMaxHeight =refHeight = lineSize;
				}
			}
		} else {
			// 横書き
			AbstractContainerBox box;
			if (containerBox == builder.getContextBox()) {
				box = builder.getFixedHeightContextBox();
			} else {
				box = builder.getFixedHeightFlowBox();
			}
			if (box == null) {
				if (builder.getContextBox().getType() == IBox.TYPE_TABLE_CELL && builder instanceof BlockBuilder) {
					// セル内でページ送りされた場合
					return;
				}
				refMaxHeight = refHeight = StyleUtils.NONE;
				refMaxWidth = refWidth = StyleUtils.NONE;
			} else {
				refHeight = box.getType()== IBox.TYPE_PAGE ? StyleUtils.NONE : box.getInnerHeight();
				refMaxHeight = box.getInnerHeight();
				// 通常のフローでないため行幅があてにならない時はフローを探す
				if (builder.isTwoPass()) {
					refMaxWidth = refWidth = StyleUtils.NONE;
				} else if (containerBox.getPos().getType() != Pos.TYPE_FLOW
						&& containerBox.getPos().getType() != Pos.TYPE_FLOAT
						&& containerBox.getPos().getType() != Pos.TYPE_TABLE_CELL) {
					if (containerBox == builder.getContextBox()) {
						box = builder.getFixedWidthContextBox();
					} else {
						box = builder.getFixedWidthFlowBox();
					}
					if (box == null) {
						refMaxWidth = refWidth = StyleUtils.NONE;
					} else {
						refMaxWidth = refWidth = box.getLineSize();
					}
				} else {
					refMaxWidth = refWidth = lineSize;
				}
			}
		}
		replacedBox.calculateSize(refWidth, refHeight, refMaxWidth, refMaxHeight);
	}

	public static double getMaxAdvance(final AbstractContainerBox box) {
		final BlockParams params = box.getBlockParams();
		final double lineSize;
		if (StyleUtils.isVertical(params.flow)) {
			// 縦書き
			lineSize = box.getInnerHeight();
		} else {
			// 横書き
			lineSize = box.getInnerWidth();
		}
		return lineSize;
	}

	public static int getColumnCount(final AbstractContainerBox box) {
		final BlockParams params = box.getBlockParams();
		if (StyleUtils.isNone(params.columns.width)) {
			return params.columns.count;
		}
		final double lineSize = StyleUtils.getMaxAdvance(box);
		if (params.columns.width >= lineSize) {
			return 1;
		}
		return (int) Math.floor((lineSize + params.columns.gap) / (params.columns.width + params.columns.gap));
	}

	public static void setupImposition(final UserAgent ua, final Imposition imposition) {
		imposition.setAutoRotate((byte) UAProps.OUTPUT_AUTO_ROTATE.getCode(ua));
		imposition.setAlign((byte) UAProps.OUTPUT_FIT_TO_PAPER.getCode(ua));

		// 左右断ちしろ
		{
			String s = UAProps.OUTPUT_HTRIM.getString(ua);
			AbsoluteLengthValue length = ValueUtils.toAbsoluteLength(ua, false, s);
			if (length != null) {
				double l = length.getLength();
				imposition.setTrims(imposition.getTrimTop(), l, imposition.getTrimBottom(), l);
			} else {
				ua.message(MessageCodes.WARN_BAD_IO_PROPERTY, UAProps.OUTPUT_HTRIM.name, s);
			}
		}
		// 上下断ちしろ
		{
			String s = UAProps.OUTPUT_VTRIM.getString(ua);
			AbsoluteLengthValue length = ValueUtils.toAbsoluteLength(ua, false, s);
			if (length != null) {
				double l = length.getLength();
				imposition.setTrims(l, imposition.getTrimRight(), l, imposition.getTrimLeft());
			} else {
				ua.message(MessageCodes.WARN_BAD_IO_PROPERTY, UAProps.OUTPUT_VTRIM.name, s);
			}
		}
		{
			double[] trims;
			String s = UAProps.OUTPUT_TRIMS.getString(ua);
			if (s != null) {
				String[] values = s.split("[\\s]+");
				if (values.length <= 0 || values.length > 4) {
					ua.message(MessageCodes.WARN_BAD_IO_PROPERTY, UAProps.OUTPUT_TRIMS.name, s);
					trims = null;
				} else {
					trims = new double[values.length];
					for (int i = 0; i < values.length; ++i) {
						AbsoluteLengthValue length = ValueUtils.toAbsoluteLength(ua, false, values[i]);
						if (length != null) {
							trims[i] = length.getLength();
						} else {
							ua.message(MessageCodes.WARN_BAD_IO_PROPERTY, UAProps.OUTPUT_TRIMS.name, s);
							trims = null;
							break;
						}
					}
				}
				switch (trims.length) {
				case 1:
					imposition.setTrims(trims[0], trims[0], trims[0], trims[0]);
					break;
				case 2:
					imposition.setTrims(trims[0], trims[1], trims[0], trims[1]);
					break;
				case 3:
					imposition.setTrims(trims[0], trims[1], trims[2], trims[1]);
					break;
				case 4:
					imposition.setTrims(trims[0], trims[1], trims[2], trims[3]);
					break;
				}
			}
		}

		// トンボ
		switch (UAProps.OUTPUT_MARKS.getCode(ua)) {
		case OutputMarks.NONE:
			imposition.setTrims(0, 0, 0, 0);
			imposition.setCuttingMargin(0);
			imposition.setNote(null);
			break;
		case OutputMarks.CROP:
			imposition.setCrop(true);
			imposition.setNote("page {0}");
			break;
		case OutputMarks.CROSS:
			imposition.setCross(true);
			imposition.setNote("page {0}");
			break;
		case OutputMarks.BOTH:
			imposition.setCrop(true);
			imposition.setCross(true);
			imposition.setNote("page {0}");
			break;
		case OutputMarks.HIDDEN:
			imposition.setNote("page {0}");
			break;
		default:
			throw new IllegalStateException();
		}
		imposition.setClip(UAProps.OUTPUT_CLIP.getBoolean(ua));

		// 背表紙
		{
			String s = UAProps.OUTPUT_MARKS_SPINE_WIDTH.getString(ua);
			if (s != null) {
				AbsoluteLengthValue length = ValueUtils.toAbsoluteLength(ua, false, s);
				if (length != null) {
					double l = length.getLength();
					imposition.setSpineWidth(l);
					if (imposition.getNote() != null) {
						imposition.setNote(imposition.getNote() + " / spine " + s);
					}
				} else {
					ua.message(MessageCodes.WARN_BAD_IO_PROPERTY, UAProps.OUTPUT_MARKS_SPINE_WIDTH.name, s);
				}
			}
		}

		{
			// 用紙幅
			String s = UAProps.OUTPUT_PAPER_WIDTH.getString(ua);
			if (s != null) {
				AbsoluteLengthValue length = ValueUtils.toAbsoluteLength(ua, false, s);
				if (length != null) {
					double l = length.getLength();
					imposition.setPaperWidth(l);
				} else {
					imposition.fitPaperWidth();
					ua.message(MessageCodes.WARN_BAD_IO_PROPERTY, UAProps.OUTPUT_PAPER_WIDTH.name, s);
				}
			} else {
				imposition.fitPaperWidth();
			}
		}

		{
			// 用紙高さ
			String s = UAProps.OUTPUT_PAPER_HEIGHT.getString(ua);
			if (s != null) {
				AbsoluteLengthValue length = ValueUtils.toAbsoluteLength(ua, false, s);
				if (length != null) {
					double l = length.getLength();
					imposition.setPaperHeight(l);
				} else {
					imposition.fitPaperHeight();
					ua.message(MessageCodes.WARN_BAD_IO_PROPERTY, UAProps.OUTPUT_PAPER_HEIGHT.name, s);
				}
			} else {
				imposition.fitPaperHeight();
			}
		}
	}

}