package jp.cssj.homare.style.builder.impl;

import java.util.ArrayList;
import java.util.List;

import jp.cssj.homare.style.box.AbstractContainerBox;
import jp.cssj.homare.style.box.AbstractLineBox;
import jp.cssj.homare.style.box.AbstractTextBox;
import jp.cssj.homare.style.box.IBox;
import jp.cssj.homare.style.box.IInlineBox;
import jp.cssj.homare.style.box.impl.FirstLineBox;
import jp.cssj.homare.style.box.impl.InlineBox;
import jp.cssj.homare.style.box.impl.LineBox;
import jp.cssj.homare.style.box.impl.TextBlockBox;
import jp.cssj.homare.style.box.params.AbstractLineParams;
import jp.cssj.homare.style.box.params.AbstractTextParams;
import jp.cssj.homare.style.box.params.BlockParams;
import jp.cssj.homare.style.box.params.FloatPos;
import jp.cssj.homare.style.box.params.InlineParams;
import jp.cssj.homare.style.box.params.InlinePos;
import jp.cssj.homare.style.box.params.Types;
import jp.cssj.homare.style.builder.InlineQuad;
import jp.cssj.homare.style.builder.InlineQuad.InlineAbsoluteQuad;
import jp.cssj.homare.style.builder.InlineQuad.InlineEndQuad;
import jp.cssj.homare.style.builder.InlineQuad.InlineReplacedQuad;
import jp.cssj.homare.style.builder.InlineQuad.InlineStartQuad;
import jp.cssj.homare.style.builder.LayoutContext;
import jp.cssj.homare.style.builder.LayoutContext.Flow;
import jp.cssj.homare.style.util.StyleUtils;
import jp.cssj.sakae.gc.font.FontListMetrics;
import jp.cssj.sakae.gc.font.FontMetrics;
import jp.cssj.sakae.gc.font.FontStyle;
import jp.cssj.sakae.gc.text.Element;
import jp.cssj.sakae.gc.text.Quad;
import jp.cssj.sakae.gc.text.Text;
import jp.cssj.sakae.gc.text.TextImpl;
import jp.cssj.sakae.gc.text.hyphenation.impl.BitSetCharacterSet;
import jp.cssj.sakae.gc.text.hyphenation.impl.CharacterSet;
import jp.cssj.sakae.gc.text.layout.control.Control;
import jp.cssj.sakae.gc.text.layout.control.Tab;
import jp.cssj.sakae.gc.text.layout.control.WhiteSpace;

/**
 * テキストブロックを構築します。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: TextBuilder.java 1593 2019-12-03 07:02:17Z miyabe $
 */
public class TextBuilder {
	private static final CharacterSet CL01 = new BitSetCharacterSet("‘“（〔［｛〈《「『【⦅〖«〝");

	/**
	 * タブの幅です。
	 */
	private static final double TAB_WIDTH = 24.0;

	/**
	 * 配置されたインラインボックスです。
	 * 
	 * @author MIYABE Tatsuhiko
	 * @version $Id: TextBuilder.java 1593 2019-12-03 07:02:17Z miyabe $
	 */
	protected static class Inline {
		public final InlineBox box;
		public double baseline;

		public Inline(InlineBox inline) {
			this.box = inline;
		}
	}

	private final BlockBuilder builder;

	/**
	 * 構築中のテキストブロック。
	 */
	TextBlockBox textBlockBox;

	/**
	 * 構築中のインラインボックスのスタック。
	 */
	private List<Inline> inlineStack = null;

	private List<InlineBox> textParamStack = null;

	/**
	 * 行頭、最初、直前での改行のユニットを示すフラグ。
	 */
	private boolean lineHead, firstUnit, last;

	/**
	 * 次のインラインまたはテキストの追加で改行する
	 */
	private boolean toLineFeed = false;

	/**
	 * スペースのつぶし、折り返し。
	 */
	private boolean collapseSpaces, wrap;

	/**
	 * 単語の分割
	 */
	private byte breakWord;

	private double textIndent, letterSpacing, minLineAxis, maxLineSize, maxPageSize, lastSpaceAdvance;

	private double pageAxis = 0;

	private double lineAxis = 0;

	private AbstractLineBox lineBox;

	private TextImpl text = null;

	private List<Element> textBuffer = new ArrayList<Element>();

	private double unitAdvance = 0;

	// あとで折り返し可能な部分までの範囲
	private int textUnitElementCount = 0;

	private int textUnitGlyphCount = 0;

