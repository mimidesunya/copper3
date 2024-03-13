package jp.cssj.homare.style.builder.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jp.cssj.homare.impl.css.lang.CSSJTextUnitizer;
import jp.cssj.homare.style.box.AbstractBlockBox;
import jp.cssj.homare.style.box.AbstractContainerBox;
import jp.cssj.homare.style.box.AbstractReplacedBox;
import jp.cssj.homare.style.box.AbstractStaticBlockBox;
import jp.cssj.homare.style.box.IBox;
import jp.cssj.homare.style.box.IFloatBox;
import jp.cssj.homare.style.box.IFlowBox;
import jp.cssj.homare.style.box.impl.AbsoluteBlockBox;
import jp.cssj.homare.style.box.impl.AbsoluteReplacedBox;
import jp.cssj.homare.style.box.impl.FloatBlockBox;
import jp.cssj.homare.style.box.impl.FlowBlockBox;
import jp.cssj.homare.style.box.impl.InlineBlockBox;
import jp.cssj.homare.style.box.impl.InlineBox;
import jp.cssj.homare.style.box.params.AbsolutePos;
import jp.cssj.homare.style.box.params.AbstractTextParams;
import jp.cssj.homare.style.box.params.BlockParams;
import jp.cssj.homare.style.box.params.Dimension;
import jp.cssj.homare.style.box.params.FlowPos;
import jp.cssj.homare.style.box.params.Length;
import jp.cssj.homare.style.box.params.Pos;
import jp.cssj.homare.style.box.params.Types;
import jp.cssj.homare.style.builder.Builder;
import jp.cssj.homare.style.builder.InlineQuad;
import jp.cssj.homare.style.builder.InlineQuad.InlineBlockQuad;
import jp.cssj.homare.style.builder.InlineQuad.InlineEndQuad;
import jp.cssj.homare.style.builder.InlineQuad.InlineReplacedQuad;
import jp.cssj.homare.style.builder.InlineQuad.InlineStartQuad;
import jp.cssj.homare.style.builder.LayoutStack;
import jp.cssj.homare.style.builder.TableBuilder;
import jp.cssj.homare.style.builder.TwoPass;
import jp.cssj.homare.style.util.ByteList;
import jp.cssj.homare.style.util.StyleUtils;
import jp.cssj.sakae.gc.font.FontMetrics;
import jp.cssj.sakae.gc.font.FontStyle;
import jp.cssj.sakae.gc.text.Element;
import jp.cssj.sakae.gc.text.FilterGlyphHandler;
import jp.cssj.sakae.gc.text.Quad;
import jp.cssj.sakae.gc.text.Text;
import jp.cssj.sakae.gc.text.TextImpl;
import jp.cssj.sakae.gc.text.layout.control.LineBreak;

public class TwoPassBlockBuilder implements Builder, LayoutStack, TwoPass {
	private static final boolean DEBUG = false;

	private static final byte TYPE_ELEMENT = 1;

	private static final byte TYPE_END_TEXT_BLOCK = 2;

	private static final byte TYPE_START_FLOW = 3;

	private static final byte TYPE_END_FLOW = 4;

	private static final byte TYPE_REPLACED = 5;

	private static final byte TYPE_STF_BLOCK = 6;

	private static final byte TYPE_ABSOLUTE_BLOCK = 7;

	private static final byte TYPE_TABLE = 9;

	protected final LayoutStack layoutStack;

	/**
	 * 最小行幅、最大行幅、最小ページ高さ
	 */
	private double minLineSize = 0, maxLineSize = 0, minPageSize = 0;

	private double maxStartFloatAdvance = 0, maxEndFloatAdvance = 0;

	private int columnCount = 1;

	private TextImpl text;

	/**
	 * 現在の行幅。
	 */
	private double lineAxis = 0;

	private double atomicLineSize = 0;

	private double letterSpacing = 0;

	private double textIndent;

	private boolean blockHead;

	/**
	 * 通常のフローのブロックボックスの枠部分の行方向の幅、ページ方向の幅。
	 */
	private double lineFrame = 0, pageFrame = 0;

	private LineBreak toLineFeed = null;

	private final List<AbstractContainerBox> flowStack = new ArrayList<AbstractContainerBox>();

	private final List<IBox> inlineStack = new ArrayList<IBox>();

	private final List<Object> recordObjects = new ArrayList<Object>();

	private final ByteList recordTypes = new ByteList();

	private final List<TwoPass> recordInlineBlocks = new ArrayList<TwoPass>();

	public TwoPassBlockBuilder(LayoutStack layoutStack, AbstractContainerBox containerBox) {
		this.layoutStack = layoutStack;
		this.flowStack.add(containerBox);
		this.textIndent = containerBox.getTextIndent();
		this.blockHead = true;
		this.letterSpacing = StyleUtils.computeLength(containerBox.getBlockParams().letterSpacing,
				this.getFlowBox().getLineSize());
	}

