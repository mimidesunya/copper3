package jp.cssj.sakae.pdf.font.cid.missing;

import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.cssj.sakae.font.BBox;
import jp.cssj.sakae.font.ShapedFont;
import jp.cssj.sakae.gc.GC;
import jp.cssj.sakae.gc.GraphicsException;
import jp.cssj.sakae.gc.font.FontStyle;
import jp.cssj.sakae.gc.font.util.FontUtils;
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

class MissingCIDFont extends CIDFont implements PdfEmbeddedFont, ShapedFont {
	private static final long serialVersionUID = 2L;

	protected final ShortList advances = new ShortList(Short.MIN_VALUE);

	protected IntList gids = new IntList(-1);

	protected final IntList unicodes = new IntList();

	protected final List<Shape> shapes = new ArrayList<Shape>();

	private final char[] chara = new char[1];

	private final FontRenderContext frc = new FontRenderContext(null, false, false);

	private final Font numericFont;
	private final AffineTransform[] at = new AffineTransform[6];

	public MissingCIDFont(MissingCIDFontSource source, String name, ObjectRef fontRef) {
		super(source, name, fontRef);
		this.numericFont = new Font("sans-serif", Font.PLAIN, 400);
		float ascent = this.source.getAscent();
		float descent = this.source.getDescent();
		this.at[0] = AffineTransform.getTranslateInstance(140, -ascent + 440);
		this.at[1] = AffineTransform.getTranslateInstance(550, -ascent + 440);
		this.at[2] = AffineTransform.getTranslateInstance(140, +descent - 160);
		this.at[3] = AffineTransform.getTranslateInstance(550, +descent - 160);
		this.at[4] = AffineTransform.getTranslateInstance(140, +descent - 160 + 400);
		this.at[5] = AffineTransform.getTranslateInstance(550, +descent - 160 + 400);
	}

	public int toGID(final int c) {
		int gid = this.gids.get(c);
		if (gid == -1) {
			gid = this.shapes.size();
			this.unicodes.set(gid, c);
			this.gids.set(c, gid);

			short advance = this.getAdvance(gid);
			this.advances.set(gid, advance);
			Shape shape = this.getShapeByGID(gid);
			this.shapes.add(shape);
		}
		return gid;
	}

	public short getAdvance(int gid) {
		int c = this.unicodes.get(gid);
		return (short) (c <= 0xFFFF ? 1000 : 1400);
	}

	public short getWidth(int gid) {
		short a = this.getAdvance(gid);
		return a == 1400 ? 1000 : a;
	}

	public void drawTo(GC gc, Text text) throws IOException, GraphicsException {
		if (gc instanceof PdfGC) {
			PdfFontUtils.drawCIDTo(((PdfGC) gc).getPDFGraphicsOutput(), text,
					this.source.getDirection() == FontStyle.DIRECTION_TB);
		} else {
			FontUtils.drawText(gc, this, text);
		}
	}

	public void writeTo(PdfFragmentOutput out, XRef xref) throws IOException {
		MissingCIDFontSource source = (MissingCIDFontSource) this.source;
		int[] unicodeArray = this.unicodes.toArray();
		short[] w = this.advances.toArray();
		short[] w2;
		if (this.source.getDirection() == FontStyle.DIRECTION_TB) {
			w2 = new short[0];
		} else {
			w2 = null;
		}
		CIDUtils.writeEmbeddedFont(out, xref, source, this, this.fontRef, w, w2, unicodeArray);
	}

	public BBox getBBox() {
		MissingCIDFontSource source = (MissingCIDFontSource) this.source;
		return source.getBBox();
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

	public int getSupplement() {
		return CIDUtils.SUPPLEMENT;
	}

	public String getPSName() {
		return this.source.getFontName();
	}

	public Shape getShapeByGID(int gid) {
		short advance = this.getAdvance(gid);
		int c = this.unicodes.get(gid);
		Shape shape;
		if (advance >= 1000) {
			GeneralPath path = new GeneralPath();
			shape = path;

			String s = Integer.toHexString(c).toUpperCase();
			if (s.length() > 6) {
				s = s.substring(s.length() - 6);
			} else if (s.length() < 4) {
				s = "0000".substring(s.length()) + s;
			}

			float yy = s.length() <= 4 ? 0 : 400;

			float ascent = this.source.getAscent();
			float descent = this.source.getDescent();
			path.moveTo(50, -ascent + 50);
			path.lineTo(950, -ascent + 50);
			path.lineTo(950, descent - 50 + yy);
			path.lineTo(50, descent - 50 + yy);
			path.closePath();
			path.moveTo(70, -ascent + 70);
			path.lineTo(70, descent - 70 + yy);
			path.lineTo(930, descent - 70 + yy);
			path.lineTo(930, -ascent + 70);
			path.closePath();

			for (int i = 0; i < s.length(); ++i) {
				this.chara[0] = s.charAt(i);
				GlyphVector gv = this.numericFont.createGlyphVector(this.frc, this.chara);
				Shape gs = gv.getGlyphOutline(0);
				path.append(gs.getPathIterator(this.at[i]), false);
			}
		} else {
			shape = null;
		}
		return shape;
	}
}