	public TextBuilder(BlockBuilder builder, byte textState) {
		this.builder = builder;
		final Flow flow = builder.getFlow();
		final BlockParams params = flow.box.getBlockParams();
		this.textBlockBox = new TextBlockBox(params, textState);

		if ((textState & BlockBuilder.TS_MIDFLOW) == 0) {
			this.textIndent = flow.box.getTextIndent();
		} else {
			this.textIndent = 0;
		}
		final AbstractLineBox lineBox;
		if ((textState & BlockBuilder.TS_MIDFLOW) == 0 && params.firstLineStyle != null) {
			lineBox = new FirstLineBox(params.firstLineStyle);
		} else {
			lineBox = new LineBox(params);
		}

		this.last = (textState & BlockBuilder.TS_WRAP) == 0;
		this.lineBox = lineBox;
		this.lineHead = this.firstUnit = true;
		this.lastSpaceAdvance = 0;
		this.changeTextState(params);
	}

	/**
	 * テキストパラメータを切り替えます。
	 * 
	 * @param params
	 */
	private void changeTextState(AbstractTextParams params) {
		switch (params.whiteSpace) {
		case AbstractTextParams.WHITE_SPACE_PRE:
			this.collapseSpaces = false;
			this.wrap = false;
			break;

		case AbstractTextParams.WHITE_SPACE_NOWRAP:
			this.collapseSpaces = true;
			this.wrap = false;
			break;

		case AbstractTextParams.WHITE_SPACE_NORMAL:
			this.collapseSpaces = true;
			this.wrap = true;
			break;

		case AbstractTextParams.WHITE_SPACE_PRE_LINE:
			this.collapseSpaces = true;
			this.wrap = true;
			break;

		case AbstractTextParams.WHITE_SPACE_PRE_WRAP:
			this.collapseSpaces = false;
			this.wrap = true;
			break;
		default:
			throw new IllegalStateException();
		}
		if (this.wrap) {
			this.breakWord = params.wordWrap;
		} else {
			this.breakWord = AbstractTextParams.WORD_WRAP_NORMAL;
		}
		this.letterSpacing = StyleUtils.computeLength(params.letterSpacing, this.builder.getFlowBox().getLineSize());

		// System.err.println("CHANGE_TEXT: " + this.wrap + "/" + this.breakWord);
	}

	/**
	 * テキストブロックに行を追加します。
	 */
	private void addLine(AbstractLineBox lineBox) {
		this.textBlockBox.addLine(lineBox, this.pageAxis);
		final double pageAdvance = lineBox.getAscent() + lineBox.getDescent();
		this.pageAxis += pageAdvance;
		assert !StyleUtils.isNone(this.pageAxis);
		if (pageAdvance > 0) {
			this.builder.poLastMargin = this.builder.neLastMargin = 0;
		}
	}

