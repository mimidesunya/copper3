package jp.cssj.homare.style.builder.impl;

import java.lang.Character.UnicodeBlock;
import java.util.ArrayList;
import java.util.List;

import jp.cssj.homare.impl.css.lang.CSSJTextUnitizer;
import jp.cssj.homare.style.box.AbstractContainerBox;
import jp.cssj.homare.style.box.AbstractReplacedBox;
import jp.cssj.homare.style.box.IAbsoluteBox;
import jp.cssj.homare.style.box.impl.InlineBlockBox;
import jp.cssj.homare.style.box.impl.InlineBox;
import jp.cssj.homare.style.box.params.AbstractTextParams;
import jp.cssj.homare.style.box.params.BlockParams;
import jp.cssj.homare.style.builder.Builder;
import jp.cssj.homare.style.builder.InlineQuad;
import jp.cssj.homare.style.builder.InlineQuad.InlineEndQuad;
import jp.cssj.homare.style.util.TextUtils;
import jp.cssj.sakae.gc.font.FontListMetrics;
import jp.cssj.sakae.gc.text.FilterGlyphHandler;
import jp.cssj.sakae.gc.text.Glypher;
import jp.cssj.sakae.gc.text.Quad;
import jp.cssj.sakae.gc.text.layout.control.LineBreak;
import jp.cssj.sakae.gc.text.layout.control.Tab;
import jp.cssj.sakae.gc.text.layout.control.WhiteSpace;

// TODO ブロックの末尾のスペースをつぶす
public class StyledTextUnitizer {
	private static final boolean DEBUG = false;

	private final Builder builder;

	private final List<AbstractTextParams> textParamsStack = new ArrayList<AbstractTextParams>();

	/**
	 * 使用する予定のInlineEndQuadのスタック。
	 */
	private final List<Quad> inlineQuadStack = new ArrayList<Quad>();

	private BuilderGlyphHandler gh;

	/**
	 * スペースのつぶし、LFコードの処理、折り返し。
	 */
	private boolean collapseSpaces, lineFeed;
	/**
	 * 直前の文字
	 */
	private char followingChar;

	/**
	 * word-spacingプロパティによるスペース幅です。
	 */
	private double wordSpacing;

	private Glypher glypher = null;

	public StyledTextUnitizer(Builder builder) {
		this.builder = builder;
	}

	private AbstractTextParams getTextParams() {
		return (AbstractTextParams) this.textParamsStack.get(this.textParamsStack.size() - 1);
	}

	public void requireGlypher() {
		if (this.glypher != null) {
			return;
		}
		final AbstractTextParams params = this.getTextParams();
		final FilterGlyphHandler textUnitizer = new CSSJTextUnitizer(params.hyphenation);
		textUnitizer.setGlyphHandler(this.gh);
		this.glypher = params.fontManager.getGlypher();
		this.glypher.setGlyphHander(textUnitizer);
		this.glypher.fontStyle(params.fontStyle);
	}

	private void changeTextState(AbstractTextParams params) {
		this.wordSpacing = params.wordSpacing;
		switch (params.whiteSpace) {
		case AbstractTextParams.WHITE_SPACE_PRE:
			this.collapseSpaces = false;
			this.lineFeed = true;
			break;

		case AbstractTextParams.WHITE_SPACE_NOWRAP:
			this.collapseSpaces = true;
			this.lineFeed = false;
			break;

		case AbstractTextParams.WHITE_SPACE_NORMAL:
			this.collapseSpaces = true;
			this.lineFeed = false;
			break;

		case AbstractTextParams.WHITE_SPACE_PRE_LINE:
			this.collapseSpaces = true;
			this.lineFeed = true;
			break;

		case AbstractTextParams.WHITE_SPACE_PRE_WRAP:
			this.collapseSpaces = false;
			this.lineFeed = true;
			break;
		default:
			throw new IllegalStateException();
		}
	}

	public void startContainer() {
		this.followingChar = '\u0020';
		final BlockParams params = this.builder.getFlowBox().getBlockParams();
		this.textParamsStack.add(params);
		if (DEBUG) {
			System.out.println(this.textParamsStack.size() + "/startContainer|" + params.element);
		}
		if (this.gh == null) {
			this.gh = new BuilderGlyphHandler(builder);
		} else {
			if (this.textParamsStack.size() > 1) {
				this.gh.startTextBox(params);
			} else {
				this.gh.updateText();
			}
		}
		this.changeTextState(params);
	}

	public void flushText() {
		if (this.glypher != null) {
			this.glypher.flush();
		}
	}

	public void endContainer() {
		final AbstractTextParams params = (AbstractTextParams) this.textParamsStack
				.remove(this.textParamsStack.size() - 1);
		if (this.glypher != null) {
			this.glypher.finish();
			this.glypher = null;
			this.gh.builder.endTextBlock();
		}
		if (DEBUG) {
			System.out.println(this.textParamsStack.size() + "/endContainer|" + params.element);
		}
		if (this.textParamsStack.size() >= 1) {
			this.gh.endTextBox();
		}
	}

