package jp.cssj.sakae.pdf.font.cid.embedded;

import java.awt.Font;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.cssj.sakae.font.BBox;
import jp.cssj.sakae.font.ShapedFont;
import jp.cssj.sakae.gc.GC;
import jp.cssj.sakae.gc.GraphicsException;
import jp.cssj.sakae.gc.text.Text;
import jp.cssj.sakae.pdf.ObjectRef;
import jp.cssj.sakae.pdf.PdfFragmentOutput;
import jp.cssj.sakae.pdf.XRef;
import jp.cssj.sakae.pdf.font.PdfEmbeddedFont;
import jp.cssj.sakae.pdf.font.cid.CIDFont;
import jp.cssj.sakae.pdf.font.cid.CIDUtils;
import jp.cssj.sakae.pdf.font.util.PdfFontUtils;
import jp.cssj.sakae.pdf.gc.PdfGC;
import jp.cssj.sakae.util.IntList;
import jp.cssj.sakae.util.ShortList;

class SystemEmbeddedCIDFont extends CIDFont implements PdfEmbeddedFont, ShapedFont {
	private static final long serialVersionUID = 0L;

	protected final ShortList advances = new ShortList(Short.MIN_VALUE);

	protected IntList gids = new IntList(-1);

	protected IntList unicodes = new IntList();

	protected final List<Shape> shapes = new ArrayList<Shape>();

	public SystemEmbeddedCIDFont(SystemEmbeddedCIDFontSource source, String name, ObjectRef fontRef) {
		super(source, name, fontRef);

		int[] cida = new int[] { source.getAwtFont().getMissingGlyphCode() };
		GlyphVector gv = source.getAwtFont().createGlyphVector(source.getFontRenderContext(), cida);
		this.advances.set(0, (short) gv.getGlyphMetrics(0).getAdvance());
		this.shapes.add(gv.getGlyphOutline(0));
	}

	private final char[] chara = new char[1];

	public int toGID(int c) {
		int gid;
		SystemEmbeddedCIDFontSource metaFont = (SystemEmbeddedCIDFontSource) this.source;
		if (metaFont.getAwtFont().canDisplay((char) c)) {
			gid = this.gids.get(c);
			if (gid == -1) {
				this.chara[0] = (char) c;
				gid = this.shapes.size();
				this.gids.set(c, gid);
				this.unicodes.set(gid, c);
				GlyphVector gv = metaFont.getAwtFont().createGlyphVector(metaFont.getFontRenderContext(), this.chara);
				short advance = (short) gv.getGlyphMetrics(0).getAdvance();
				Shape shape = gv.getGlyphOutline(0);
				this.shapes.add(shape);

				this.advances.set(gid, advance);
			}
		} else {
			gid = 0;
		}
		return gid;
	}

	public short getAdvance(int gid) {
		return this.advances.get(gid);
	}

	public short getWidth(int gid) {
		return this.getAdvance(gid);
	}

	public void drawTo(GC gc, Text text) throws IOException, GraphicsException {
		if (gc instanceof PdfGC) {
			PdfFontUtils.drawCIDTo(((PdfGC) gc).getPDFGraphicsOutput(), text, false);
		} else {
			SystemEmbeddedCIDFontSource source = (SystemEmbeddedCIDFontSource) this.getFontSource();
			PdfFontUtils.drawAwtFont(gc, source, source.getAwtFont(), text);
		}
	}

	public void writeTo(PdfFragmentOutput out, XRef xref) throws IOException {
		SystemEmbeddedCIDFontSource metaFont = (SystemEmbeddedCIDFontSource) this.source;
		int[] unicodea = this.unicodes.toArray();
		this.unicodes = null;
		CIDUtils.writeEmbeddedFont(out, xref, metaFont, this, this.fontRef, this.advances.toArray(), null, unicodea);
	}

	public BBox getBBox() {
		SystemEmbeddedCIDFontSource metaFont = (SystemEmbeddedCIDFontSource) this.source;
		return metaFont.getBBox();
	}

	public int getGlyphCount() {
		return this.shapes.size();
	}

	public int getCharCount() {
		return this.shapes.size();
	}

	public String getOrdering() {
		return CIDUtils.ORDERING;
	}

	public String getRegistry() {
		return CIDUtils.REGISTRY;
	}

	public Shape getShape(int gid) {
		return (Shape) this.shapes.get(gid);
	}

	public byte[] getCharString(int i) {
		return null;
	}

	public Shape getShapeByGID(int gid) {
		return this.getShape(gid);
	}

	public int getSupplement() {
		return CIDUtils.SUPPLEMENT;
	}

	public String getPSName() {
		SystemEmbeddedCIDFontSource metaFont = (SystemEmbeddedCIDFontSource) this.source;
		Font awtFont = metaFont.getAwtFont();
		return awtFont.getPSName();
	}
}
