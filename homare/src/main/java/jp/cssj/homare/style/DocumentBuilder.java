package jp.cssj.homare.style;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.List;

import jp.cssj.homare.style.box.AbstractBlockBox;
import jp.cssj.homare.style.box.AbstractContainerBox;
import jp.cssj.homare.style.box.AbstractInnerTableBox;
import jp.cssj.homare.style.box.AbstractReplacedBox;
import jp.cssj.homare.style.box.IAbsoluteBox;
import jp.cssj.homare.style.box.IBox;
import jp.cssj.homare.style.box.IFramedBox;
import jp.cssj.homare.style.box.INonReplacedBox;
import jp.cssj.homare.style.box.impl.AbsoluteBlockBox;
import jp.cssj.homare.style.box.impl.FloatBlockBox;
import jp.cssj.homare.style.box.impl.FlowBlockBox;
import jp.cssj.homare.style.box.impl.FlowReplacedBox;
import jp.cssj.homare.style.box.impl.InlineBlockBox;
import jp.cssj.homare.style.box.impl.InlineBox;
import jp.cssj.homare.style.box.impl.MulticolumnBlockBox;
import jp.cssj.homare.style.box.impl.RubyBox;
import jp.cssj.homare.style.box.impl.TableBox;
import jp.cssj.homare.style.box.params.AbsolutePos;
import jp.cssj.homare.style.box.params.BlockParams;
import jp.cssj.homare.style.box.params.Columns;
import jp.cssj.homare.style.box.params.FloatPos;
import jp.cssj.homare.style.box.params.FlowPos;
import jp.cssj.homare.style.box.params.Params;
import jp.cssj.homare.style.box.params.Pos;
import jp.cssj.homare.style.box.params.TableParams;
import jp.cssj.homare.style.box.params.Types;
import jp.cssj.homare.style.builder.Builder;
import jp.cssj.homare.style.builder.PageGenerator;
import jp.cssj.homare.style.builder.TableBuilder;
import jp.cssj.homare.style.builder.impl.BlockBuilder;
import jp.cssj.homare.style.builder.impl.BreakableBuilder;
import jp.cssj.homare.style.builder.impl.OnePassTableBuilder;
import jp.cssj.homare.style.builder.impl.RootBuilder;
import jp.cssj.homare.style.builder.impl.StyledTextUnitizer;
import jp.cssj.homare.style.builder.impl.TwoPassBlockBuilder;
import jp.cssj.homare.style.builder.impl.TwoPassTableBuilder;
import jp.cssj.homare.style.util.StyleUtils;
import jp.cssj.homare.ua.props.UAProps;
import jp.cssj.sakae.util.NumberUtils;

/**
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: DocumentBuilder.java 1622 2022-05-02 06:22:56Z miyabe $
 */
public class DocumentBuilder {
	private static final boolean DEBUG = false;

	public static final byte PAGE_MODE_CONTINUOUS = 1;
	public static final byte PAGE_MODE_NO_BREAK = 1 << 1;
	
	private final boolean normalizeText;

	protected static class ContainerBuilderEntry {
		public final Builder builder;

		protected StyledTextUnitizer styledTextUnitizer = null;

		public ContainerBuilderEntry(Builder builder) {
			this.builder = builder;
		}

		/**
		 * テキスト出力のためのインタフェースを返します。
		 * 
		 * @return
		 */
		public StyledTextUnitizer getStyledTextUnitizer() {
			if (this.styledTextUnitizer == null) {
				this.styledTextUnitizer = new StyledTextUnitizer(this.builder);
			}
			return this.styledTextUnitizer;
		}
	}

	/**
	 * ページ生成オブジェクト。
	 */
	private final PageGenerator pageGenerator;

	private byte pageMode = 0;

	private final List<INonReplacedBox> boxStack = new ArrayList<INonReplacedBox>();

	private final List<Object> builderStack = new ArrayList<Object>();

	private final List<Object> inlineStack = new ArrayList<Object>();

	private final List<Object> columnSpanStack = new ArrayList<Object>();

