package jp.cssj.homare.style.box.impl;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

import jp.cssj.homare.impl.css.lang.CSSJTextUnitizer;
import jp.cssj.homare.style.box.AbstractBox;
import jp.cssj.homare.style.box.AbstractLineBox;
import jp.cssj.homare.style.box.IFlowBox;
import jp.cssj.homare.style.box.IFramedBox;
import jp.cssj.homare.style.box.IPageBreakableBox;
import jp.cssj.homare.style.box.content.BreakMode;
import jp.cssj.homare.style.box.params.BlockParams;
import jp.cssj.homare.style.box.params.Params;
import jp.cssj.homare.style.box.params.Pos;
import jp.cssj.homare.style.box.params.TextBlockPos;
import jp.cssj.homare.style.builder.impl.BlockBuilder;
import jp.cssj.homare.style.builder.impl.BuilderGlyphHandler;
import jp.cssj.homare.style.draw.DebugDrawable;
import jp.cssj.homare.style.draw.Drawable;
import jp.cssj.homare.style.draw.Drawer;
import jp.cssj.homare.style.util.StyleUtils;
import jp.cssj.homare.style.visitor.Visitor;
import jp.cssj.sakae.gc.paint.RGBColor;
import jp.cssj.sakae.gc.text.FilterGlyphHandler;
import jp.cssj.sakae.gc.text.GlyphHandler;

/**
 * テキストだけを含むことができるボックスです。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: TextBlockBox.java 1631 2022-05-15 05:43:49Z miyabe $
 */
public class TextBlockBox extends AbstractBox implements IPageBreakableBox, IFlowBox {
	/**
	 * ボックスの外辺を薄紫色の枠で囲みます。
	 */
	private static final boolean DEBUG = false;

	/**
	 * 配置された行です。
	 * 
	 * @author MIYABE Tatsuhiko
	 * @version $Id: TextBlockBox.java 1631 2022-05-15 05:43:49Z miyabe $
	 */
	protected static class Line {
		public final AbstractLineBox box;
		public final double pageAxis;

		public Line(AbstractLineBox line, double pageAxis) {
			this.box = line;
			this.pageAxis = pageAxis;
		}

		public double getPageEnd() {
			return this.pageAxis + this.box.getAscent() + this.box.getDescent();
		}

		public String toString() {
			return this.box.toString();
		}
	}

	protected final BlockParams params;

	/**
	 * テキストブロックに含まれる行のリスト。
	 */
	protected final List<Line> lines = new ArrayList<Line>();

	protected double lineSize = 0;

	/**
	 * ブロックの先頭であればtrue。
	 */
	protected final byte textState;

	public TextBlockBox(final BlockParams params, final byte textState) {
		this.params = params;
		this.textState = textState;
	}

	public final byte getType() {
		return TYPE_TEXT_BLOCK;
	}

	public final Params getParams() {
		return this.params;
	}

	public final BlockParams getBlockParams() {
		return this.params;
	}

	public final Pos getPos() {
		return TextBlockPos.POS;
	}

	public final double getFirstAscent() {
		double ascent = 0;
		if (this.lines != null && !this.lines.isEmpty()) {
			Line line = (Line) this.lines.get(0);
			ascent += line.box.getAscent();
		}
		return ascent;
	}

	public final double getLastDescent() {
		double descent = 0;
		if (this.lines != null && !this.lines.isEmpty()) {
			final Line line = (Line) this.lines.get(this.lines.size() - 1);
			descent += line.box.getDescent();
		}
		return descent;
	}

	public final double getLineSize() {
		return this.lineSize;
	}

	public final double getPageSize() {
		Line line = (Line) this.lines.get(this.lines.size() - 1);
		return line.getPageEnd();
	}

	public final double getWidth() {
		if (StyleUtils.isVertical(this.params.flow)) {
			// 縦書き
			return this.getPageSize();
		} else {
			// 横書き
			return this.lineSize;
		}
	}

	public final double getHeight() {
		if (StyleUtils.isVertical(this.params.flow)) {
			// 縦書き
			return this.lineSize;
		} else {
			// 横書き
			return this.getPageSize();
		}
	}

	public final double getInnerWidth() {
		return this.getWidth();
	}

	public final double getInnerHeight() {
		return this.getHeight();
	}