	public AbstractContainerBox getFixedWidthContextBox() {
		AbstractContainerBox box = this.getContextBox();
		if (box.getBlockParams().size.getWidthType() != Dimension.TYPE_AUTO) {
			return box;
		}
		switch (box.getPos().getType()) {
		case Pos.TYPE_PAGE:
		case Pos.TYPE_INLINE:
		case Pos.TYPE_FLOW:
		case Pos.TYPE_FLOAT:
		case Pos.TYPE_TABLE_CELL:
		case Pos.TYPE_TABLE_CAPTION:
			return this.layoutStack.getFixedWidthFlowBox();

		case Pos.TYPE_ABSOLUTE:
			return this.layoutStack.getFixedWidthContextBox();
		default:
			throw new IllegalStateException();
		}
	}

	public AbstractContainerBox getFixedHeightContextBox() {
		AbstractContainerBox box = this.getContextBox();
		if (box.getBlockParams().size.getHeightType() != Dimension.TYPE_AUTO) {
			return box;
		}
		switch (box.getPos().getType()) {
		case Pos.TYPE_PAGE:
		case Pos.TYPE_INLINE:
		case Pos.TYPE_FLOW:
		case Pos.TYPE_FLOAT:
		case Pos.TYPE_TABLE_CELL:
		case Pos.TYPE_TABLE_CAPTION:
			return this.layoutStack.getFixedHeightFlowBox();

		case Pos.TYPE_ABSOLUTE:
			return this.layoutStack.getFixedHeightContextBox();
		default:
			throw new IllegalStateException(String.valueOf(box.getPos().getType()));
		}
	}

	public double getFixedWidth() {
		double frameWidth = 0;
		for (int i = this.flowStack.size() - 1; i >= 1; --i) {
			AbstractContainerBox flowBox = (AbstractContainerBox) this.flowStack.get(i);
			frameWidth += flowBox.getFrame().getFrameWidth();
			if (flowBox.getBlockParams().size.getWidthType() != Dimension.TYPE_AUTO) {
				return flowBox.getWidth() - frameWidth;
			}
		}
		AbstractContainerBox box = this.getFixedWidthContextBox();
		if (box == null) {
			return 0;
		}
		return box.getInnerWidth() - frameWidth;
	}

	public AbstractContainerBox getFixedWidthFlowBox() {
		for (int i = this.flowStack.size() - 1; i >= 1; --i) {
			AbstractContainerBox flowBox = (AbstractContainerBox) this.flowStack.get(i);
			if (flowBox.getBlockParams().size.getWidthType() != Dimension.TYPE_AUTO) {
				return flowBox;
			}
		}
		return this.getFixedWidthContextBox();
	}

	public double getFixedHeight() {
		double flowHeight = 0;
		for (int i = this.flowStack.size() - 1; i >= 1; --i) {
			AbstractContainerBox flowBox = (AbstractContainerBox) this.flowStack.get(i);
			flowHeight += flowBox.getFrame().getFrameHeight();
			if (flowBox.getBlockParams().size.getHeightType() != Dimension.TYPE_AUTO) {
				return flowBox.getHeight() - flowHeight;
			}
		}
		AbstractContainerBox box = this.getFixedHeightContextBox();
		if (box == null) {
			return 0;
		}
		return box.getInnerHeight() - flowHeight;
	}

	public AbstractContainerBox getFixedHeightFlowBox() {
		for (int i = this.flowStack.size() - 1; i >= 1; --i) {
			AbstractContainerBox flowBox = (AbstractContainerBox) this.flowStack.get(i);
			if (flowBox.getBlockParams().size.getHeightType() != Dimension.TYPE_AUTO) {
				return flowBox;
			}
		}
		return this.getFixedHeightContextBox();
	}

	public RootBuilder getPageContext() {
		return this.layoutStack.getPageContext();
	}

	public Builder getParentBuilder() {
		return (Builder) this.layoutStack;
	}

	public double getMaxLineSize() {
		return this.maxLineSize;
	}

	public double getMinLineSize() {
		return this.minLineSize;
	}

	public double getMinPageSize() {
		return this.minPageSize;
	}

	public boolean isMain() {
		return false;
	}

	public boolean isTwoPass() {
		return true;
	}

	public AbstractContainerBox getContextBox() {
		if (this.flowStack != null) {
			for (int i = this.flowStack.size() - 1; i >= 1; --i) {
				AbstractContainerBox box = (AbstractContainerBox) this.flowStack.get(i);
				if (box.isContextBox()) {
					return box;
				}
			}
		}
		AbstractContainerBox box = (AbstractContainerBox) this.flowStack.get(0);
		if (this.layoutStack == null) {
			return box;
		}
		if (!box.isContextBox()) {
			return this.layoutStack.getContextBox();
		}
		return box;
	}

	public AbstractContainerBox getMulticolumnBox() {
		if (this.flowStack != null) {
			for (int i = this.flowStack.size() - 1; i >= 0; --i) {
				final AbstractContainerBox box = (AbstractContainerBox) this.flowStack.get(i);
				if (box.getColumnCount() > 1) {
					return box;
				}
			}
		}
		return null;
	}