	/**
	 * 現在構築中の行の位置を調整します。
	 */
	private void locateLine() {
		double pageStart = this.builder.pageAxis + this.pageAxis;
		double lineStart = this.builder.lineAxis;
		this.maxPageSize = Double.MAX_VALUE;

		this.maxLineSize = this.builder.getFlowBox().getLineSize();
		if (this.builder.floatings != null) {
			final double lineHeight = this.lineBox.getLineParams().lineHeight;
			// System.out.println("TB-locateLine1:" + pageStart + "/"
			// + this.builder.floatings.size() + "/" + lineHeight);
			final double lineEnd0 = this.builder.lineAxis + this.maxLineSize;
			for (;;) {
				// ラインを入れるスペースがある部分の左右
				LayoutContext.Floating startContent = null, endContent = null;
				double lineEnd = lineEnd0;
				lineStart = this.builder.lineAxis;

				// 浮動ボックスを底辺が上にあるものから順に探索
				for (int i = 0; i < this.builder.floatings.size(); ++i) {
					LayoutContext.Floating floating = (LayoutContext.Floating) this.builder.floatings.get(i);
					double pageEnd = floating.pageEnd;
					// System.out.println("TB-locateLine2:" + i + "/" +
					// pageEnd);
					if (StyleUtils.compare(pageStart, pageEnd) >= 0) {
						// 底辺より下に行がある場合
						continue;
					}
					FloatPos floatingPos = floating.box.getFloatPos();
					if (StyleUtils.compare(floating.pageStart, pageStart + lineHeight) >= 0) {
						// 上辺が以下にある場合
						// 行高さを余裕高さに制限する
						this.maxPageSize = floating.pageStart - pageStart;
						break;
					}
					switch (floatingPos.floating) {
					case Types.FLOATING_START:
						double tempStart = floating.lineEnd;
						if (StyleUtils.compare(tempStart, lineStart) >= 0) {
							startContent = floating;
							lineStart = tempStart;
						}
						continue;

					case Types.FLOATING_END:
						double tempEnd = floating.lineStart;
						if (StyleUtils.compare(tempEnd, lineEnd) <= 0) {
							endContent = floating;
							lineEnd = tempEnd;
						}
						continue;

					default:
						throw new IllegalStateException();
					}
				}
				this.maxLineSize = lineEnd - lineStart;
				if (StyleUtils.compare(this.maxLineSize, this.lineAxis) >= 0) {
					// 幅に余裕がある
					break;
				}
				// 余裕がない場合は１つ下りて再探索
				if (startContent == null && endContent == null) {
					break;
				}
				if (endContent == null) {
					pageStart = startContent.pageEnd;
				} else if (startContent == null) {
					pageStart = endContent.pageEnd;
				} else {
					double startEnd = startContent.pageEnd;
					double endEnd = endContent.pageEnd;
					if (startEnd > endEnd) {
						pageStart = endEnd;
					} else {
						pageStart = startEnd;
					}
				}
			}
		}

		assert StyleUtils.compare(pageStart - this.builder.pageAxis, this.pageAxis) >= 0;
		this.pageAxis = pageStart - this.builder.pageAxis;
		assert !StyleUtils.isNone(this.pageAxis);
		this.minLineAxis = lineStart - this.builder.lineAxis;
		// System.out.println("NewLine:"+lineStart+"/"+this.maxLineSize);

		// 天付き
		if (!this.last && StyleUtils.isVertical(this.lineBox.getLineParams().flow)) {
			for (int i = 0; i < this.textBuffer.size(); ++i) {
				Element e = (Element) this.textBuffer.get(i);
				if (e.getAdvance() == 0) {
					continue;
				}
				if (e.getElementType() == Element.TEXT) {
					Text text = (Text) e;
					char c = text.getChars()[0];
					if (CL01.contains(c)) {
						this.textIndent = -text.getFontStyle().getSize() * .5;
					}
				}
				break;
			}
		}
	}

	/**
	 * 現在のテキストボックスを返します。
	 * 
	 * @return
	 */
	private AbstractTextBox getTextBox() {
		if (this.inlineStack == null || this.inlineStack.isEmpty()) {
			return this.lineBox;
		}
		Inline inline = (Inline) this.inlineStack.get(this.inlineStack.size() - 1);
		return inline.box;
	}

