package jp.cssj.sakae.pdf.font.cid.embedded;

import java.awt.Font;

import jp.cssj.sakae.pdf.ObjectRef;
import jp.cssj.sakae.pdf.font.PdfFont;
import jp.cssj.sakae.pdf.font.cid.SystemCIDFontSource;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: SystemEmbeddedCIDFontFace.java,v 1.1 2005/06/07 11:45:08
 *          harumanx Exp $
 */
public class SystemEmbeddedCIDFontSource extends SystemCIDFontSource {
	private static final long serialVersionUID = 1L;

	public SystemEmbeddedCIDFontSource(Font font) {
		super(font);
	}

	public PdfFont createFont(String name, ObjectRef fontRef) {
		return new SystemEmbeddedCIDFont(this, name, fontRef);
	}

	public jp.cssj.sakae.font.Font createFont() {
		return this.createFont(null, null);
	}

	public byte getType() {
		return TYPE_EMBEDDED;
	}
}
