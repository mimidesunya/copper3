package jp.cssj.sakae.pdf.font.cid.identity;

import java.awt.Font;
import java.awt.font.GlyphVector;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.cssj.sakae.pdf.ObjectRef;
import jp.cssj.sakae.pdf.font.PdfFont;
import jp.cssj.sakae.pdf.font.cid.SystemCIDFontSource;
import jp.cssj.sakae.util.IntList;
import jp.cssj.sakae.util.ShortList;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: SystemExternalCIDFontFace.java,v 1.2 2005/06/10 12:46:30
 *          harumanx Exp $
 */
public class SystemCIDIdentityFontSource extends SystemCIDFontSource {
	private static final Logger LOG = Logger.getLogger(SystemCIDIdentityFontSource.class.getName());

	private static final long serialVersionUID = 2L;

	transient protected WeakReference<IntList> charToGid = null;

	transient protected WeakReference<ShortList> advances = null;

	public SystemCIDIdentityFontSource(Font font) {
		super(font);
	}

	public byte getType() {
		return TYPE_CID_IDENTITY;
	}

	private final char[] chara = new char[1];

	private IntList getCharToGid() {
		IntList charToGid = null;
		if (this.charToGid != null) {
			charToGid = this.charToGid.get();
			if (charToGid != null) {
				return charToGid;
			}
		}
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("build charToGid: " + this.getFontName());
		}
		charToGid = new IntList();
		this.charToGid = new WeakReference<IntList>(charToGid);
		return charToGid;
	}

	private ShortList getAdvances() {
		ShortList advances = null;
		if (this.advances != null) {
			advances = (ShortList) this.advances.get();
			if (advances != null) {
				return advances;
			}
		}
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("build advances: " + this.getFontName());
		}
		advances = new ShortList(Short.MIN_VALUE);
		this.advances = new WeakReference<ShortList>(advances);
		return advances;
	}

	public synchronized int toGID(int c) {
		int gid;
		IntList charToGid = this.getCharToGid();
		if (charToGid.get(c) == 0) {
			this.chara[0] = (char) c;
			GlyphVector gv = this.awtFont.createGlyphVector(this.getFontRenderContext(), this.chara);
			gid = gv.getGlyphCode(0);
			if (gid < 0 || gid > 0xFFFF) {
				LOG.warning("16ビットを超えるGIDの文字は使用できません:" + c + "(gid=" + gid + ")");
				gid = this.awtFont.getMissingGlyphCode();
			}
			charToGid.set(c, gid == 0 ? -1 : gid);
		} else {
			gid = charToGid.get(c);
		}

		return gid == -1 ? 0 : gid;
	}

	private final int[] gida = new int[1];

	public synchronized short getWidth(int gid) {
		ShortList advances = this.getAdvances();
		short advance = advances.get(gid);
		if (advance == 0) {
			this.gida[0] = gid;
			GlyphVector gv = this.awtFont.createGlyphVector(this.getFontRenderContext(), this.gida);
			advance = (short) gv.getGlyphMetrics(0).getAdvance();
			advances.set(gid, advance);
		}
		return advance;
	}

	public PdfFont createFont(String name, ObjectRef fontRef) {
		return new SystemCIDIdentityFont(this, name, fontRef);
	}

	public jp.cssj.sakae.font.Font createFont() {
		return this.createFont(null, null);
	}
}
