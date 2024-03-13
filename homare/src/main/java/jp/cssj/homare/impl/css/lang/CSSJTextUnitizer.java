package jp.cssj.homare.impl.css.lang;

import java.util.ArrayList;
import java.util.List;

import jp.cssj.homare.style.box.params.AbstractTextParams;
import jp.cssj.homare.style.builder.InlineQuad;
import jp.cssj.homare.style.builder.InlineQuad.InlineStartQuad;
import jp.cssj.sakae.gc.text.Quad;
import jp.cssj.sakae.gc.text.hyphenation.Hyphenation;
import jp.cssj.sakae.gc.text.hyphenation.impl.TextUnitizer;

public class CSSJTextUnitizer extends TextUnitizer {

	private List<Hyphenation> hyphStack = new ArrayList<Hyphenation>();;

	public CSSJTextUnitizer(Hyphenation hyph) {
		super(hyph);
		this.hyphStack.add(hyph);
	}

	public void quad(Quad quad) {
		if (quad instanceof InlineQuad) {
			final InlineQuad inlineQuad = (InlineQuad) quad;
			switch (inlineQuad.getType()) {
			case InlineQuad.INLINE_START: {
				final InlineStartQuad inlineStartQuad = (InlineStartQuad) inlineQuad;
				AbstractTextParams params = inlineStartQuad.box.getTextParams();
				this.hyphStack.add(params.hyphenation);
				this.setHyphenation(params.hyphenation);
			}
				break;

			case InlineQuad.INLINE_END: {
				this.hyphStack.remove(this.hyphStack.size() - 1);
				final Hyphenation hyph = this.hyphStack.get(this.hyphStack.size() - 1);
				this.setHyphenation(hyph);
			}
				break;

			case InlineQuad.INLINE_REPLACED:
			case InlineQuad.INLINE_BLOCK:
			case InlineQuad.INLINE_ABSOLUTE:
				break;

			default:
				throw new IllegalStateException();
			}
		}
		super.quad(quad);
	}

}
