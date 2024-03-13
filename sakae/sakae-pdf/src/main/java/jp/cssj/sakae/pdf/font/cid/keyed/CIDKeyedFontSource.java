package jp.cssj.sakae.pdf.font.cid.keyed;

import java.awt.Font;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.cssj.sakae.font.AbstractFontSource;
import jp.cssj.sakae.font.BBox;
import jp.cssj.sakae.gc.font.FontStyle;
import jp.cssj.sakae.gc.font.Panose;
import jp.cssj.sakae.pdf.ObjectRef;
import jp.cssj.sakae.pdf.font.PdfFont;
import jp.cssj.sakae.pdf.font.cid.CIDFontSource;
import jp.cssj.sakae.pdf.font.cid.CMap;
import jp.cssj.sakae.pdf.font.cid.WArray;
import jp.cssj.sakae.pdf.font.util.PdfFontUtils;

/**
 * 等幅の一般フォントです。 このフォントはプラットフォームによって書体が変わります。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: GenericType0FontFace.java,v 1.2 2005/06/06 04:42:24 harumanx
 *          Exp $
 */
public class CIDKeyedFontSource extends AbstractFontSource implements CIDFontSource {
	private static final Logger LOG = Logger.getLogger(CIDKeyedFontSource.class.getName());

	private static final long serialVersionUID = 1L;

	protected final CMap hcmap, vcmap;

	protected String fontName;

	protected BBox bbox;

	protected short ascent, descent, capHeight, xHeight, stemH, stemV, spaceAdvance;

	protected WArray warray;

	protected Panose panose;

	transient protected Font awtFont = null;

	public CIDKeyedFontSource(CMap hcmap, CMap vcmap) {
		if (hcmap == null) {
			throw new NullPointerException();
		}
		this.hcmap = hcmap;
		this.vcmap = vcmap;
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("new font: " + this.getFontName());
		}
	}

	public byte getDirection() {
		return this.vcmap == null ? FontStyle.DIRECTION_LTR : FontStyle.DIRECTION_TB;
	}

	public String getFontName() {
		return this.fontName;
	}

	public BBox getBBox() {
		return this.bbox;
	}

	public short getAscent() {
		return this.ascent;
	}

	public short getDescent() {
		return this.descent;
	}

	public short getCapHeight() {
		return this.capHeight;
	}

	public short getXHeight() {
		return this.xHeight;
	}

	public short getStemH() {
		return this.stemH;
	}

	public short getStemV() {
		return this.stemV;
	}

	public WArray getWArray() {
		return this.warray;
	}

	public short getSpaceAdvance() {
		return this.spaceAdvance;
	}

	public void setFontName(String fontName) {
		this.fontName = fontName;
	}

	public void setPanose(Panose panose) {
		this.panose = panose;
	}

	public void setBBox(BBox bbox) {
		this.bbox = bbox;
	}

	public void setAscent(short ascent) {
		this.ascent = ascent;
	}

	public void setDescent(short descent) {
		this.descent = descent;
	}

	public void setCapHeight(short capHeight) {
		this.capHeight = capHeight;
	}

	public void setXHeight(short xHeight) {
		this.xHeight = xHeight;
	}

	public void setWArray(WArray warray) {
		if (warray == null) {
			throw new NullPointerException();
		}
		this.warray = warray;
		this.spaceAdvance = warray.getWidth(this.hcmap.getCIDTable().toCID(' '));
	}

	public void setStemH(short stemH) {
		this.stemH = stemH;
	}

	public void setStemV(short stemV) {
		this.stemV = stemV;
	}

	protected synchronized Font getAwtFont() {
		if (this.awtFont == null) {
			this.awtFont = PdfFontUtils.toAwtFont(this);
		}
		return this.awtFont;
	}

	public byte getType() {
		return TYPE_CID_KEYED;
	}

	public boolean canDisplay(int c) {
		return this.hcmap.getCIDTable().containsChar(c);
	}

	public Panose getPanose() {
		return this.panose;
	}

	public PdfFont createFont(String name, ObjectRef fontRef) {
		switch (this.getDirection()) {
		case FontStyle.DIRECTION_LTR:
		case FontStyle.DIRECTION_RTL:// TODO RTL
			// 横書き
			return new CIDKeyedFont(this, name, fontRef, this.hcmap);
		case FontStyle.DIRECTION_TB:
			// 縦書き
			return new CIDKeyedFont(this, name, fontRef, this.vcmap);
		default:
			throw new IllegalArgumentException();
		}
	}

	public jp.cssj.sakae.font.Font createFont() {
		return this.createFont(null, null);
	}
}