	public AbstractContainerBox getRootBox() {
		return (AbstractContainerBox) this.flowStack.get(0);
	}

	public AbstractContainerBox getFlowBox() {
		return (AbstractContainerBox) this.flowStack.get(this.flowStack.size() - 1);
	}

	public void startFlowBlock(final FlowBlockBox flowBox) {
		// 通常のフローのブロックボックス
		assert this.inlineStack.isEmpty();
		AbstractContainerBox containerBox = this.getFlowBox();
		BlockParams params = containerBox.getBlockParams();
		FlowPos pos = (FlowPos) flowBox.getPos();
		this.clearFloatAdvance(pos.clear);

		flowBox.firstPassLayout(containerBox);
		double lineSize;
		if (StyleUtils.isVertical(params.flow)) {
			// 縦書き
			lineSize = this.lineFrame + flowBox.getHeight();
			this.lineFrame += flowBox.getFrame().getFrameHeight();
			this.pageFrame += flowBox.getFrame().getFrameWidth();
		} else {
			// 横書き
			lineSize = this.lineFrame + flowBox.getWidth();
			this.lineFrame += flowBox.getFrame().getFrameWidth();
			this.pageFrame += flowBox.getFrame().getFrameHeight();
		}
		assert !StyleUtils.isNone(this.lineFrame);
		if (flowBox.getColumnCount() > 0) {
			this.lineFrame += flowBox.getBlockParams().columns.gap * (flowBox.getColumnCount() - 1);
		}
		this.lineFrame *= this.columnCount;
		lineSize *= this.columnCount;
		if (this.lineFrame > this.minLineSize) {
			this.minLineSize = this.lineFrame;
		}
		if (this.pageFrame > this.minPageSize) {
			this.minPageSize = this.pageFrame;
		}
		if (lineSize > this.maxLineSize) {
			this.maxLineSize = lineSize;
		}
		this.textIndent = flowBox.getTextIndent();
		this.blockHead = true;

		this.flowStack.add(flowBox);
		this.recordTypes.add(TYPE_START_FLOW);
		this.recordObjects.add(flowBox);
		this.columnCount *= flowBox.getColumnCount();
		this.letterSpacing = StyleUtils.computeLength(flowBox.getBlockParams().letterSpacing,
				this.getFlowBox().getLineSize());
	}

	public void endFlowBlock() {
		// 通常のフローのブロックボックス
		assert this.inlineStack.isEmpty();
		AbstractBlockBox flowBox = (AbstractBlockBox) this.flowStack.remove(this.flowStack.size() - 1);
		AbstractContainerBox containerBox = this.getFlowBox();
		BlockParams params = containerBox.getBlockParams();
		BlockParams flowParams = flowBox.getBlockParams();
		this.columnCount /= flowBox.getColumnCount();
		this.lineFrame /= this.columnCount;
		if (flowBox.getColumnCount() > 0) {
			this.lineFrame -= flowBox.getBlockParams().columns.gap * (flowBox.getColumnCount() - 1);
		}

		switch (params.flow) {
		case AbstractTextParams.FLOW_TB:
			// 横書き
			this.lineFrame -= flowBox.getFrame().getFrameWidth();
			this.pageFrame -= flowBox.getFrame().getFrameHeight();
			if (flowParams.size.getWidthType() == Dimension.TYPE_ABSOLUTE) {
				// 固定幅フロー
				this.maxLineSize = this.minLineSize = flowBox.getWidth();
			}
			break;
		case AbstractTextParams.FLOW_LR:
		case AbstractTextParams.FLOW_RL:
			// 縦書き
			this.lineFrame -= flowBox.getFrame().getFrameHeight();
			this.pageFrame -= flowBox.getFrame().getFrameWidth();
			if (flowParams.size.getHeightType() == Dimension.TYPE_ABSOLUTE) {
				// 固定幅フロー
				this.maxLineSize = this.minLineSize = flowBox.getHeight();
			}
			break;
		default:
			throw new IllegalStateException();
		}

		assert !StyleUtils.isNone(this.lineFrame);
		this.recordTypes.add(TYPE_END_FLOW);
		this.recordObjects.add(flowBox);

		this.textIndent = 0;
		this.blockHead = false;
		this.letterSpacing = StyleUtils.computeLength(flowBox.getBlockParams().letterSpacing,
				this.getFlowBox().getLineSize());
	}

