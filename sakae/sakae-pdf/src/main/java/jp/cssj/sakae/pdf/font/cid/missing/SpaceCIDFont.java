package jp.cssj.sakae.pdf.font.cid.missing;

import java.io.IOException;

import jp.cssj.sakae.gc.GC;
import jp.cssj.sakae.gc.GraphicsException;
import jp.cssj.sakae.gc.text.Text;
import jp.cssj.sakae.pdf.ObjectRef;

class SpaceCIDFont extends MissingCIDFont {
	private static final long serialVersionUID = 1L;

	public SpaceCIDFont(MissingCIDFontSource source, String name, ObjectRef fontRef) {
		super(source, name, fontRef);
	}

	public short getAdvance(int gid) {
		int c = this.unicodes.get(gid);
		switch (c) {
		// スペース文字
		case 0x007F:
		case 0x0020:
		case 0x00A0:
		case 0x2028:
		case 0x2029:
		case 0x202F:
			return (short) 500;
		}
		return (short)0;
	}

	public short getWidth(int gid) {
		return (short)0;
	}

	public void drawTo(GC gc, Text text) throws IOException, GraphicsException {
		// ignore
	}
}
