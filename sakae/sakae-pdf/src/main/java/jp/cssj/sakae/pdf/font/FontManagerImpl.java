package jp.cssj.sakae.pdf.font;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jp.cssj.sakae.font.DefaultFontStore;
import jp.cssj.sakae.font.Font;
import jp.cssj.sakae.font.FontMetricsImpl;
import jp.cssj.sakae.font.FontSource;
import jp.cssj.sakae.font.FontSourceManager;
import jp.cssj.sakae.font.FontStore;
import jp.cssj.sakae.gc.font.FontFace;
import jp.cssj.sakae.gc.font.FontListMetrics;
import jp.cssj.sakae.gc.font.FontManager;
import jp.cssj.sakae.gc.font.FontMetrics;
import jp.cssj.sakae.gc.font.FontStyle;
import jp.cssj.sakae.gc.text.GlyphHandler;
import jp.cssj.sakae.gc.text.Glypher;
import jp.cssj.sakae.gc.text.Quad;
import jp.cssj.sakae.pdf.font.cid.missing.MissingCIDFontSource;
import jp.cssj.sakae.pdf.font.cid.missing.SpaceCIDFontSource;

/**
 * Implementation of FontManager for PDF.
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: FontManagerImpl.java 1633 2023-02-12 03:22:32Z miyabe $
 */
public class FontManagerImpl implements FontManager {
	private static final long serialVersionUID = 1L;

	protected final FontSourceManager globaldb;

	protected PdfFontSourceManager localdb = null;

	protected final FontStore fontStore;

	protected final Map<FontStyle, FontListMetrics> fontListMetricsCache = new HashMap<FontStyle, FontListMetrics>();

	public FontManagerImpl(FontSourceManager fontdb, FontStore fontStore) {
		assert fontdb != null;
		this.globaldb = fontdb;
		this.fontStore = fontStore;
	}

	public FontManagerImpl(FontSourceManager fontdb) {
		this(fontdb, new DefaultFontStore());
	}

	public void addFontFace(FontFace face) throws IOException {
		if (this.localdb == null) {
			this.localdb = new PdfFontSourceManager(true);
		}
		this.localdb.addFontFace(face);
	}

	public FontListMetrics getFontListMetrics(FontStyle fontStyle) {
		FontListMetrics flm = (FontListMetrics) this.fontListMetricsCache.get(fontStyle);
		if (flm != null) {
			return flm;
		}
		int count = 2;
		FontSource[] fonts1;
		if (this.localdb != null) {
			fonts1 = this.localdb.lookup(fontStyle);
			count += fonts1.length;
		} else {
			fonts1 = null;
		}
		FontSource[] fonts2 = this.globaldb.lookup(fontStyle);
		count += fonts2.length;
		FontMetrics[] fms = new FontMetrics[count];
		int j = 0;

		if (fonts1 != null) {
			for (int i = 0; i < fonts1.length; ++i) {
				fms[j++] = new FontMetricsImpl(this.fontStore, fonts1[i], fontStyle);
			}
		}
		for (int i = 0; i < fonts2.length; ++i) {
			fms[j++] = new FontMetricsImpl(this.fontStore, fonts2[i], fontStyle);
		}

		if (fontStyle.getDirection() == FontStyle.DIRECTION_TB) {
			fms[fms.length - 2] = new FontMetricsImpl(this.fontStore, SpaceCIDFontSource.INSTANCES_TB, fontStyle);
			fms[fms.length - 1] = new FontMetricsImpl(this.fontStore, MissingCIDFontSource.INSTANCES_TB, fontStyle);
		} else {
			fms[fms.length - 2] = new FontMetricsImpl(this.fontStore, SpaceCIDFontSource.INSTANCES_LTR, fontStyle);
			fms[fms.length - 1] = new FontMetricsImpl(this.fontStore, MissingCIDFontSource.INSTANCES_LTR, fontStyle);
		}
		flm = new FontListMetrics(fms);
		this.fontListMetricsCache.put(fontStyle, flm);
		return flm;
	}

	public Glypher getGlypher() {
		return new CharacterHandler();
	}

	protected class CharacterHandler implements Glypher {
		private GlyphHandler glyphHandler;

		private final char[] ch = new char[12];

		private int charOffset;

		private byte len;

		private int gid;

		private FontListMetrics fontListMetrics = null;