	public void addBound(IBox box) {
		AbstractReplacedBox replacedBox = (AbstractReplacedBox) box;
		switch (replacedBox.getPos().getType()) {
		case Pos.TYPE_FLOW: {
			// 静的・相対配置
			AbstractContainerBox containerBox = this.getFlowBox();
			IFlowBox flowBox = (IFlowBox) replacedBox;
			FlowPos pos = (FlowPos) flowBox.getPos();
			this.clearFloatAdvance(pos.clear);
			StyleUtils.calclateReplacedSize(this, replacedBox);

			double minLineAxis, maxLineAxis = 0, minPageAxis;
			BlockParams params = containerBox.getBlockParams();
			if (StyleUtils.isVertical(params.flow)) {
				// 縦書き
				minLineAxis = replacedBox.getHeight();
				minPageAxis = replacedBox.getWidth();
				if (replacedBox.getReplacedParams().size.getHeightType() == Length.TYPE_ABSOLUTE) {
					maxLineAxis = replacedBox.getReplacedParams().size.getHeight();
				}
			} else {
				// 横書き
				minLineAxis = replacedBox.getWidth();
				minPageAxis = replacedBox.getHeight();
				if (replacedBox.getReplacedParams().size.getWidthType() == Length.TYPE_ABSOLUTE) {
					maxLineAxis = replacedBox.getReplacedParams().size.getWidth();
				}
			}
			minPageAxis += this.pageFrame;
			minLineAxis *= this.columnCount;
			minLineAxis += this.lineFrame;
			
			maxLineAxis *= this.columnCount;
			maxLineAxis += this.lineFrame;

			assert !StyleUtils.isNone(minLineAxis);
			if (minLineAxis > this.minLineSize) {
				this.minLineSize = minLineAxis;
			}
			if (minPageAxis > this.minPageSize) {
				this.minPageSize = minPageAxis;
			}
			if (maxLineAxis > this.maxLineSize) {
				this.maxLineSize = maxLineAxis;
			}
		}
			break;
		case Pos.TYPE_FLOAT: {
			// 浮動体
			AbstractContainerBox containerBox = this.getFlowBox();
			IFloatBox floatingBox = (IFloatBox) replacedBox;
			this.clearFloatAdvance(floatingBox.getFloatPos().clear);
			StyleUtils.calclateReplacedSize(this, replacedBox);

			double minLineAxis, minPageAxis, maxLineAxis = 0;
			BlockParams params = containerBox.getBlockParams();
			if (StyleUtils.isVertical(params.flow)) {
				// 縦書き
				minLineAxis = replacedBox.getHeight();
				minPageAxis = replacedBox.getWidth();
				if (replacedBox.getReplacedParams().size.getHeightType() == Length.TYPE_ABSOLUTE) {
					maxLineAxis = replacedBox.getReplacedParams().size.getHeight();
				}
			} else {
				// 横書き
				minLineAxis = replacedBox.getWidth();
				minPageAxis = replacedBox.getHeight();
				if (replacedBox.getReplacedParams().size.getWidthType() == Length.TYPE_ABSOLUTE) {
					maxLineAxis = replacedBox.getReplacedParams().size.getWidth();
				}
			}
			assert !StyleUtils.isNone(minLineAxis);
			if (minLineAxis > this.minLineSize) {
				this.minLineSize = minLineAxis;
			}
			if (minPageAxis > this.minPageSize) {
				this.minPageSize = minPageAxis;
			}

			switch (floatingBox.getFloatPos().floating) {
			case Types.FLOATING_START: {
				this.maxStartFloatAdvance += minLineAxis;
			}
				break;
			case Types.FLOATING_END: {
				this.maxEndFloatAdvance += minLineAxis;
			}
				break;
			default:
				throw new IllegalStateException();
			}
			double xmaxLineAxis = this.maxStartFloatAdvance + this.maxEndFloatAdvance;
			if (xmaxLineAxis > maxLineAxis) {
				maxLineAxis = xmaxLineAxis;
			}
			maxLineAxis *= this.columnCount;
			maxLineAxis += this.lineFrame;
			if (maxLineAxis > this.maxLineSize) {
				this.maxLineSize = maxLineAxis;
			}
		}
			break;

		case Pos.TYPE_ABSOLUTE:
			// 絶対配置
			AbstractContainerBox contextBox;
			switch (((AbsoluteReplacedBox) replacedBox).getAbsolutePos().fiducial) {
			case Types.FODUCIAL_CONTEXT:
			case Types.FODUCIAL_ALL_PAGE: {
				// 通常の絶対配置
				// 固定配置
				contextBox = this.getFlowBox();
			}
				break;
			case Types.FODUCIAL_CURRENT_PAGE: {
				// ページコンテンツ
				contextBox = this.getPageContext().getRootBox();
			}
				break;
			default:
				throw new IllegalStateException();
			}
			replacedBox.calculateFrame(contextBox.getLineSize());
			break;

		default:
			throw new IllegalStateException();
		}
		this.recordTypes.add(TYPE_REPLACED);
		this.recordObjects.add(replacedBox);
	}