	private void startInline(IInlineBox box) {
		// System.err.println(box.getParams().augmentation);
		AbstractTextBox textBox = this.getTextBox();
		textBox.addInline(box);

		double baseline;
		if (this.inlineStack == null) {
			this.inlineStack = new ArrayList<Inline>();
			baseline = 0;
		} else if (this.inlineStack.isEmpty()) {
			baseline = 0;
		} else {
			// System.out.println(this.textParamStack.size()+"/"+this.inlineStack
			// .size());
			Inline parentInline = (Inline) this.inlineStack.get(this.inlineStack.size() - 1);
			baseline = parentInline.baseline;
		}

		switch (box.getType()) {
		case IBox.TYPE_INLINE: {
			InlineBox inlineBox = (InlineBox) box;
			InlineParams params = inlineBox.getInlineParams();
			InlinePos pos = box.getInlinePos();
			FontListMetrics flm = params.getFontListMetrics();
			double ascent = flm.getMaxAscent();
			double descent = flm.getMaxDescent();
			inlineBox.addAscentDescent(ascent, descent);

			double start;
			AbstractTextParams textParams = textBox.getTextParams();
			if (StyleUtils.isVertical(textParams.flow)) {
				// 縦書き
				start = inlineBox.getFrame().getFrameTop();
			} else {
				// 横書き
				start = inlineBox.getFrame().getFrameLeft();
			}
			this.lineBox.addAdvance(start);
			inlineBox.addAdvance(start);
			Inline inline = new Inline(inlineBox);
			this.inlineStack.add(inline);

			// baselineの設定
			double verticalAlign = pos.verticalAlign.getVerticalAlign(textBox, this.lineBox, ascent, descent,
					pos.lineHeight, baseline);
			inline.baseline = baseline + verticalAlign;

			if (inlineBox.getFrame().getFrameWidth() > 0) {
				// line-heightの適用
				double lineHeight = pos.lineHeight;
				lineHeight = Math.max(this.lineBox.getLineParams().lineHeight, lineHeight);
				double textHeight = ascent + descent;
				if (lineHeight != textHeight) {
					lineHeight = (lineHeight - textHeight) / 2.0;
					ascent = (ascent + lineHeight);
					descent = (descent + lineHeight);
				}
				ascent = ascent + verticalAlign + baseline;
				descent = descent - verticalAlign - baseline;
				this.lineBox.addAscentDescent(ascent, descent);
			}
		}
			break;

		case IBox.TYPE_REPLACED:
		case IBox.TYPE_BLOCK: {
			final IInlineBox inlineBox = box;
			final double advance;
			final AbstractLineParams lineParams = this.lineBox.getLineParams();
			if (StyleUtils.isVertical(lineParams.flow)) {
				// 縦書き
				advance = inlineBox.getHeight();
			} else {
				// 横書き
				advance = inlineBox.getWidth();
			}
			textBox.addAdvance(advance);
			if (this.lineBox != textBox) {
				this.lineBox.addAdvance(advance);
			}
			this.inlineStack.add(null);

			final InlinePos pos = box.getInlinePos();
			double descent, ascent;
			if (box.getType() == IBox.TYPE_BLOCK) {
				// インラインブロック・テーブルの基底線
				final AbstractContainerBox inlineBlockBox = (AbstractContainerBox) box;
				final BlockParams params = inlineBlockBox.getBlockParams();
				switch (lineParams.flow) {
				case AbstractTextParams.FLOW_TB:
					// 横書き
					if (params.flow == AbstractTextParams.FLOW_TB) {
						descent = inlineBlockBox.getLastDescent();
						if (StyleUtils.isNone(descent)) {
							descent = 0;
						}
					} else {
						// 横中縦
						descent = 0;
					}
					ascent = inlineBox.getHeight() - descent;
					break;
				case AbstractTextParams.FLOW_LR:
				case AbstractTextParams.FLOW_RL:
					// 縦書き
					if (params.flow == AbstractTextParams.FLOW_RL || params.flow == AbstractTextParams.FLOW_LR) {
						descent = inlineBlockBox.getLastDescent();
						if (StyleUtils.isNone(descent)) {
							descent = inlineBox.getWidth() / 2.0;
						}
					} else {
						// 縦中横
						descent = inlineBox.getWidth() / 2.0;
					}
					ascent = inlineBox.getWidth() - descent;
					break;
				default:
					throw new IllegalStateException();
				}
			} else {
				// 画像の基底線
				switch (lineParams.flow) {
				case AbstractTextParams.FLOW_TB:
					// 横書き
					ascent = box.getHeight();
					descent = 0;
					break;
				case AbstractTextParams.FLOW_LR:
				case AbstractTextParams.FLOW_RL:
					// 縦書き
					ascent = box.getWidth();
					descent = ascent = ascent / 2.0;
					break;
				default:
					throw new IllegalStateException();
				}
			}

			final double verticalAlign = pos.verticalAlign.getVerticalAlign(textBox, this.lineBox, ascent, descent,
					pos.lineHeight, baseline);

			if (box.getType() == IBox.TYPE_BLOCK) {
				// line-heightの適用
				double lineHeight = pos.lineHeight;
				lineHeight = Math.max(this.lineBox.getLineParams().lineHeight, lineHeight);
				double textHeight = ascent + descent;
				if (lineHeight > textHeight) {
					lineHeight = (lineHeight - textHeight) / 2.0;
					ascent = (ascent + lineHeight);
					descent = (descent + lineHeight);
				}
			}

			ascent = ascent + verticalAlign + baseline;
			descent = descent - verticalAlign - baseline;

			this.lineBox.addAscentDescent(ascent, descent);
		}
			break;

		default:
			throw new IllegalStateException();
		}
	}

	private void endInline() {
		Inline inline = (Inline) this.inlineStack.remove(this.inlineStack.size() - 1);
		if (inline != null) {
			InlineBox inlineBox = inline.box;
			// System.err.println("/"+inline.box.getParams().augmentation);
			inlineBox.closeInline();

			double advance, end;
			AbstractLineParams params = this.lineBox.getLineParams();
			switch (params.flow) {
			case AbstractTextParams.FLOW_TB: {
				// 横書き
				end = inlineBox.getFrame().getFrameRight();
				advance = inlineBox.getWidth() + end;
				break;
			}
			case AbstractTextParams.FLOW_LR:
			case AbstractTextParams.FLOW_RL: {
				// 縦書き
				end = inlineBox.getFrame().getFrameBottom();
				advance = inlineBox.getHeight() + end;
				break;
			}
			default:
				throw new IllegalStateException();
			}
			inlineBox.addAdvance(end);
			this.lineBox.addAdvance(end);
			AbstractTextBox textBox = this.getTextBox();
			if (this.lineBox != textBox) {
				textBox.addAdvance(advance);
			}
		}
	}