	public DocumentBuilder(PageGenerator pageGenerator) {
		this.pageGenerator = pageGenerator;
		this.normalizeText = UAProps.INPUT_NORMALIZE_TEXT.getBoolean(pageGenerator.getUserAgent());
	}

	public void setPageMode(byte pageMode) {
		this.pageMode = pageMode;
	}

	public byte getPageMode() {
		return this.pageMode;
	}

	private void requirePage() {
		if (!this.builderStack.isEmpty()) {
			return;
		}
		byte mode = (this.pageMode & (PAGE_MODE_CONTINUOUS | PAGE_MODE_NO_BREAK)) != 0 ? BreakableBuilder.MODE_NO_BREAK
				: BreakableBuilder.MODE_PAGE_BREAK;
		BlockBuilder builder = new RootBuilder(this.pageGenerator, mode);
		this.startContainerBuilder(builder);
		this.startContainer();
	}

	private void startContainerBuilder(Builder builder) {
		this.builderStack.add(new ContainerBuilderEntry(builder));
	}

	private ContainerBuilderEntry containerBuilder() {
		int index = this.builderStack.size() - 1;
		Object o = this.builderStack.get(index);
		while (o instanceof TableBuilder) {
			// テーブル内でセル外のinline, block, テキスト等をテーブルの前に置くため
			// 一般的なブラウザの動作による
			--index;
			o = this.builderStack.get(index);
		}
		return (ContainerBuilderEntry) o;
	}

	private ContainerBuilderEntry contextBuilder() {
		for (int i = this.builderStack.size() - 1; i >= 0; --i) {
			Object entry = this.builderStack.get(i);
			if (entry instanceof ContainerBuilderEntry) {
				return (ContainerBuilderEntry) entry;
			}
		}
		throw new ArrayIndexOutOfBoundsException();
	}

	private RootBuilder pageContextBuilder() {
		return (RootBuilder) ((ContainerBuilderEntry) this.builderStack.get(0)).builder;
	}

	private ContainerBuilderEntry endContainerBuilder() {
		ContainerBuilderEntry entry = this.containerBuilder();
		if (!entry.builder.isTwoPass()) {
			((BlockBuilder) entry.builder).finish();
		}
		this.builderStack.remove(this.builderStack.size() - 1);
		return entry;
	}

	private TableBuilder tableBuilder() {
		TableBuilder tableBuilder = (TableBuilder) this.builderStack.get(this.builderStack.size() - 1);
		return tableBuilder;
	}

	private TableBuilder endTableBuilder() {
		TableBuilder builder = (TableBuilder) this.builderStack.remove(this.builderStack.size() - 1);
		return builder;
	}

	private void closeInlines(Params params) {
		int count = 0;

		for (int i = this.boxStack.size() - 1; i >= 0; --i) {
			final IBox box = (IBox) this.boxStack.get(i);
			if (box.getType() != IBox.TYPE_INLINE) {
				break;
			}
			this.endBox();
			this.inlineStack.add(box);
			++count;
		}
		if (count > 0) {
			this.inlineStack.add(NumberUtils.intValue(count));
			this.inlineStack.add(params);
		}
		if (DEBUG) {
			System.err.println(count + ":" + params.element);
		}
	}

	private void restoreInlines(Params params) {
		if (DEBUG) {
			System.err.println("/:" + params.element);
		}
		if (this.inlineStack.isEmpty() || this.inlineStack.get(this.inlineStack.size() - 1) != params) {
			return;
		}
		this.inlineStack.remove(this.inlineStack.size() - 1);
		final Integer count = (Integer) this.inlineStack.remove(this.inlineStack.size() - 1);
		for (int i = 0; i < count.intValue(); ++i) {
			InlineBox box = (InlineBox) this.inlineStack.remove(this.inlineStack.size() - 1);
			box = box.splitLine(false);
			this.startBox(box);
		}
	}