	public void addTable(TableBuilder tableBuilder) {
		TwoPassTableBuilder autoTableBuilder = (TwoPassTableBuilder) tableBuilder;
		autoTableBuilder.prepareLayout();
		double maxLineAxis = autoTableBuilder.getMinLineSize();
		double minLineAxis = autoTableBuilder.getMaxLineSize();
		maxLineAxis *= this.columnCount;
		minLineAxis *= this.columnCount;
		this.minLineSize = Math.max(this.minLineSize, maxLineAxis);
		this.maxLineSize = Math.max(this.maxLineSize, minLineAxis);
		this.recordTypes.add(TYPE_TABLE);
		this.recordObjects.add(tableBuilder);
		switch (autoTableBuilder.getTableBox().getBlockBox().getPos().getType()) {
		case Pos.TYPE_INLINE:
			this.recordInlineBlocks.add(autoTableBuilder);
			break;
		}
	}

	public Builder newBuilder(final AbstractBlockBox stfBox) {
		// * TODO 絶対幅の場合はBoundContainerContextが使えますが、
		// * 絶対配置の位置調整を構築後に行わないといけないため
		// * そのままにしています。
		final TwoPassBlockBuilder builder = new TwoPassBlockBuilder(this, stfBox);
		final AbstractContainerBox box = this.getFlowBox();
		stfBox.firstPassLayout(box);
		switch (stfBox.getPos().getType()) {
		case Pos.TYPE_FLOW:
			// 書字方向が違う
		case Pos.TYPE_FLOAT:
			// 浮動体
			this.recordTypes.add(TYPE_STF_BLOCK);
			this.recordObjects.add(builder);
			break;

		case Pos.TYPE_ABSOLUTE:
			// 絶対配置
			this.recordTypes.add(TYPE_ABSOLUTE_BLOCK);
			this.recordObjects.add(builder);
			break;

		case Pos.TYPE_INLINE:
			// インラインブロック
			this.recordInlineBlocks.add(builder);
			break;

		default:
			throw new IllegalStateException();
		}
		return builder;
	}

	public void fitFloating(TwoPassBlockBuilder childBuilder) {
		FloatBlockBox floatingBox = (FloatBlockBox) childBuilder.getRootBox();
		this.clearFloatAdvance(floatingBox.getFloatPos().clear);

		BlockParams params = floatingBox.getBlockParams();
		BlockParams flowParams = this.getFlowBox().getBlockParams();
		double minLineAxis, maxLineAxis;
		if (StyleUtils.isVertical(flowParams.flow)) {
			// 縦書き
			if (params.size.getHeightType() != Dimension.TYPE_AUTO) {
				minLineAxis = maxLineAxis = floatingBox.getHeight();
			} else {
				minLineAxis = childBuilder.getMinLineSize() + floatingBox.getFrame().getFrameWidth();
				maxLineAxis = childBuilder.getMaxLineSize() + floatingBox.getFrame().getFrameHeight();
			}
		} else {
			// 横書き
			if (params.size.getWidthType() != Dimension.TYPE_AUTO) {
				minLineAxis = maxLineAxis = floatingBox.getWidth();
			} else {
				minLineAxis = childBuilder.getMinLineSize() + floatingBox.getFrame().getFrameWidth();
				maxLineAxis = childBuilder.getMaxLineSize() + floatingBox.getFrame().getFrameWidth();
			}
		}
		assert !StyleUtils.isNone(maxLineAxis);
		// System.err.println(this.minLineAxis + "/" + this.maxLineAxis);
		if (minLineAxis > this.minLineSize) {
			this.minLineSize = minLineAxis;
		}

		switch (floatingBox.getFloatPos().floating) {
		case Types.FLOATING_START:
			this.maxStartFloatAdvance += maxLineAxis;
			break;
		case Types.FLOATING_END:
			this.maxEndFloatAdvance += maxLineAxis;
			break;
		default:
			throw new IllegalStateException();
		}
		maxLineAxis = this.maxStartFloatAdvance + this.maxEndFloatAdvance;
		maxLineAxis *= this.columnCount;
		maxLineAxis += this.lineFrame;
		if (maxLineAxis > this.maxLineSize) {
			this.maxLineSize = maxLineAxis;
		}
	}