		private int fontBound = 0;

		private boolean zw = false; // ZWS(u200B), ZWJ(u200D)およびそれに続く文字はフォントを変えない

		private FontStyle fontStyle;

		private FontMetricsImpl fontMetrics = null;

		private int pgid = -1;

		private boolean outOfRun = true;

		public CharacterHandler() {
			// ignore
		}

		public void setGlyphHander(GlyphHandler glyphHandler) {
			this.glyphHandler = glyphHandler;
		}

		public void fontStyle(FontStyle fontStyle) {
			this.glyphBreak();
			this.fontListMetrics = null;
			this.fontStyle = fontStyle;
		}

		private void glyphBreak() {
			if (!this.outOfRun) {
				this.glyph();
				this.endRun();
				this.zw = false;
			}
		}

		public void characters(int charOffset, char[] ch, int off, int len) {
			if (this.fontListMetrics == null) {
				this.fontListMetrics = FontManagerImpl.this.getFontListMetrics(this.fontStyle);
				this.initFont();
			}

			for (int k = 0; k < len; ++k) {
				this.charOffset = charOffset + k;
				char c = ch[k + off];

				// \A0は空白に変換
				int cc = c == '\u00A0' ? '\u0020' : c;

				// サロゲートペアの処理
				char ls = 0;
				if (Character.isHighSurrogate((char) cc)) {
					if (k + 1 < len && Character.isLowSurrogate(ch[k + off + 1])) {
						++k;
						cc = Character.toCodePoint((char) cc, ls = ch[k + off]);
					}
					else {
						cc = '\u0020';
					}
				}

				// ランの範囲を作成
				if (this.fontMetrics.canDisplay(cc)) {
					if (cc >= 0x200B && cc <= 0x200D) {
						this.zw = true;
					}
				} else {
					this.glyphBreak();
					this.initFont();
				}
				if (!this.zw) {
					// 優先順位の高いフォントに切り替える
					for (int j = 0; j < this.fontBound; ++j) {
						FontMetricsImpl metrics = (FontMetricsImpl) this.fontListMetrics.getFontMetrics(j);
						if (metrics.canDisplay(cc)) {
							this.glyphBreak();
							this.fontMetrics = (FontMetricsImpl) this.fontListMetrics.getFontMetrics(j);
							this.fontBound = j;
							break;
						}
					}
				}

				// 通常の文字として処理
				Font font = this.fontMetrics.getFont();
				int gid = font.toGID(cc);

				if (this.outOfRun) {
					this.outOfRun = false;
					this.gid = -1;
					this.pgid = -1;
					this.len = 0;
					this.glyphHandler.startTextRun(charOffset, this.fontStyle, this.fontMetrics);
				}

				// 連字のチェック
				int lgid = this.len >= this.ch.length ? -1 : this.fontMetrics.getLigature(this.gid, cc);
				if (lgid != -1) {
					// 連字にできる
					this.gid = this.pgid;
					gid = lgid;
					this.ch[this.len] = c;
					++this.len;
					if (ls != 0) {
						this.ch[this.len] = ls;
						++this.len;
					}
				} else {
					// 連字にできない
					if (this.gid != -1) {
						this.glyph();
					}
					this.ch[0] = c;
					this.len = 1;
					if (ls != 0) {
						this.ch[1] = ls;
						this.len = 2;
					}
					this.zw = (cc >= 0x200B && cc <= 0x200D);
				}

				this.pgid = this.gid;
				this.gid = gid;
			}
		}

		public void quad(Quad quad) {
			this.glyphBreak();
			this.glyphHandler.quad(quad);
		}

		private void glyph() {
			this.glyphHandler.glyph(this.charOffset, this.ch, 0, this.len, this.gid);
		}

		private void endRun() {
			assert this.outOfRun == false;
			this.glyphHandler.endTextRun();
			this.outOfRun = true;
		}

		private void initFont() {
			this.fontBound = this.fontListMetrics.getLength();
			this.fontMetrics = (FontMetricsImpl) this.fontListMetrics.getFontMetrics(this.fontBound - 1);
			this.zw = false;
		}

		public void flush() {
			this.glyphBreak();
			this.glyphHandler.flush();
		}

		public void finish() {
			this.glyphBreak();
			this.glyphHandler.finish();
		}
	}
}