	private void addElement(Element e) {
		final AbstractTextBox textBox = this.getTextBox();

		final double advance = e.getAdvance();
		double ascent;
		double descent;
		switch (e.getElementType()) {
		case Element.TEXT:
			final Text text = (Text) e;
			textBox.addText(text);
			ascent = text.getAscent();
			descent = text.getDescent();
			assert !StyleUtils.isNone(ascent + descent);
			break;
		case Element.QUAD:
			final Control control = (Control) e;
			textBox.addControl(control);
			if (control.getControlChar() == ' ' && control.getAdvance() == 0) {
				return;
			}
			ascent = control.getAscent();
			descent = control.getDescent();
			assert !StyleUtils.isNone(ascent + descent);
			break;
		default:
			throw new IllegalStateException();
		}
		textBox.addAdvance(advance);
		if (this.lineBox != textBox) {
			this.lineBox.addAdvance(advance);

			final AbstractTextBox parentText;
			final double baseline;
			if (this.inlineStack.size() >= 2) {
				final Inline parentInline = (Inline) this.inlineStack.get(this.inlineStack.size() - 2);
				baseline = parentInline.baseline;
				parentText = parentInline.box;
			} else {
				baseline = 0;
				parentText = this.lineBox;
			}
			final InlineBox inlineBox = (InlineBox) textBox;
			final InlinePos pos = inlineBox.getInlinePos();

			final double verticalAlign = pos.verticalAlign.getVerticalAlign(parentText, this.lineBox, ascent, descent,
					pos.lineHeight, baseline);
			double lineHeight = pos.lineHeight;
			// 行のline-heightを適用する
			{
				final double textHeight = this.lineBox.getLineParams().lineHeight;
				if (!StyleUtils.isNone(textHeight)) {
					lineHeight = Math.max(textHeight, lineHeight);
				}
			}
			assert !StyleUtils.isNone(lineHeight);
			final double textHeight = ascent + descent;
			// line-heightの適用
			if (lineHeight != textHeight) {
				lineHeight = (lineHeight - textHeight) / 2.0;
				ascent = (ascent + lineHeight);
				descent = (descent + lineHeight);
				ascent = ascent + verticalAlign + baseline;
				descent = descent - verticalAlign - baseline;
			}
			assert !StyleUtils.isNone(ascent + descent);
		} else {
			double lineHeight = this.lineBox.getLineParams().lineHeight;
			// line-heightの適用
			final double textHeight = ascent + descent;
			if (lineHeight != textHeight) {
				lineHeight = (lineHeight - textHeight) / 2.0;
				ascent = (ascent + lineHeight);
				descent = (descent + lineHeight);
			}
			assert !StyleUtils.isNone(ascent + descent);
		}
		this.lineBox.addAscentDescent(ascent, descent);
	}

	double getActualPageAxis() {
		return this.pageAxis + this.lineBox.getPageSize();
	}

	double getPageAxis() {
		return this.pageAxis;
	}

	double getLineAxis() {
		return this.lineAxis;
	}

