package jp.cssj.sakae.pdf.font.cid.missing;

import java.awt.Font;

import jp.cssj.sakae.font.AbstractFontSource;
import jp.cssj.sakae.font.BBox;
import jp.cssj.sakae.font.FontSource;
import jp.cssj.sakae.gc.font.FontStyle;
import jp.cssj.sakae.gc.font.Panose;
import jp.cssj.sakae.pdf.ObjectRef;
import jp.cssj.sakae.pdf.font.PdfFont;
import jp.cssj.sakae.pdf.font.cid.CIDFontSource;

/**
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: GenericType0FontFace.java,v 1.2 2005/06/06 04:42:24 harumanx
 *          Exp $
 */
public class MissingCIDFontSource extends AbstractFontSource implements CIDFontSource {
	private static final long serialVersionUID = 5L;

	private static final BBox BBOX = new BBox((short) 0, DEFAULT_DESCENT, (short) 1000, DEFAULT_ASCENT);

	public static final MissingCIDFontSource INSTANCES_LTR = new MissingCIDFontSource(FontStyle.DIRECTION_LTR);
	public static final MissingCIDFontSource INSTANCES_TB = new MissingCIDFontSource(FontStyle.DIRECTION_TB);

	private final byte direction;

	MissingCIDFontSource(byte direction) {
		this.direction = direction;
	}

	public byte getDirection() {
		return this.direction;
	}

	public String getFontName() {
		return "MISSING";
	}

	public BBox getBBox() {
		return BBOX;
	}

	public short getAscent() {
		return DEFAULT_ASCENT;
	}

	public short getDescent() {
		return DEFAULT_DESCENT;
	}

	public short getCapHeight() {
		return DEFAULT_CAP_HEIGHT;
	}

	public short getXHeight() {
		return DEFAULT_X_HEIGHT;
	}

	public short getSpaceAdvance() {
		return FontSource.DEFAULT_UNITS_PER_EM / 2;
	}

	public short getStemH() {
		return 0;
	}

	public short getStemV() {
		return 0;
	}

	public Font getAwtFont() {
		return null;
	}

	public byte getType() {
		return TYPE_MISSING;
	}

	public boolean canDisplay(int c) {
		return !SpaceCIDFontSource.INSTANCES_LTR.canDisplay(c);
	}

	public Panose getPanose() {
		return null;
	}

	public PdfFont createFont(String name, ObjectRef fontRef) {
		return new MissingCIDFont(this, name, fontRef);
	}

	public jp.cssj.sakae.font.Font createFont() {
		return this.createFont(null, null);
	}
}
