package jp.cssj.homare.style.box.content;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

import jp.cssj.homare.style.box.AbstractContainerBox;
import jp.cssj.homare.style.box.AbstractReplacedBox;
import jp.cssj.homare.style.box.IBox;
import jp.cssj.homare.style.box.IFloatBox;
import jp.cssj.homare.style.box.IPageBreakableBox;
import jp.cssj.homare.style.box.impl.PageBox;
import jp.cssj.homare.style.box.params.BlockParams;
import jp.cssj.homare.style.box.params.Types;
import jp.cssj.homare.style.builder.impl.BlockBuilder;
import jp.cssj.homare.style.draw.Drawer;
import jp.cssj.homare.style.util.StyleUtils;
import jp.cssj.homare.style.visitor.Visitor;

/**
 * 通常のフロー以外のボックスを一括管理します。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: Floatings.java 1554 2018-04-26 03:34:02Z miyabe $
 */
public class Floatings {
	/**
	 * 配置された浮動ボックスです。
	 * 
	 * @author MIYABE Tatsuhiko
	 * @version $Id: Floatings.java 1554 2018-04-26 03:34:02Z miyabe $
	 */
	public static class Floating extends BoxHolder {
		public final IFloatBox box;
		public final double lineAxis, pageAxis;

		public Floating(int serial, IFloatBox box, double lineAxis, double pageAxis) {
			super(serial);
			this.box = box;
			this.lineAxis = lineAxis;
			this.pageAxis = pageAxis;
		}

		public IBox getBox() {
			return this.box;
		}

		public void restyle(BlockBuilder builder) {
			switch (this.box.getType()) {
			case IBox.TYPE_BLOCK: {
				// ブロックボックス
				// 匿名ボックス
				AbstractContainerBox floatBox = (AbstractContainerBox) this.box;
				BlockBuilder floatBindBuilder = new BlockBuilder(builder, floatBox);
				floatBox.restyle(floatBindBuilder, 0);
				floatBindBuilder.finish();
				builder.addBound(floatBox);
			}
				break;
			case IBox.TYPE_REPLACED: {
				// 置換されたボックス
				AbstractReplacedBox floatBox = (AbstractReplacedBox) this.box;
				builder.addBound(floatBox);
			}
				break;
			default:
				throw new IllegalStateException(this.box.toString());
			}
		}
	}

	/**
	 * 浮動ボックス。
	 */
	private final List<Floating> floatings = new ArrayList<Floating>();

	/**
	 * 浮動ボックスを追加します。
	 * 
	 * @param floating
	 */
	public void addFloating(Floating floating) {
		assert !StyleUtils.isNone(floating.pageAxis) : "Undefined pageAxis";
		assert !StyleUtils.isNone(floating.lineAxis) : "Undefined lineAxis";
		this.floatings.add(floating);
	}

	public void draw(AbstractContainerBox box, PageBox pageBox, Drawer drawer, Visitor visitor, Shape clip,
			AffineTransform transform, double contextX, double contextY, double x, double y) {
		assert !StyleUtils.isNone(x) : "Undefined x";
		assert !StyleUtils.isNone(y) : "Undefined y";
		// 浮動体
		boolean vertical = StyleUtils.isVertical(box.getBlockParams().flow);
		if (vertical) {
			x += box.getInnerWidth();
		}
		for (int i = 0; i < this.floatings.size(); ++i) {
			Floating floating = (Floating) this.floatings.get(i);
			double xx;
			double yy;
			if (vertical) {
				// 縦書き
				xx = x - floating.pageAxis - floating.box.getWidth();
				yy = y + floating.lineAxis;
			} else {
				// 横書き
				xx = x + floating.lineAxis;
				yy = y + floating.pageAxis;
			}
			floating.box.draw(pageBox, drawer, visitor, clip, transform, contextX, contextY, xx, yy);
		}
	}

	public int getCount() {
		return this.floatings.size();
	}

	public Floating getFloating(int i) {
		return (Floating) this.floatings.get(i);
	}

	/**
	 * 浮動ボックスをページ分割します。全て前ページに残す場合はnull, 全て送る場合は自分自身を返します。
	 */
	public Floatings splitPageAxis(final AbstractContainerBox box, final double pageLimit, final byte flags) {
		assert !this.floatings.isEmpty();
		final boolean vertical = StyleUtils.isVertical(box.getBlockParams().flow);
		Floatings nextFloatings = this;
		// 浮動体
		for (int i = 0; i < this.floatings.size(); ++i) {
			Floating floating = (Floating) this.floatings.get(i);
			double pageEnd = floating.pageAxis;
			if (vertical) {
				pageEnd += floating.box.getWidth();
			} else {
				pageEnd += floating.box.getHeight();
			}
			final boolean first = (flags & IPageBreakableBox.FLAGS_FIRST) != 0
					&& StyleUtils.compare(floating.pageAxis, 0) <= 0;
			final IFloatBox nextBox;
			if (StyleUtils.compare(pageEnd, pageLimit) <= 0) {
				// 移動なし
				nextBox = null;
			} else if (!first && StyleUtils.compare(pageLimit, floating.pageAxis) < 0) {
				nextBox = floating.box;
			} else {
				switch (floating.box.getType()) {
				case IBox.TYPE_BLOCK: {
					// ブロックボックス
					// 匿名ボックス
					final AbstractContainerBox containerBox = (AbstractContainerBox) floating.box;
					final BlockParams params = containerBox.getBlockParams();
					if (params.pageBreakInside != Types.PAGE_BREAK_AVOID
							&& vertical == StyleUtils.isVertical(params.flow)) {
						byte xflags = first ? IPageBreakableBox.FLAGS_FIRST : IPageBreakableBox.FLAGS_SPLIT;
						double pageAxis = pageLimit - floating.pageAxis;
						nextBox = (IFloatBox) containerBox.splitPageAxis(pageAxis, BreakMode.DEFAULT_BREAK_MODE,
								xflags);
						break;
					}
					// 改ページ禁止されていた場合、ページ進行方向が違う場合は置換されたボックスと同じ処理
				}
				case IBox.TYPE_REPLACED: {
					// 置換されたボックス
					nextBox = first ? null : floating.box;
				}
					break;
				default:
					throw new IllegalStateException(floating.box.toString());
				}
			}
			if (nextFloatings == this) {
				if (nextBox == floating.box) {
					continue;
				}
				if (i > 0 || nextBox != null) {
					nextFloatings = new Floatings();
					for (int j = 0; j < i; ++j) {
						nextFloatings.floatings.add(this.floatings.remove(j));
						--j;
						--i;
					}
				} else {
					nextFloatings = null;
				}
			}
			if (nextBox == null) {
				continue;
			}
			if (nextFloatings == null) {
				nextFloatings = new Floatings();
			}
			if (nextBox == floating.box) {
				this.floatings.remove(i);
				--i;
			} else {
				floating = new Floating(floating.serial, nextBox, 0, 0);
			}
			nextFloatings.floatings.add(floating);
		}
		assert !(nextFloatings != null && nextFloatings.floatings.isEmpty());
		return nextFloatings;
	}

	public String toString() {
		return super.toString() + ": floatings.size=" + this.floatings.size();
	}
}