	/**
	 * 新しい行を開始します。
	 * 
	 * @param last
	 */
	private boolean newLine(boolean last) {
		// System.out.println("endLine: " + this.textBuffer);
		boolean lineAdded = false;
		if (this.drawLine(last)) {
			final AbstractLineBox lineBox = this.lineBox;
			final LineBox newLineBox = lineBox.splitLine(this.textBlockBox.getBlockParams());

			// StringBuffer text = new StringBuffer();
			// lineBox.getText(text);
			// System.out.println("endLine: " + this.maxLineAxis+"/"+text);

			lineBox.align(this.textIndent, this.minLineAxis, this.maxLineSize, last);
			if (this.inlineStack != null && !this.inlineStack.isEmpty()) {
				final AbstractTextParams lineParams = this.lineBox.getTextParams();
				this.lineBox = newLineBox;
				// TODO inlineStackの再生成を抑える
				List<Inline> inlineStack = this.inlineStack;
				this.inlineStack = null;

				for (int i = 0; i < inlineStack.size(); ++i) {
					Inline inline = (Inline) inlineStack.get(i);
					InlineBox oldInline = inline.box;
					InlineBox newInline = oldInline.splitLine(true);
					newInline.fixLineAxis(this.builder.getFlowBox());
					this.startInline(newInline);
				}
				for (int i = inlineStack.size() - 1; i >= 1; --i) {
					Inline inline = (Inline) inlineStack.get(i);
					Inline parent = (Inline) inlineStack.get(i - 1);
					if (StyleUtils.isVertical(lineParams.flow)) {
						// 縦書き
						parent.box.addAdvance(inline.box.getHeight());
					} else {
						// 横書き
						parent.box.addAdvance(inline.box.getWidth());
					}
				}
			} else {
				this.lineBox = newLineBox;
			}
			this.addLine(lineBox);
			lineAdded = true;
		}

		this.last = last;
		this.textIndent = 0;
		this.lineHead = this.firstUnit = true;
		this.lastSpaceAdvance = 0;
		if (last) {
			return lineAdded;
		}
		// 折り返し
		if (!this.collapseSpaces) {
			return lineAdded;
		}
		// 行頭のスペースのつぶし
		for (int i = 0; i < this.textBuffer.size(); ++i) {
			Element e = (Element) this.textBuffer.get(i);
			if (e instanceof WhiteSpace) {
				WhiteSpace whiteSpace = (WhiteSpace) e;
				this.lineAxis -= whiteSpace.getAdvance();
				whiteSpace.collapse();
				continue;
			}
			this.lineHead = false;
			break;
		}
		// System.out.println("nextLine: " + this.textBuffer);
		return lineAdded;
	}

	/**
	 * 行を生成します。
	 * 
	 * @param last
	 * @return
	 */
	private boolean drawLine(boolean last) {
		if (this.firstUnit) {
			this.locateLine();
			this.firstUnit = false;
		}
		final int count;
		if (last) {
			count = this.textBuffer.size();
		} else {
			count = this.textUnitElementCount;
		}
		// TODO 本来はここで assert count > 0 が成立するようにする。
		assert count > 0 || last;

		boolean content;
		if (count > 0) {
			for (int i = 0; i < count; ++i) {
				Element e = (Element) this.textBuffer.get(i);
				switch (e.getElementType()) {
				case Element.TEXT: {
					final TextImpl text = (TextImpl) e;
					if (i == count - 1) {
						if (last || this.textUnitGlyphCount == 0 || this.textUnitGlyphCount == text.getGLen()) {
							// 最後の行or
							// テキストで終わっていない場合
							// １ユニットしか幅がない場合
							text.pack();
							if (this.text == text) {
								this.text = null;
							}
						} else {
							// 分割可能な箇所で分割
							e = text.split(this.textUnitGlyphCount);
							TextImpl prevText = (TextImpl) e;
							// 分割部分のカーニングを取り消して位置を計算する
							this.lineAxis += this.fontMetrics.getKerning(prevText.gids[prevText.glen - 1],
									text.gids[0]);
							this.textBuffer.add(i, e);
						}
					}
					this.addElement(e);
					break;
				}

				case Element.QUAD:
					final Quad quad = (Quad) e;
					if (quad instanceof InlineQuad) {
						// インラインボックス
						final InlineQuad inlineQuad = (InlineQuad) quad;
						switch (inlineQuad.getType()) {
						case InlineQuad.INLINE_START: {
							// インライン開始
							final InlineStartQuad inlineStartQuad = (InlineStartQuad) inlineQuad;
							this.startInline(inlineStartQuad.box);
						}
							break;

						case InlineQuad.INLINE_END: {
							// インライン終了
							this.endInline();
						}
							break;

						case InlineQuad.INLINE_REPLACED: {
							// 置換されたボックス
							final InlineReplacedQuad inlineReplacedQuad = (InlineReplacedQuad) inlineQuad;
							this.startInline((IInlineBox) inlineReplacedQuad.box);
							this.endInline();
						}
							break;

						case InlineQuad.INLINE_BLOCK: {
							// ブロックボックス
							this.startInline((IInlineBox) inlineQuad.getBox());
							this.endInline();
						}
							break;

						case InlineQuad.INLINE_ABSOLUTE: {
							// 絶対配置ボックス
							final InlineAbsoluteQuad inlineAbsoluteQuad = (InlineAbsoluteQuad) inlineQuad;
							this.getTextBox().addAbsolute(inlineAbsoluteQuad.box);
						}
							break;

						default:
							throw new IllegalStateException();
						}
					} else {
						final Control control = (Control) quad;
						this.addElement(control);
					}
					break;

				default:
					throw new IllegalStateException();
				}
				this.lineAxis -= e.getAdvance();
			}

			double lastSpaceAdvance = 0;
			for (int i = count - 1; i >= 0; --i) {
				Element e = (Element) this.textBuffer.get(i);
				if (e.getElementType() == Element.QUAD) {
					if (e instanceof Control) {
						Control c = (Control) e;
						lastSpaceAdvance += c.getAdvance();
						continue;
					}
				}
				if (e.getAdvance() <= 0) {
					continue;
				}
				break;
			}
			this.lineBox.addAdvance(-lastSpaceAdvance);
			int remainder = this.textBuffer.size() - count;
			for (int i = 0; i < remainder; ++i) {
				this.textBuffer.set(i, this.textBuffer.get(count + i));
			}
			for (int i = 0; i < count; ++i) {
				this.textBuffer.remove(this.textBuffer.size() - 1);
			}
			content = true;
		} else {
			content = false;
		}

		this.textUnitElementCount = this.textBuffer.size();
		if (this.text != null) {
			this.textUnitGlyphCount = this.text.getGLen();
		} else {
			this.textUnitGlyphCount = 0;
		}
		this.builder.checkFloatings();
		return content;
	}

