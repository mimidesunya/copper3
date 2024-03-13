package jp.cssj.sakae.gc.text.layout;

import jp.cssj.sakae.gc.GC;
import jp.cssj.sakae.gc.font.FontMetrics;
import jp.cssj.sakae.gc.font.FontStyle;
import jp.cssj.sakae.gc.text.GlyphHandler;
import jp.cssj.sakae.gc.text.Quad;
import jp.cssj.sakae.gc.text.TextImpl;
import jp.cssj.sakae.gc.text.layout.control.Control;
import jp.cssj.sakae.gc.text.layout.control.Tab;

public class SimpleLayoutGlyphHandler implements GlyphHandler {
	/**
	 * タブの幅です。
	 */
	private static final double TAB_WIDTH = 24.0;

	/** 描画先です。 */
	private GC gc;

	private TextImpl text = null;

	private double letterSpacing = 0;

	private double advance = 0, line = 0, maxLineHeight = 0;

	public GC getGC() {
		return this.gc;
	}

	public void setGC(GC gc) {
		this.gc = gc;
	}

	public double getAdvance() {
		return this.advance;
	}

	public double getLetterSpacing() {
		return this.letterSpacing;
	}

	public void setLetterSpacing(double letterSpacing) {
		this.letterSpacing = letterSpacing;
	}

	public void startTextRun(int charOffset, FontStyle fontStyle, FontMetrics fontMetrics) {
		this.text = new TextImpl(charOffset, fontStyle, fontMetrics);
		this.text.setLetterSpacing(this.letterSpacing);
	}

	public void glyph(int charOffset, char[] ch, int coff, byte clen, int gid) {
		this.text.appendGlyph(ch, coff, clen, gid);
	}

	public void endTextRun() {
		assert this.text.getGLen() > 0;
		if (this.gc != null) {
			switch (this.text.getFontStyle().getDirection()) {
			case FontStyle.DIRECTION_LTR:
			case FontStyle.DIRECTION_RTL:
				// 横書き
				this.gc.drawText(this.text, this.advance, this.line);
				break;
			case FontStyle.DIRECTION_TB:
				// 縦書き
				this.gc.drawText(this.text, -this.line, this.advance);
				break;
			default:
				throw new IllegalArgumentException();
			}
		}
		this.advance += this.text.getAdvance();
		this.maxLineHeight = Math.max(this.maxLineHeight, this.text.fontStyle.getSize());
	}

	public void quad(Quad quad) {
		Control control = (Control) quad;
		this.maxLineHeight = Math.max(this.maxLineHeight, control.getAscent() + control.getDescent());
		switch (control.getControlChar()) {
		case '\n':
			this.line += this.maxLineHeight;
			this.maxLineHeight = 0;
			this.advance = 0;
			return;

		case '\t':
			// タブ文字
			Tab tab = (Tab) control;
			tab.advance = (TAB_WIDTH - (this.advance % TAB_WIDTH));
			break;
		}
		this.advance += quad.getAdvance();
	}

	public void flush() {
		// ignore
	}

	public void finish() {
		// ignore
	}
}