	public void bind(BlockBuilder builder) {
		// 再レイアウト
		if (DEBUG) {
			System.err.println("BIND");
		}
		FilterGlyphHandler textUnitizer = null;
		final Iterator<Object> k = this.recordObjects.iterator();

		for (int i = 0; i < this.recordTypes.size(); ++i) {
			switch (this.recordTypes.get(i)) {
			case TYPE_ELEMENT: {
				if (textUnitizer == null) {
					textUnitizer = new CSSJTextUnitizer(builder.getFlowBox().getBlockParams().hyphenation);
					textUnitizer.setGlyphHandler(new BuilderGlyphHandler(builder));
				}
				final Element e = (Element) k.next();
				if (DEBUG) {
					System.err.println("ELEMENT " + e);
				}
				switch (e.getElementType()) {
				case Element.TEXT:
					final Text text = ((Text) e);
					assert text.getGLen() > 0;
					text.toGlyphs(textUnitizer);
					break;
				case Element.QUAD:
					final Quad quad = (Quad) e;
					if (quad instanceof InlineBlockQuad) {
						final TwoPass twoPass = (TwoPass) this.recordInlineBlocks.remove(0);
						if (twoPass instanceof TwoPassBlockBuilder) {
							final InlineBlockQuad inlineBlockQuad = (InlineBlockQuad) quad;
							final InlineBlockBox inlineBlockBox = inlineBlockQuad.box;
							final TwoPassBlockBuilder stfBuilder = (TwoPassBlockBuilder) twoPass;
							inlineBlockBox.shrinkToFit(builder, stfBuilder.getMinLineSize(),
									stfBuilder.getMaxLineSize(), false);
							final BlockBuilder lnlineBlockBuilder = new BlockBuilder(this, inlineBlockBox);
							stfBuilder.bind(lnlineBlockBuilder);
							lnlineBlockBuilder.finish();
						}
					}
					textUnitizer.quad(quad);
					break;
				default:
					throw new IllegalStateException();
				}
			}
				break;

			case TYPE_END_TEXT_BLOCK:
				if (DEBUG) {
					System.err.println("END_TEXT_BLOCK");
				}
				textUnitizer.finish();
				textUnitizer = null;
				builder.endTextBlock();
				break;

			case TYPE_START_FLOW: {
				if (DEBUG) {
					System.err.println("START_FLOW");
				}
				final FlowBlockBox flow = (FlowBlockBox) k.next();
				if (StyleUtils.isVertical(flow.getBlockParams().flow) == StyleUtils
						.isVertical(builder.getRootBox().getBlockParams().flow)) {
					builder.startFlowBlock(flow);
				} else {
					// 書字方向が違う場合
					builder = (BlockBuilder) builder.newBuilder(flow);
				}
			}
				break;

			case TYPE_END_FLOW:
				if (DEBUG) {
					System.err.println("END_FLOW");
				}
				final FlowBlockBox flow = (FlowBlockBox) k.next();
				if (flow == builder.getRootBox()) {
					builder = (BlockBuilder) builder.getParentBuilder();
					builder.addBound(flow);
				} else {
					builder.endFlowBlock();
				}
				break;

			case TYPE_REPLACED: {
				if (DEBUG) {
					System.err.println("REPLACED");
				}
				final IBox replacedBox = (IBox) k.next();
				switch (replacedBox.getPos().getType()) {
				case Pos.TYPE_FLOAT:
				case Pos.TYPE_FLOW:
					if (textUnitizer != null) {
						textUnitizer.flush();
					}
				}
				builder.addBound(replacedBox);
			}
				break;

			case TYPE_STF_BLOCK: {
				if (DEBUG) {
					System.err.println("STF");
				}
				if (textUnitizer != null) {
					textUnitizer.flush();
				}
				final TwoPassBlockBuilder stfBuilder = (TwoPassBlockBuilder) k.next();
				final AbstractStaticBlockBox blockBox = (AbstractStaticBlockBox) stfBuilder.getRootBox();
				blockBox.shrinkToFit(builder, stfBuilder.getMinLineSize(), stfBuilder.getMaxLineSize(), false);
				final BlockBuilder boundBuilder = new BlockBuilder(this, blockBox);
				stfBuilder.bind(boundBuilder);
				boundBuilder.finish();
				builder.addBound(blockBox);
			}
				break;

			case TYPE_ABSOLUTE_BLOCK: {
				if (DEBUG) {
					System.err.println("ABSOLUTE");
				}

				final TwoPassBlockBuilder stfBuilder = (TwoPassBlockBuilder) k.next();
				final AbsoluteBlockBox absoluteBox = (AbsoluteBlockBox) stfBuilder.getRootBox();
				final AbstractContainerBox containerBox;
				if (absoluteBox.getAbsolutePos().fiducial != Types.FODUCIAL_CONTEXT) {
					containerBox = builder.getPageContext().getRootBox();
				} else {
					containerBox = builder.getContextBox();
				}
				absoluteBox.shrinkToFit(containerBox, stfBuilder.getMinLineSize(), stfBuilder.getMaxLineSize());
				final BlockBuilder boundBuilder = new BlockBuilder(this, absoluteBox);
				stfBuilder.bind(boundBuilder);
				boundBuilder.finish();
				final AbsolutePos pos = absoluteBox.getAbsolutePos();
				switch (pos.autoPosition) {
				case Types.AUTO_POSITION_BLOCK:
					builder.addBound(absoluteBox);
					break;
				case Types.AUTO_POSITION_INLINE:
					final Quad quad = InlineQuad.createInlineAbsoluteBoxQuad(absoluteBox);
					if (textUnitizer == null) {
						textUnitizer = new CSSJTextUnitizer(builder.getFlowBox().getBlockParams().hyphenation);
						textUnitizer.setGlyphHandler(new BuilderGlyphHandler(builder));
					}
					textUnitizer.quad(quad);
					break;
				default:
					throw new IllegalStateException();
				}
			}
				break;

			case TYPE_TABLE: {
				if (DEBUG) {
					System.err.println("TABLE");
				}
				TwoPassTableBuilder tableBuilder = (TwoPassTableBuilder) k.next();
				switch (tableBuilder.getTableBox().getBlockBox().getPos().getType()) {
				case Pos.TYPE_FLOAT:
				case Pos.TYPE_FLOW:
					if (textUnitizer != null) {
						textUnitizer.flush();
					}
				}
				tableBuilder.bind(builder);
			}
				break;

			default:
				throw new IllegalStateException();
			}
		}
		if (DEBUG) {
			System.err.println("/FINISH");
		}
	}