	FontStyle fontStyle;
	FontMetrics fontMetrics;

	public void startTextRun(FontStyle fontStyle, FontMetrics fontMetrics) {
		// System.err.println("TBBR: "+fontStyle);
		assert this.text == null;
		// assert fontStyle != null;
		this.fontStyle = fontStyle;
		this.fontMetrics = fontMetrics;
	}

	public void glyph(int charOffset, char[] ch, int coff, byte clen, int gid) {
		// if (this.breakWord && this.unitAdvance > 0) {
		// if (this.firstUnit) {
		// this.locateLine();
		// this.firstUnit = false;
		// }
		// this.flush();
		// }
		if (this.breakWord == AbstractTextParams.WORD_WRAP_BREAK_WORD && this.unitAdvance > 0) {
			if (this.firstUnit) {
				this.locateLine();
				this.firstUnit = false;
			}
			double lineAxis = this.unitAdvance + this.letterSpacing;
			if (this.text == null) {
				lineAxis += this.fontMetrics.getAdvance(gid);
			} else {
				lineAxis += this.text.glyphAdvance(gid);
			}
			final double maxLineAxis = this.maxLineSize - this.textIndent;
			if (StyleUtils.compare(lineAxis, maxLineAxis) > 0) {
				this.flush();
			}
		}

		if (this.text == null) {
			assert this.fontStyle != null;
			assert this.fontMetrics != null;
			this.text = new TextImpl(charOffset, this.fontStyle, this.fontMetrics);
			this.text.setLetterSpacing(this.letterSpacing);
			this.textBuffer.add(this.text);
		}

		final double advance = this.text.appendGlyph(ch, coff, clen, gid) + this.letterSpacing;
		this.unitAdvance += advance;
		this.lineAxis += advance;
		this.lastSpaceAdvance = 0;
		this.lineHead = false;

		if (StyleUtils.compare(this.text.getAscent() + this.text.getDescent(), this.maxPageSize) > 0) {
			// 行高さの制限を超えたら強制折り返し
			this.maxLineSize = 0;
		}

		if (this.text.getGLen() > 10000) {
			// あまりにも長いランができるのを防止する
			this.endTextRun();
			this.startTextRun(this.fontStyle, this.fontMetrics);
		}
		// System.err.println("TB glyph: " + this.breakWord + ":" + advance + "/" + new String(ch, coff, clen));
	}

	public void endTextRun() {
		assert this.text.getGLen() > 0;
		this.text.pack();
		this.text = null;
	}