	public final void addLine(AbstractLineBox lineBox, double pageAxis) {
		assert !StyleUtils.isNone(pageAxis);
		this.lines.add(new Line(lineBox, pageAxis));
		// この拡張はIE互換モードでなければ、あまり意味はない
		this.lineSize = Math.max(lineBox.getLineSize(), this.lineSize);
	}

	public final void finishLayout(IFramedBox containerBox) {
		for (int i = 0; i < this.lines.size(); ++i) {
			Line line = (Line) this.lines.get(i);
			line.box.finishLayout(containerBox);
		}
	}

	public final void getText(StringBuffer textBuff) {
		for (int i = 0; i < this.lines.size(); ++i) {
			Line line = (Line) this.lines.get(i);
			line.box.getText(textBuff);
		}
	}

	public final double getCutPoint(double pageAxis) {
		if (this.lines.isEmpty()) {
			return pageAxis;
		}
		if (StyleUtils.isVertical(this.getBlockParams().flow)) {
			// 縦書き
			for (int i = 0; i < this.lines.size(); ++i) {
				final Line line = (Line) this.lines.get(i);
				final double bottom = line.pageAxis + line.box.getWidth();
				if (StyleUtils.compare(bottom, pageAxis) >= 0) {
					pageAxis = bottom;
					break;
				}
			}
		} else {
			// 横書き
			for (int i = 0; i < this.lines.size(); ++i) {
				final Line line = (Line) this.lines.get(i);
				final double bottom = line.pageAxis + line.box.getHeight();
				if (StyleUtils.compare(bottom, pageAxis) >= 0) {
					pageAxis = bottom;
					break;
				}
			}
		}

		return pageAxis;
	}

	public final void draw(PageBox pageBox, Drawer drawer, Visitor visitor, Shape clip, AffineTransform transform,
			double contextX, double contextY, double x, double y) {
		assert !StyleUtils.isNone(x);
		assert !StyleUtils.isNone(y);
		visitor.visitBox(transform, this, x, y);

		if (DEBUG) {
			Drawable drawable = new DebugDrawable(this.getWidth(), this.getHeight(), RGBColor.create(1, .5f, 1));
			drawer.visitDrawable(drawable, x, y);
		}
		for (int i = 0; i < this.lines.size(); ++i) {
			Line line = (Line) this.lines.get(i);
			AbstractLineBox lineBox = line.box;
			// 描画
			if (StyleUtils.isVertical(this.params.flow)) {
				// 縦書き
				lineBox.draw(pageBox, drawer, visitor, clip, transform, contextX, contextY,
						x + this.getPageSize() - line.getPageEnd(), y);
			} else {
				// 横書き
				lineBox.draw(pageBox, drawer, visitor, clip, transform, contextX, contextY, x, y + line.pageAxis);
			}
		}
	}

	public void textShape(PageBox pageBox, GeneralPath path, AffineTransform transform, double x, double y) {
		for (int i = 0; i < this.lines.size(); ++i) {
			Line line = (Line) this.lines.get(i);
			AbstractLineBox lineBox = line.box;
			// 描画
			if (StyleUtils.isVertical(this.params.flow)) {
				// 縦書き
				lineBox.textShape(pageBox, path, transform, x + this.getPageSize() - line.getPageEnd(), y);
			} else {
				// 横書き
				lineBox.textShape(pageBox, path, transform, x, y + line.pageAxis);
			}
		}
	}