	private double getCurrentLineHeight() {
		if (this.inlineStack.isEmpty()) {
			return this.getFlowBox().getBlockParams().lineHeight;
		}
		InlineBox box = (InlineBox) this.inlineStack.get(this.inlineStack.size() - 1);
		return box.getInlinePos().lineHeight;
	}

	public void startTextRun(int charOffset, final FontStyle fontStyle, final FontMetrics fontMetrics) {
		this.text = new TextImpl(charOffset, fontStyle, fontMetrics);
	}

	public void glyph(int charOffset, char[] ch, int coff, byte clen, int gid) {
		double advance = this.text.appendGlyph(ch, coff, clen, gid);
		advance += this.letterSpacing;
		this.atomicLineSize += advance;
		this.lineAxis += advance;
		double minPageAxis = this.getCurrentLineHeight() + this.pageFrame;
		if (minPageAxis > this.minPageSize) {
			this.minPageSize = minPageAxis;
		}
	}

	public void endTextRun() {
		this.text.pack();
		this.recordTypes.add(TYPE_ELEMENT);
		this.recordObjects.add(this.text);
		this.text = null;
	}

	public void quad(final Quad quad) {
		if (quad instanceof LineBreak) {
			this.toLineFeed = (LineBreak) quad;
		}
		this.recordTypes.add(TYPE_ELEMENT);
		this.recordObjects.add(quad);

		double minAdvance, maxAdvance, pageSize;
		if (quad instanceof InlineQuad) {
			final InlineQuad inlineQuad = (InlineQuad) quad;
			final BlockParams cParams = this.getFlowBox().getBlockParams();
			if (quad instanceof InlineReplacedQuad) {
				// 画像
				final AbstractReplacedBox box = (AbstractReplacedBox) inlineQuad.getBox();
				maxAdvance = quad.getAdvance();
				minAdvance = 0;
				if (StyleUtils.isVertical(cParams.flow)) {
					// 縦書き
					if (box.getReplacedParams().size.getHeightType() != Dimension.TYPE_RELATIVE
							&& box.getReplacedParams().maxSize.getHeightType() != Dimension.TYPE_RELATIVE) {
						minAdvance = maxAdvance;
					}
					if (box.getReplacedParams().size.getHeightType() == Dimension.TYPE_ABSOLUTE) {
						if(box.getReplacedParams().size.getHeight() > maxAdvance) {
							maxAdvance = box.getReplacedParams().size.getHeight();
						}
					}	
					pageSize = box.getWidth();
				} else {
					// 横書き
					if (box.getReplacedParams().size.getWidthType() != Dimension.TYPE_RELATIVE
							&& box.getReplacedParams().maxSize.getWidthType() != Dimension.TYPE_RELATIVE) {
						minAdvance = maxAdvance;
					}
					if (box.getReplacedParams().size.getWidthType() == Dimension.TYPE_ABSOLUTE) {
						if(box.getReplacedParams().size.getWidth() > maxAdvance) {
							maxAdvance = box.getReplacedParams().size.getWidth();
						}
					}	
					pageSize = box.getHeight();
				}
			} else if (quad instanceof InlineBlockQuad) {
				// インラインブロック
				final AbstractContainerBox box = (AbstractContainerBox) inlineQuad.getBox();
				final double lineFrame, pageFrame;
				if (StyleUtils.isVertical(cParams.flow)) {
					// 縦書き
					lineFrame = box.getFrame().getFrameHeight();
					pageFrame = box.getFrame().getFrameWidth();
				} else {
					// 横書き
					lineFrame = box.getFrame().getFrameWidth();
					pageFrame = box.getFrame().getFrameHeight();
				}
				// インラインブロック
				final BlockParams params = (BlockParams) box.getParams();
				final TwoPass stfBuilder = (TwoPass) this.recordInlineBlocks.get(this.recordInlineBlocks.size() - 1);
				if (StyleUtils.isVertical(cParams.flow) == StyleUtils.isVertical(params.flow)) {
					minAdvance = stfBuilder.getMinLineSize() + lineFrame;
					maxAdvance = stfBuilder.getMaxLineSize() + lineFrame;
					pageSize = stfBuilder.getMinPageSize() + pageFrame;
				} else {
					// 縦中横/横中縦
					minAdvance = maxAdvance = stfBuilder.getMinPageSize() + pageFrame;
					pageSize = stfBuilder.getMinLineSize() + lineFrame;
				}
				if (StyleUtils.isVertical(params.flow)) {
					// 縦書き
					minAdvance = Math.max(minAdvance, box.getHeight());
					maxAdvance = Math.max(maxAdvance, box.getHeight());
					pageSize = Math.max(pageSize, box.getWidth());
				} else {
					// 横書き
					minAdvance = Math.max(minAdvance, box.getWidth());
					maxAdvance = Math.max(maxAdvance, box.getWidth());
					pageSize = Math.max(pageSize, box.getHeight());
				}
			} else {
				if (inlineQuad instanceof InlineStartQuad) {
					this.inlineStack.add(inlineQuad.getBox());
					final InlineStartQuad inlineStartQuad = (InlineStartQuad) inlineQuad;
					this.letterSpacing = StyleUtils.computeLength(inlineStartQuad.box.getTextParams().letterSpacing,
							this.getFlowBox().getLineSize());
				} else if (inlineQuad instanceof InlineEndQuad) {
					this.inlineStack.remove(this.inlineStack.size() - 1);
					AbstractTextParams params;
					if (this.inlineStack.isEmpty()) {
						params = this.getFlowBox().getBlockParams();
					} else {
						final InlineBox box = (InlineBox) this.inlineStack.get(this.inlineStack.size() - 1);
						params = box.getTextParams();
					}
					this.letterSpacing = StyleUtils.computeLength(params.letterSpacing,
							this.getFlowBox().getLineSize());
				}
				minAdvance = maxAdvance = quad.getAdvance();
				if (StyleUtils.isVertical(cParams.flow)) {
					pageSize = inlineQuad.getBox().getWidth();
				} else {
					pageSize = inlineQuad.getBox().getHeight();
				}
			}
		} else {
			minAdvance = maxAdvance = quad.getAdvance();
			pageSize = 0;
		}
		pageSize = Math.max(pageSize, this.getCurrentLineHeight());
		pageSize += this.pageFrame;
		if (pageSize > this.minPageSize) {
			this.minPageSize = pageSize;
		}
		this.atomicLineSize += minAdvance;
		this.lineAxis += maxAdvance;
	}