	private void startColumnSpan(FlowPos pos) {
		final Builder builder = this.containerBuilder().builder;
		if (builder.getMulticolumnBox() == null) {
			return;
		}

		final List<AbstractBlockBox> flows = new ArrayList<AbstractBlockBox>();
		for (;;) {
			final AbstractBlockBox blockBox = (AbstractBlockBox) builder.getFlowBox();
			flows.add(blockBox);
			if (blockBox.getColumnCount() > 1) {
				final BlockParams colParams = blockBox.getBlockParams();
				final Columns oldColumns = colParams.columns;
				colParams.columns = new Columns(colParams.columns.count, colParams.columns.width, colParams.columns.gap,
						colParams.columns.rule, Columns.FILL_BALANCE);
				this.endContainer();
				builder.endFlowBlock();
				this.startContainer();
				this.restoreInlines(blockBox.getParams());
				colParams.columns = oldColumns;
				break;
			} else {
				this.endContainer();
				builder.endFlowBlock();
				this.startContainer();
				this.restoreInlines(blockBox.getParams());
			}
		}
		this.columnSpanStack.add(flows);
		this.columnSpanStack.add(pos);
	}

	private void endColumnSpan(FlowPos pos) {
		if (this.columnSpanStack.isEmpty() || this.columnSpanStack.get(this.columnSpanStack.size() - 1) != pos) {
			return;
		}
		final Builder builder = this.containerBuilder().builder;
		this.columnSpanStack.remove(this.columnSpanStack.size() - 1);
		final List<?> flows = (List<?>) this.columnSpanStack.remove(this.columnSpanStack.size() - 1);
		for (int i = flows.size() - 1; i >= 0; --i) {
			FlowBlockBox flowBox = (FlowBlockBox) flows.get(i);
			this.closeInlines(flowBox.getBlockParams());
			this.endContainer();
			if (flowBox.getColumnCount() > 1) {
				flowBox = new MulticolumnBlockBox(flowBox.getBlockParams(), flowBox.getFlowPos());
			} else {
				flowBox = new FlowBlockBox(flowBox.getBlockParams(), flowBox.getFlowPos());
			}
			builder.startFlowBlock(flowBox);
			this.startContainer();
		}
	}

	private void startContainer() {
		final ContainerBuilderEntry cbe = this.containerBuilder();
		cbe.getStyledTextUnitizer().startContainer();
	}

	private void endContainer() {
		final ContainerBuilderEntry cbe = this.containerBuilder();
		cbe.getStyledTextUnitizer().endContainer();
	}