	public void startInline(InlineBox inlineBox) {
		AbstractContainerBox containerBox = this.gh.builder.getFlowBox();
		inlineBox.firstPassLayout(containerBox);
		this.requireGlypher();

		Quad end = InlineQuad.createInlineBoxEndQuad(inlineBox);
		this.inlineQuadStack.add(end);
		AbstractTextParams params = inlineBox.getInlineParams();
		this.textParamsStack.add(params);
		this.glypher.fontStyle(params.fontStyle);
		Quad start = InlineQuad.createInlineBoxStartQuad(inlineBox);
		this.glypher.quad(start);
		this.changeTextState(params);
	}

	public void endInline() {
		// ブロックでインラインが寸断されて復帰した直後にインラインが終わるときに、ここを実行する
		this.requireGlypher();

		Quad end = (InlineEndQuad) this.inlineQuadStack.remove(this.inlineQuadStack.size() - 1);
		this.glypher.quad(end);
		this.textParamsStack.remove(this.textParamsStack.size() - 1);
		AbstractTextParams params = this.getTextParams();
		this.glypher.fontStyle(params.fontStyle);
		this.changeTextState(params);
	}

	public void addInlineReplaced(AbstractReplacedBox inlineReplacedBox) {
		this.requireGlypher();
		Quad quad = InlineQuad.createReplacedBoxQuad(inlineReplacedBox);
		this.glypher.quad(quad);
		this.followingChar = 'x';
	}

	public void addInlineBlock(InlineBlockBox inlineBlockBox) {
		this.requireGlypher();
		final Quad quad = InlineQuad.createInlineBlockBoxQuad(inlineBlockBox);
		this.glypher.quad(quad);
		this.followingChar = 'x';
	}

	public void addInlineAbsolute(final IAbsoluteBox absoluteBox) {
		this.requireGlypher();
		final Quad quad = InlineQuad.createInlineAbsoluteBoxQuad(absoluteBox);
		this.glypher.quad(quad);
	}

	public void characters(int charOffset, char[] ch, final int off, final int len, boolean lineFeed) {
		assert len > 0;
		final AbstractTextParams params = this.getTextParams();

		// テキスト処理
		int ooff = 0;
		FontListMetrics flm = params.getFontListMetrics();
		for (int i = 0; i < len; ++i) {
			char c = ch[i + off];
			if (TextUtils.isControl(c)) {
				Quad quad = null;
				switch (c) {
				case '\n':
					// 改行コード
					if (lineFeed || this.lineFeed) {
						quad = new LineBreak(flm, charOffset + i);
					} else if (this.collapseSpaces) {
						UnicodeBlock block = UnicodeBlock.of(this.followingChar);
						if (block == UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
								|| block == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || block == UnicodeBlock.HIRAGANA
								|| block == UnicodeBlock.KATAKANA) {
							// 1文字削除
							if (i > ooff) {
								this._characters(charOffset + ooff, ch, off + ooff, i - ooff);
							}
							ooff = i + 1;
							continue;
						}
					}
					break;
				case '\t':
					// タブ文字
					if (!this.collapseSpaces) {
						quad = new Tab(flm, charOffset + i);
					}
					break;
				}
				if (quad != null) {
					// 1文字削除
					if (i > ooff) {
						this._characters(charOffset + ooff, ch, off + ooff, i - ooff);
					}
					ooff = i + 1;
					this.requireGlypher();
					this.glypher.quad(quad);
					this.followingChar = c;
					continue;
				}
				// 空白に変換
				c = '\u0020';
			}
			if (c == '\u0020') {
				// 1文字削除
				if (i > ooff) {
					this._characters(charOffset + ooff, ch, off + ooff, i - ooff);
				}
				ooff = i + 1;
				if (this.followingChar != '\u0020' || !this.collapseSpaces) {
					// スペースの出力
					WhiteSpace ws = new WhiteSpace(flm, charOffset + i);
					ws.setWordSpacing(this.wordSpacing);
					this.requireGlypher();
					this.glypher.quad(ws);
				}
				this.followingChar = c;
				continue;
			}
			this.followingChar = c;
			ch[i + off] = c;
		}
		if (len > ooff) {
			this._characters(charOffset + ooff, ch, off + ooff, len - ooff);
		}
	}

	private void _characters(int charOffset, char[] ch, int off, int len) {
		switch (this.getTextParams().textTransform) {
		case AbstractTextParams.TEXT_TRANSFORM_LOWERCASE:
			for (int i = 0; i < len; ++i) {
				char c = ch[i + off];
				ch[i + off] = Character.toLowerCase(c);
			}
			break;
		case AbstractTextParams.TEXT_TRANSFORM_UPPERCASE:
			for (int i = 0; i < len; ++i) {
				char c = ch[i + off];
				ch[i + off] = Character.toUpperCase(c);
			}
			break;
		case AbstractTextParams.TEXT_TRANSFORM_CAPITALIZE:
			boolean spaceBefore = true;
			for (int i = 0; i < len; ++i) {
				char c = ch[i + off];
				if (Character.isLetter(c)) {
					if (spaceBefore) {
						ch[i + off] = Character.toUpperCase(c);
					}
					spaceBefore = false;
				} else {
					spaceBefore = true;
				}
			}
			break;
		case AbstractTextParams.TEXT_TRANSFORM_NONE:
			break;
		default:
			throw new IllegalStateException();
		}
		this.requireGlypher();
		this.glypher.characters(charOffset, ch, off, len);
	}

}