	public final IPageBreakableBox splitPageAxis(double pageLimit, BreakMode mode, byte flags) {
		assert (!this.lines.isEmpty());
		// System.err.println("TBB A: " +flags + "/" + mode + "/" + pageLimit
		// + "/" + this.getHeight() + "/" + this.lines.size() + "/"
		// + this.params.augmentation);
		assert mode.getType() != BreakMode.FORCE;
		// assert (flags & IPageBreakableBox.FLAGS_LAST) == 0;
		// FLAGS_LASTは実際の要素に対するもので、仮想的なテキストブロックには適用しない

		final boolean vertical = StyleUtils.isVertical(this.params.flow);
		final double pageSize;
		if (vertical) {
			pageSize = this.getWidth();
		} else {
			pageSize = this.getHeight();
		}
		if (StyleUtils.compare(pageLimit, pageSize) >= 0) {
			// 切断線が底辺以下にある場合は移動なし
			return null;
		}

		// 実質的に高さのある行をカウントする
		int nonZeroLines = 0;
		for (int i = 0; i < this.lines.size(); ++i) {
			final Line line = (Line) this.lines.get(i);
			if (line.pageAxis > 0 || line.box.getPageSize() > 0) {
				if (++nonZeroLines >= 2) {
					break;
				}
			}
		}
		if (nonZeroLines >= 2) {
			if ((flags & IPageBreakableBox.FLAGS_FIRST) == 0) {
				final Line line = (Line) this.lines.get(0);
				if (StyleUtils.compare(pageLimit, line.getPageEnd()) < 0) {
					// 切断線が最初の行の底辺より上にある場合は全部移動
					return this;
				}
			}
		} else {
			// １行だけの場合
			if ((flags & IPageBreakableBox.FLAGS_FIRST) == 0) {
				return this;
			}
			return null;
		}

		// 前ページに残すことができる最後の行を求める
		int lastOrphan;
		for (lastOrphan = this.lines.size() - 1; lastOrphan > 0; --lastOrphan) {
			final Line line = (Line) this.lines.get(lastOrphan);
			if (StyleUtils.compare(pageLimit, line.getPageEnd()) >= 0) {
				break;
			}
		}

		// widows, orphansは対象範囲の高さをline-heightで割った値(仮想行数)を基準に計算する
		// widows, orphansを満たすように改ページ位置を決める
		// 両方を満たすことができない場合、全体を次ページに送る、ただし
		// ページの先頭ではorphansを無視し、少なくとも１行を前ページに残す

		// 'widows'による制約
		while (lastOrphan >= 0) {
			final Line line = (Line) this.lines.get(lastOrphan);
			double virHeight = pageSize - line.getPageEnd();
			int virWidows = (int) Math.round(virHeight / this.params.lineHeight);
			if (virWidows >= this.params.widows) {
				break;
			}
			--lastOrphan;
		}
		if (lastOrphan == -1) {
			if ((flags & IPageBreakableBox.FLAGS_FIRST) == 0) {
				return this;
			}
			lastOrphan = 0;
		}
		if ((flags & IPageBreakableBox.FLAGS_FIRST) == 0) {
			// 'orphans'による制約
			final Line line = (Line) this.lines.get(lastOrphan);
			int virOrphans = (int) Math.round(line.getPageEnd() / this.params.lineHeight);
			if (virOrphans < this.params.orphans) {
				return this;
			}
		}

		// widowsを次ページに移動
		final int firstWidow = lastOrphan + 1;
		final double top = ((Line) this.lines.get(firstWidow)).pageAxis;
		byte textState = BlockBuilder.TS_MIDFLOW;
		{
			final Line line = (Line) this.lines.get(lastOrphan);
			if (!line.box.isLast()) {
				textState |= BlockBuilder.TS_WRAP;
			}
		}
		final TextBlockBox nextTextBlock = new TextBlockBox(this.params, textState);
		for (int i = firstWidow; i < this.lines.size(); ++i) {
			final Line line = (Line) this.lines.get(i);
			nextTextBlock.addLine(line.box, line.pageAxis - top);
		}
		while (this.lines.size() > firstWidow) {
			this.lines.remove(this.lines.size() - 1);
		}

		assert !this.lines.isEmpty() : mode;
		assert !nextTextBlock.lines.isEmpty();
		// System.err.println("TextBlockBox D: " + this.lines.size() + "/"
		// + nextTextBlock.lines.size());
		return nextTextBlock;
	}

	public final int getLineCount() {
		return this.lines.size();
	}

	public final void restyle(final BlockBuilder builder) {
		assert (!this.lines.isEmpty());
		builder.setTextState(this.textState);
		final GlyphHandler gh = new BuilderGlyphHandler(builder);
		final FilterGlyphHandler textUnitizer = new CSSJTextUnitizer(this.params.hyphenation);
		textUnitizer.setGlyphHandler(gh);
		// System.err.println("*** start");
		for (int i = 0; i < this.lines.size(); ++i) {
			final Line line = (Line) this.lines.get(i);
			line.box.restyle(textUnitizer, i == 0);
		}
		// System.err.println("*** end");
		textUnitizer.finish();
	}

	public final boolean avoidBreakAfter() {
		return false;
	}

	public final boolean avoidBreakBefore() {
		return false;
	}

	public String toString() {
		return super.toString() + "/lineCount=" + this.lines.size();
	}
}