	public void startBox(final INonReplacedBox box) {
		if (DEBUG) {
			System.err.println("startBox: " + box.getParams().element);
		}
		this.requirePage();
		switch (box.getPos().getType()) {
		case Pos.TYPE_TABLE: {
			// テーブル
			final TableBox tableBox = (TableBox) box;
			final TableParams tableParams = tableBox.getTableParams();
			final Builder builder = this.containerBuilder().builder;
			switch (tableBox.getBlockBox().getPos().getType()) {
			case Pos.TYPE_FLOW:
				this.closeInlines(tableParams);
				this.endContainer();
				this.startContainer();
				break;
			}
			final TableBuilder tableBuilder;
			if (!builder.isMain() || StyleUtils.isTwoPassTable(tableBox)) {
				// 2パスレイアウト
				tableBuilder = new TwoPassTableBuilder(builder, tableBox);
			} else {
				// 1パスレイアウト
				final OnePassTableBuilder fixedTableBuilder = new OnePassTableBuilder(tableBox);
				fixedTableBuilder.startLayout((RootBuilder) builder);
				tableBuilder = fixedTableBuilder;
			}
			this.builderStack.add(tableBuilder);
		}
			break;

		case Pos.TYPE_TABLE_CELL:
		case Pos.TYPE_TABLE_CAPTION: {
			// テーブルセル
			// キャプション
			final TableBuilder tableBuiler = this.tableBuilder();
			if (tableBuiler.isOnePass()) {
				this.closeInlines(tableBuiler.getTableBox().getParams());
				this.endContainer();
				this.startContainer();
			}
			final AbstractContainerBox containerBox = (AbstractContainerBox) box;
			final Builder newBuilder = tableBuiler.newContext(containerBox);
			this.startContainerBuilder(newBuilder);
			this.startContainer();
		}
			break;

		case Pos.TYPE_TABLE_COLUMN:
		case Pos.TYPE_TABLE_ROW_GROUP:
		case Pos.TYPE_TABLE_ROW: {
			// テーブルカラムグループ
			// テーブルカラム
			// テーブル行グループ
			// テーブル行
			final TableBuilder tableBuiler = this.tableBuilder();
			if (tableBuiler.isOnePass()) {
				this.closeInlines(tableBuiler.getTableBox().getParams());
				this.endContainer();
			}
			final AbstractInnerTableBox innerTableBox = (AbstractInnerTableBox) box;
			tableBuiler.startInnerTable(innerTableBox);
			if (tableBuiler.isOnePass()) {
				this.startContainer();
			}
		}
			break;

		case Pos.TYPE_INLINE: {
			// インライン
			if (box.getType() == IBox.TYPE_INLINE) {
				final InlineBox inlineBox = (InlineBox) box;
				this.containerBuilder().getStyledTextUnitizer().startInline(inlineBox);
			} else {
				// インラインブロック
				final InlineBlockBox inlineBlockBox = (InlineBlockBox) box;
				final Builder builder = this.containerBuilder().builder;
				final Builder newBuilder = builder.newBuilder(inlineBlockBox);
				this.startContainerBuilder(newBuilder);
				this.startContainer();
			}
		}
			break;

		case Pos.TYPE_FLOW: {
			// 通常のフローのボックス
			final FlowBlockBox blockBox = (FlowBlockBox) box;
			final BlockParams params = blockBox.getBlockParams();

			if (!this.boxStack.isEmpty()) {
				final IBox parentBox = (IBox) this.boxStack.get(this.boxStack.size() - 1);
				if (parentBox.getSubtype() == IBox.SUBTYPE_RUBY && blockBox.getSubtype() == IBox.SUBTYPE_RUBY_BODY
						&& this.containerBuilder().builder.isTwoPass()) {
					TwoPassBlockBuilder builder = (TwoPassBlockBuilder) this.containerBuilder().builder;
					if (!builder.isEmpty()) {
						RubyBox rubyBox = (RubyBox) parentBox;
						this.endBox();
						rubyBox = new RubyBox(rubyBox.getBlockParams(), rubyBox.getInlinePos());
						this.startBox(rubyBox);
					}
				}
			}

			// ぶちぬき
			final FlowPos pos = blockBox.getFlowPos();
			if (pos.columnSpan == FlowPos.COLUMN_SPAN_ALL) {
				this.startColumnSpan(pos);
			}

			this.closeInlines(params);
			this.endContainer();
			final Builder builder = this.containerBuilder().builder;
			if (StyleUtils.isVertical(params.flow) == StyleUtils.isVertical(builder.getRootBox().getBlockParams().flow)
					&& !blockBox.isFixedMulcolumn()) {
				builder.startFlowBlock(blockBox);
			} else {
				// ページ進行方向が違う場合
				final Builder newBuilder = builder.newBuilder(blockBox);
				this.startContainerBuilder(newBuilder);
			}
			this.startContainer();
		}
			break;

		case Pos.TYPE_FLOAT:
			// 浮動体
			this.containerBuilder().getStyledTextUnitizer().flushText();
		case Pos.TYPE_ABSOLUTE: {
			// 絶対位置指定
			final AbstractBlockBox stfBox = (AbstractBlockBox) box;
			final Builder builder = this.contextBuilder().builder;
			if (box.getPos().getType() == Pos.TYPE_ABSOLUTE) {
				final AbsolutePos pos = (AbsolutePos) stfBox.getPos();
				if (pos.autoPosition == Types.AUTO_POSITION_INLINE) {
					this.containerBuilder().getStyledTextUnitizer().flushText();
					this.containerBuilder().getStyledTextUnitizer().requireGlypher();
				}
			}
			final Builder newBuilder = builder.newBuilder(stfBox);
			this.startContainerBuilder(newBuilder);
			this.startContainer();
		}
			break;
		default:
			throw new IllegalStateException();
		}

		this.boxStack.add(box);
	}

