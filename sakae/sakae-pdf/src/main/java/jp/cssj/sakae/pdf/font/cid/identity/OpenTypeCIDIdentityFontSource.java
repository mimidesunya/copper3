package jp.cssj.sakae.pdf.font.cid.identity;

import java.io.File;
import java.io.IOException;

import jp.cssj.sakae.font.Font;
import jp.cssj.sakae.font.otf.OpenTypeFontSource;
import jp.cssj.sakae.pdf.ObjectRef;
import jp.cssj.sakae.pdf.font.PdfFont;
import jp.cssj.sakae.pdf.font.cid.CIDFontSource;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: SystemExternalCIDFontFace.java,v 1.2 2005/06/10 12:46:30
 *          harumanx Exp $
 */
public class OpenTypeCIDIdentityFontSource extends OpenTypeFontSource implements CIDFontSource {
	private static final long serialVersionUID = 1L;

	public OpenTypeCIDIdentityFontSource(File ttfFont, int index, byte direction) throws IOException {
		super(ttfFont, index, direction);
	}

	public byte getType() {
		return TYPE_CID_IDENTITY;
	}

	public PdfFont createFont(String name, ObjectRef fontRef) {
		return new OpenTypeCIDIdentityFont(this, name, fontRef);
	}

	public Font createFont() {
		return this.createFont(null, null);
	}
}