	public void flush() {
		double minLineSize = this.atomicLineSize;
		if (this.blockHead) {
			minLineSize += this.textIndent;
			this.blockHead = false;
		}
		minLineSize *= this.columnCount;
		minLineSize += this.lineFrame;
		if (minLineSize > this.minLineSize) {
			this.minLineSize = minLineSize;
			if (minLineSize > this.maxLineSize) {
				this.maxLineSize = minLineSize;
			}
		}
		this.atomicLineSize = 0;
		if (this.toLineFeed != null) {
			assert !StyleUtils.isNone(this.lineAxis);
			assert !StyleUtils.isNone(this.lineFrame);
			double maxLineSize = this.textIndent + this.maxStartFloatAdvance + this.maxEndFloatAdvance + this.lineAxis;
			maxLineSize *= this.columnCount;
			maxLineSize += this.lineFrame;
			if (maxLineSize > this.maxLineSize) {
				this.maxLineSize = maxLineSize;
			}
			this.lineAxis = 0;
			this.toLineFeed = null;
			this.textIndent = 0;
			this.clearFloatAdvance(Types.CLEAR_BOTH);
		}
	}
	
	public void finish() {
		this.flush();
	}

	public void endTextBlock() {
		this.recordTypes.add(TYPE_END_TEXT_BLOCK);
		assert !StyleUtils.isNone(this.lineAxis);
		assert !StyleUtils.isNone(this.lineFrame);
		double minLineSize = this.atomicLineSize;
		if (this.blockHead) {
			minLineSize += this.textIndent;
			this.blockHead = false;
		}
		minLineSize *= this.columnCount;
		minLineSize += this.lineFrame;
		if (minLineSize > this.minLineSize) {
			this.minLineSize = minLineSize;
		}
		double maxLineSize = this.textIndent + this.maxStartFloatAdvance + this.maxEndFloatAdvance + this.lineAxis;
		maxLineSize *= this.columnCount;
		maxLineSize += this.lineFrame;
		if (maxLineSize > this.maxLineSize) {
			this.maxLineSize = maxLineSize;
		}
		this.atomicLineSize = 0;
		this.lineAxis = 0;
	}

	private void clearFloatAdvance(byte clear) {
		switch (clear) {
		case Types.CLEAR_BOTH:
			this.maxStartFloatAdvance = 0;
			this.maxEndFloatAdvance = 0;
			break;
		case Types.CLEAR_START:
			this.maxStartFloatAdvance = 0;
			break;
		case Types.CLEAR_END:
			this.maxEndFloatAdvance = 0;
			break;
		case Types.CLEAR_NONE:
			break;
		default:
			throw new IllegalStateException();
		}
	}

	public boolean isEmpty() {
		return this.recordTypes.isEmpty();
	}
}