	public void quad(Quad quad) {
		assert this.text == null;
		if (quad instanceof Control) {
			// 制御コード
			Control control = (Control) quad;
			switch (control.getControlChar()) {
			case '\n':
				// 改行文字
				this.toLineFeed = true;
				break;

			case '\t':
				// タブ文字
				Tab tab = (Tab) control;
				tab.advance = (TAB_WIDTH - (this.lineAxis % TAB_WIDTH));
				break;

			case '\u0020':
				// 空白
				if (!this.collapseSpaces) {
					break;
				}
				WhiteSpace whiteSpace = (WhiteSpace) control;
				if (this.lineHead) {
					// 先頭のつぶし
					whiteSpace.collapse();
				} else {
					// 末尾のつぶし
					this.lastSpaceAdvance = whiteSpace.getAdvance();
				}
				break;

			default:
				throw new IllegalStateException();
			}
		} else {
			AbstractTextParams params;
			if (this.textParamStack == null || this.textParamStack.isEmpty()) {
				params = this.lineBox.getTextParams();
			} else {
				final InlineBox box = (InlineBox) this.textParamStack.get(this.textParamStack.size() - 1);
				params = box.getTextParams();
			}

			final InlineQuad inlineQuad = (InlineQuad) quad;
			switch (inlineQuad.getType()) {
			case InlineQuad.INLINE_START: {
				final InlineStartQuad inlineStartQuad = (InlineStartQuad) inlineQuad;
				params = inlineStartQuad.box.getTextParams();
				this.changeTextState(params);
				if (this.textParamStack == null) {
					this.textParamStack = new ArrayList<InlineBox>();
				}
				assert this.textParamStack.isEmpty() || ((IBox) this.textParamStack.get(this.textParamStack.size() - 1))
						.getParams().element != params.element : params.element;
				this.textParamStack.add(inlineStartQuad.box);
				if (inlineStartQuad.getAdvance() != 0) {
					this.lastSpaceAdvance = 0;
				}
				// System.err.println(this.textParamStack.size()+"
				// start/"+this+" TB:
				// "+quad.getAdvance()+"/"+params.augmentation);
			}
				break;

			case InlineQuad.INLINE_END: {
				final InlineEndQuad inlineEndQuad = (InlineEndQuad) inlineQuad;
				// System.err.println(this.textParamStack.size()+" end/"+this+"
				// TB:
				// "+quad.getAdvance()+"/"+inlineEndQuad.box.getParams().augmentation);
				this.textParamStack.remove(this.textParamStack.size() - 1);
				if (this.textParamStack.isEmpty()) {
					params = this.lineBox.getTextParams();
				} else {
					final InlineBox box = (InlineBox) this.textParamStack.get(this.textParamStack.size() - 1);
					params = box.getTextParams();
				}

				this.changeTextState(params);
				if (inlineEndQuad.getAdvance() != 0) {
					this.lastSpaceAdvance = 0;
				}
			}
				break;

			case InlineQuad.INLINE_REPLACED:
			case InlineQuad.INLINE_BLOCK:
				final double lineHeight;
				if (StyleUtils.isVertical(params.flow)) {
					lineHeight = inlineQuad.getBox().getWidth();
				} else {
					lineHeight = inlineQuad.getBox().getHeight();
				}
				if (StyleUtils.compare(lineHeight, this.maxPageSize) > 0) {
					// 行高さの制限を超えたら強制折り返し
					this.maxLineSize = 0;
				}
				this.lineHead = false;
				break;

			case InlineQuad.INLINE_ABSOLUTE:
				break;

			default:
				throw new IllegalStateException();
			}
		}
		this.unitAdvance += quad.getAdvance();
		this.lineAxis += quad.getAdvance();
		this.textBuffer.add(quad);
	}

	/**
	 * 改行されるとtrueを返します。
	 * 
	 * @return
	 */
	public boolean flush() {
		//System.err.println("TB FLUSH: " + this.wrap);
		this.unitAdvance = 0;
		if (this.textBuffer.isEmpty()) {
			return false;
		}
		if (this.lineAxis > 0) {
			//System.err.println("TB flush: " + lineAxis + "/" + textUnitElementCount);
			if (this.firstUnit) {
				this.locateLine();
				this.firstUnit = false;
			}
			if (this.textUnitElementCount > 0) {
				double lineAxis = this.lineAxis - this.lastSpaceAdvance;
				double maxLineAxis = this.maxLineSize - this.textIndent;
				// System.err.println("TB flush: " + lineAxis + "/" + maxLineAxis);
				if (StyleUtils.compare(lineAxis, maxLineAxis) > 0) {
					// テキストブロックの途中での折り返し
					final boolean ret = this.newLine(false);
					return ret;
				}
			}
		}
		if (this.toLineFeed) {
			// 改行コード
			final boolean ret = this.newLine(true);
			this.toLineFeed = false;
			return ret;
		}
		//if (this.wrap) {
			this.textUnitElementCount = this.textBuffer.size();
			if (this.text != null) {
				this.textUnitGlyphCount = this.text.getGLen();
			} else {
				this.textUnitGlyphCount = 0;
			}
		//}
		return false;
	}

	void finish() {
		// テキストブロックの末尾
		// assert this.textParamStack == null || this.textParamStack.isEmpty();
		if (!this.drawLine(true)) {
			assert this.textBlockBox.getLineCount() > 0 : this.text;
			return;
		}
		this.lineBox.align(this.textIndent, this.minLineAxis, this.maxLineSize, true);
		this.addLine(this.lineBox);
	}
}
