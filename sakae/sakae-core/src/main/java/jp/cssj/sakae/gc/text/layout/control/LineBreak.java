package jp.cssj.sakae.gc.text.layout.control;

import jp.cssj.sakae.gc.font.FontListMetrics;

public class LineBreak extends Control {
	private final FontListMetrics flm;
	private final int charOffset;

	public LineBreak(FontListMetrics flm, int charOffset) {
		this.flm = flm;
		this.charOffset = charOffset;
	}

	public int getCharOffset() {
		return this.charOffset;
	}

	public char getControlChar() {
		return '\n';
	}

	public double getAdvance() {
		return 0;
	}

	public double getAscent() {
		return this.flm.getMaxAscent();
	}

	public double getDescent() {
		return this.flm.getMaxDescent();
	}

	public String toString() {
		return "\\n";
	}

}