	public void endBox() {
		final IBox box = (IBox) this.boxStack.remove(this.boxStack.size() - 1);
		if (DEBUG) {
			System.err.println("endBox: " + box.getParams().element);
		}
		switch (box.getPos().getType()) {
		case Pos.TYPE_TABLE: {
			// テーブル
			final TableBuilder tableBuilder = this.endTableBuilder();
			final TableBox tableBox = tableBuilder.getTableBox();
			final TableParams tableParams = tableBox.getTableParams();
			switch (tableBox.getBlockBox().getPos().getType()) {
			case Pos.TYPE_FLOW:
				this.closeInlines(tableBox.getBlockBox().getParams());
				this.endContainer();
				break;
			case Pos.TYPE_FLOAT:
				this.containerBuilder().getStyledTextUnitizer().flushText();
				break;
			}
			final Builder builder = this.containerBuilder().builder;
			if (!builder.isMain() || StyleUtils.isTwoPassTable(tableBox)) {
				// 2パスレイアウト
				builder.addTable(tableBuilder);
			} else {
				// 1パスレイアウト
				final OnePassTableBuilder fixedTableBuilder = (OnePassTableBuilder) tableBuilder;
				fixedTableBuilder.endLayout();
			}
			switch (tableBox.getBlockBox().getPos().getType()) {
			case Pos.TYPE_FLOW:
				this.startContainer();
				this.restoreInlines(tableParams);
				break;
			case Pos.TYPE_INLINE:
				this.containerBuilder().getStyledTextUnitizer().addInlineBlock((InlineBlockBox) tableBox.getBlockBox());
				break;
			case Pos.TYPE_ABSOLUTE:
				final AbsoluteBlockBox absoluteBox = (AbsoluteBlockBox) tableBox.getBlockBox();
				if (absoluteBox.getAbsolutePos().autoPosition == Types.AUTO_POSITION_INLINE) {
					this.containerBuilder().getStyledTextUnitizer().addInlineAbsolute(absoluteBox);
				}
				break;
			}
		}
			break;
		case Pos.TYPE_TABLE_CELL:
		case Pos.TYPE_TABLE_CAPTION: {
			// テーブルセル
			// キャプション
			this.endContainer();
			this.endContainerBuilder();
			assert this.builderStack.size() != 1;
		}
			break;
		case Pos.TYPE_TABLE_COLUMN:
		case Pos.TYPE_TABLE_ROW_GROUP:
		case Pos.TYPE_TABLE_ROW: {
			// テーブル列グループ
			// テーブル列
			// テーブル行グループ
			// テーブル行
			this.tableBuilder().endInnerTable();
		}
			break;

		case Pos.TYPE_INLINE: {
			if (box.getType() == IBox.TYPE_INLINE) {
				// インライン
				this.containerBuilder().getStyledTextUnitizer().endInline();
			} else {
				// インラインブロック
				this.endContainer();
				final ContainerBuilderEntry entry = this.endContainerBuilder();
				final InlineBlockBox inlineBlockBox = (InlineBlockBox) entry.builder.getRootBox();
				final Builder parentBuilder = this.containerBuilder().builder;
				if (!parentBuilder.isTwoPass() && entry.builder.isTwoPass()) {
					// インラインブロックボックスの幅が明示されてなかった場合
					final TwoPassBlockBuilder stfBuilder = (TwoPassBlockBuilder) entry.builder;
					inlineBlockBox.shrinkToFit(parentBuilder, stfBuilder.getMinLineSize(), stfBuilder.getMaxLineSize(),
							false);
					final BlockBuilder lnlineBlockBuilder = new BlockBuilder(this.pageContextBuilder(), inlineBlockBox);
					stfBuilder.bind(lnlineBlockBuilder);
					lnlineBlockBuilder.finish();
				}
				this.containerBuilder().getStyledTextUnitizer().addInlineBlock(inlineBlockBox);
			}
		}
			break;

		case Pos.TYPE_FLOW: {
			// 通常のフロー
			this.endContainer();
			final FlowBlockBox blockBox = (FlowBlockBox) box;
			final Builder builder = this.containerBuilder().builder;
			if (builder.getRootBox() != box) {
				builder.endFlowBlock();
				this.startContainer();
				this.restoreInlines(box.getParams());
			} else {
				final ContainerBuilderEntry entry = this.endContainerBuilder();
				final Builder parentBuilder = this.containerBuilder().builder;
				if (!parentBuilder.isTwoPass()) {
					if (entry.builder.isTwoPass()) {
						// ビルド
						final TwoPassBlockBuilder contentBuilder = (TwoPassBlockBuilder) entry.builder;
						blockBox.shrinkToFit(parentBuilder, contentBuilder.getMinLineSize(),
								contentBuilder.getMaxLineSize(), false);
						final BlockBuilder bindBuilder = new BlockBuilder(this.pageContextBuilder(), blockBox);
						contentBuilder.bind(bindBuilder);
						bindBuilder.finish();
					}
					parentBuilder.addBound(blockBox);
				}
				this.startContainer();
				this.restoreInlines(box.getParams());
			}

			final FlowPos pos = blockBox.getFlowPos();
			// ぶち抜き復帰
			if (pos.columnSpan == FlowPos.COLUMN_SPAN_ALL) {
				this.endColumnSpan(pos);
			}
		}
			break;

		case Pos.TYPE_FLOAT: {
			// 浮動体
			this.endContainer();
			final ContainerBuilderEntry entry = this.endContainerBuilder();
			final Builder parentBuilder = this.containerBuilder().builder;
			if (!parentBuilder.isTwoPass()) {
				final BlockBuilder boundBuilder = (BlockBuilder) parentBuilder;
				final FloatBlockBox floatBox = (FloatBlockBox) entry.builder.getRootBox();
				if (entry.builder.isTwoPass()) {
					// ビルド
					final TwoPassBlockBuilder contentBuilder = (TwoPassBlockBuilder) entry.builder;
					floatBox.shrinkToFit(parentBuilder, contentBuilder.getMinLineSize(),
							contentBuilder.getMaxLineSize(), false);
					final BlockBuilder floatBuilder = new BlockBuilder(this.pageContextBuilder(), floatBox);
					contentBuilder.bind(floatBuilder);
					floatBuilder.finish();
				}
				final FloatPos pos = (FloatPos) box.getPos();
				final boolean pageBreak = (this.pageMode == 0 && ((pos.pageBreakBefore != Types.PAGE_BREAK_AUTO
						&& pos.pageBreakBefore != Types.PAGE_BREAK_AVOID)
						|| (pos.pageBreakAfter != Types.PAGE_BREAK_AUTO
								&& pos.pageBreakAfter != Types.PAGE_BREAK_AVOID)));
				if (pageBreak) {
					this.closeInlines(box.getParams());
					this.endContainer();
				}
				boundBuilder.addBound(floatBox);
				if (pageBreak) {
					this.startContainer();
					this.restoreInlines(box.getParams());
				}
			} else if (entry.builder.isTwoPass()) {
				// STFコンテキスト内
				TwoPassBlockBuilder stfBuilder = (TwoPassBlockBuilder) parentBuilder;
				TwoPassBlockBuilder contentBuilder = (TwoPassBlockBuilder) entry.builder;
				stfBuilder.fitFloating(contentBuilder);
			}
		}
			break;

		case Pos.TYPE_ABSOLUTE: {
			// 絶対位置指定
			this.endContainer();
			ContainerBuilderEntry entry = this.endContainerBuilder();
			Builder builder = this.contextBuilder().builder;
			if (!builder.isTwoPass()) {
				BlockBuilder boundBuilder = (BlockBuilder) builder;
				AbsoluteBlockBox absoluteBox = (AbsoluteBlockBox) entry.builder.getRootBox();
				if (entry.builder.isTwoPass()) {
					// ビルド
					TwoPassBlockBuilder contentBuilder = (TwoPassBlockBuilder) entry.builder;
					if (absoluteBox.getAbsolutePos().fiducial != Types.FODUCIAL_CONTEXT) {
						// position: fixed; の場合、ここで構築
						IFramedBox containerBox = this.pageContextBuilder().getRootBox();
						absoluteBox.shrinkToFit(containerBox, contentBuilder.getMinLineSize(),
								contentBuilder.getMaxLineSize());
						BlockBuilder absoluteBuilder = new BlockBuilder(this.pageContextBuilder(), absoluteBox);
						contentBuilder.bind(absoluteBuilder);
						absoluteBuilder.finish();
					} else {
						// position: absolute; は後で構築
						absoluteBox.prepareBind(contentBuilder);
					}
				}
				switch (absoluteBox.getAbsolutePos().autoPosition) {
				case Types.AUTO_POSITION_BLOCK:
					boundBuilder.addBound(absoluteBox);
					break;
				case Types.AUTO_POSITION_INLINE:
					this.containerBuilder().getStyledTextUnitizer().addInlineAbsolute(absoluteBox);
					break;
				default:
					throw new IllegalStateException();
				}
			}
		}
			break;

		default:
			throw new IllegalStateException();
		}
	}

