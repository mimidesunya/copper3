package jp.cssj.sakae.pdf.font.cid;

import jp.cssj.sakae.font.FontSource;
import jp.cssj.sakae.pdf.ObjectRef;
import jp.cssj.sakae.pdf.font.PdfFont;

public abstract class CIDFont implements PdfFont {
	private static final long serialVersionUID = 0L;

	protected final FontSource source;

	protected final String name;

	protected final ObjectRef fontRef;

	protected CIDFont(FontSource metaFont, String name, ObjectRef fontRef) {
		this.source = metaFont;
		this.name = name;
		this.fontRef = fontRef;
	}

	public FontSource getFontSource() {
		return this.source;
	}

	public String getName() {
		return this.name;
	}

	public short getKerning(int gid, int c) {
		return 0;
	}

	public int getLigature(int gid, int c) {
		return -1;
	}

	public String toString() {
		return super.toString() + ":[PDFName=" + this.getName() + " source=" + this.getFontSource() + "]";
	}
}