	public void addReplacedBox(AbstractReplacedBox replacedBox) {
		this.requirePage();

		switch (replacedBox.getPos().getType()) {
		case Pos.TYPE_FLOW: {
			// 通常のフロー
			// ぶちぬき
			final FlowPos pos = ((FlowReplacedBox) replacedBox).getFlowPos();
			if (pos.columnSpan == FlowPos.COLUMN_SPAN_ALL) {
				this.startColumnSpan(pos);
			}

			final Builder builder = this.containerBuilder().builder;
			this.closeInlines(replacedBox.getParams());
			this.endContainer();
			builder.addBound(replacedBox);
			this.startContainer();
			this.restoreInlines(replacedBox.getParams());

			// ぶち抜き復帰
			if (pos.columnSpan == FlowPos.COLUMN_SPAN_ALL) {
				this.endColumnSpan(pos);
			}
		}
			break;

		case Pos.TYPE_FLOAT: {
			// 浮動体
			final Builder context = this.containerBuilder().builder;
			final FloatPos pos = (FloatPos) replacedBox.getPos();
			boolean pageBreak = (this.pageMode == 0 && ((pos.pageBreakBefore != Types.PAGE_BREAK_AUTO
					&& pos.pageBreakBefore != Types.PAGE_BREAK_AVOID)
					|| (pos.pageBreakAfter != Types.PAGE_BREAK_AUTO && pos.pageBreakAfter != Types.PAGE_BREAK_AVOID)));
			if (pageBreak) {
				this.closeInlines(replacedBox.getParams());
				this.endContainer();
			} else {
				this.containerBuilder().getStyledTextUnitizer().flushText();
			}
			context.addBound(replacedBox);
			if (pageBreak) {
				this.startContainer();
				this.restoreInlines(replacedBox.getParams());
			}
		}
			break;
		case Pos.TYPE_ABSOLUTE: {
			// 絶対位置指定
			final Builder context = this.containerBuilder().builder;
			final IAbsoluteBox absoluteBox = (IAbsoluteBox) replacedBox;
			switch (absoluteBox.getAbsolutePos().autoPosition) {
			case Types.AUTO_POSITION_BLOCK:
				context.addBound(replacedBox);
				break;
			case Types.AUTO_POSITION_INLINE:
				this.containerBuilder().getStyledTextUnitizer().addInlineAbsolute(absoluteBox);
				break;
			default:
				throw new IllegalStateException();
			}
		}
			break;

		case Pos.TYPE_INLINE: {
			// インライン
			this.containerBuilder().getStyledTextUnitizer().addInlineReplaced(replacedBox);
		}
			break;

		default:
			throw new IllegalStateException();
		}
	}

	public void characters(int charOffset, char[] ch, int off, int len, boolean lineFeed) {
		if (this.normalizeText) {
			String s = new String(ch, off, len);
			s = Normalizer.normalize(s, Form.NFC);
			ch = s.toCharArray();
			off = 0;
			len = s.length();
		}
		
		if (DEBUG) {
			System.err.println(charOffset + "/" + new String(ch, off, len));
		}
		this.requirePage();
		this.containerBuilder().getStyledTextUnitizer().characters(charOffset, ch, off, len, lineFeed);
	}

	public void end() {
		this.requirePage();
		this.endContainer();
		this.endContainerBuilder();
		assert this.builderStack.isEmpty();
	}
}
