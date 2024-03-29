package jp.cssj.sakae.pdf.font;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.LRUMap;

import jp.cssj.sakae.font.FontSource;
import jp.cssj.sakae.font.FontSourceManager;
import jp.cssj.sakae.font.FontSourceWrapper;
import jp.cssj.sakae.gc.font.FontFace;
import jp.cssj.sakae.gc.font.FontFamily;
import jp.cssj.sakae.gc.font.FontFamilyList;
import jp.cssj.sakae.gc.font.FontPolicyList;
import jp.cssj.sakae.gc.font.FontStyle;
import jp.cssj.sakae.gc.font.UnicodeRangeList;
import jp.cssj.sakae.gc.font.util.FontUtils;
import jp.cssj.sakae.pdf.ObjectRef;
import jp.cssj.sakae.pdf.font.util.MultimapUtils;
import jp.cssj.sakae.util.NumberUtils;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: PdfFontSourceManager.java 1635 2023-04-03 08:16:41Z miyabe $
 */
public class PdfFontSourceManager implements FontSourceManager {
	protected Map<String, Object> nameToFonts = new HashMap<String, Object>();

	protected Map<String, FontFamilyList> genericToFamily = new HashMap<String, FontFamilyList>();

	protected Map<URI, File> uriToFile = new HashMap<URI, File>();

	protected Collection<FontSource> allFonts = new ArrayList<FontSource>();

	transient protected Map<FontStyle, FontSource[]> fontListCache = null;

	protected final boolean strictMatchName;

	public PdfFontSourceManager(boolean strictMatchName) {
		this.strictMatchName = strictMatchName;
	}

	public PdfFontSourceManager() {
		this(false);
	}

	protected void finalize() throws Throwable {
		super.finalize();
		for (Iterator<File> i = this.uriToFile.values().iterator(); i.hasNext();) {
			File file = i.next();
			file.delete();
		}
	}

	public synchronized void addFontFace(FontFace face) throws IOException {
		final List<FontSource> list = new ArrayList<FontSource>();
		if (face.local != null) {
			list.add(FontLoader.readSystemFont(face, FontLoader.TYPE_EMBEDDED, face.local, null));
			list.add(FontLoader.readSystemFont(face, FontLoader.TYPE_CID_IDENTITY, face.local, null));
		} else {
			File file;
			if (face.src.isFile()) {
				file = face.src.getFile();
			} else {
				file = this.uriToFile.get(face.src.getURI());
				if (file == null) {
					byte[] buff = new byte[8192];
					file = File.createTempFile("copper-font-face", ".font");
					try (InputStream in = face.src.getInputStream(); OutputStream out = new FileOutputStream(file)) {
						for (int len = in.read(buff); len != -1; len = in.read(buff)) {
							out.write(buff, 0, len);
						}
					}
					this.uriToFile.put(face.src.getURI(), file);
				}
			}
			FontLoader.readTTF(list, face, FontLoader.TYPE_EMBEDDED, file, face.index, null);
			FontLoader.readTTF(list, face, FontLoader.TYPE_CID_IDENTITY, file, face.index, null);
		}

		if (face.unicodeRange != null && !face.unicodeRange.isEmpty()) {
			for (int i = 0; i < list.size(); ++i) {
				PdfFontSource source = (PdfFontSource) list.get(i);
				list.set(i, new PdfFontSourceWrapper(source, face.unicodeRange));
			}
		}
		this.allFonts.addAll(list);

		final List<String> m = new ArrayList<String>();
		if (face.fontFamily != null) {
			for (int i = 0; i < face.fontFamily.getLength(); ++i) {
				final FontFamily family = face.fontFamily.get(i);
				String name = family.getName();
				if (family.isGenericFamily()) {
					// 基本フォント名を上書きする
					final FontFamilyList generics = this.genericToFamily.get(name);
					if (generics == null) {
						this.genericToFamily.put(name, new FontFamilyList(new FontFamily(name)));
					} else {
						boolean found = false;
						for (int j = 0; j < generics.getLength(); ++j) {
							if (generics.get(j).getName().equals(name)) {
								found = true;
								break;
							}
						}
						if (!found) {
							final FontFamily[] families = new FontFamily[generics.getLength() + 1];
							for (int j = 0; j < generics.getLength(); ++j) {
								families[j] = generics.get(j);
							}
							families[generics.getLength()] = new FontFamily(name);
							this.genericToFamily.put(name, new FontFamilyList(families));
						}
					}
				}
				name = FontUtils.normalizeName(name);
				if (m.contains(name)) {
					continue;
				}
				for (int j = 0; j < list.size(); ++j) {
					FontSource source = list.get(j);
					MultimapUtils.putDirect(this.nameToFonts, name, source);
				}
				m.add(name);
			}
		}
		// FontSourceから得られるフォント名を入れる
		// CSSの@font-faceでは不要なのでコメントアウトしている
		// for (int j = 0; j < list.size(); ++j) {
		// FontSource source = (FontSource) list.get(j);
		// String[] aliases = source.getAliases();
		// if (aliases != null) {
		// for (int i = 0; i < aliases.length; ++i) {
		// String name = FontUtils.normalizeName(aliases[i]);
		// if (m.contains(name)) {
		// continue;
		// }
		// MultimapUtils.putDirect(this.nameToFonts, name, source);
		// m.add(name);
		// }
		// }
		// String name = FontUtils.normalizeName(source.getFontName());
		// if (m.contains(name)) {
		// continue;
		// }
		// MultimapUtils.putDirect(this.nameToFonts, name, source);
		// m.add(name);
		// }
	}

	@SuppressWarnings("unchecked")
	public synchronized FontSource[] lookup(final FontStyle fontStyle) {
		if (fontStyle == null) {
			return this.allFonts.toArray(new FontSource[this.allFonts.size()]);
		}

		FontSource[] fonts;
		if (this.fontListCache != null) {
			fonts = this.fontListCache.get(fontStyle);
			if (fonts != null) {
				return fonts;
			}
		}

		final List<FontSource> fontList = new ArrayList<FontSource>();
		this.lookup(fontStyle, fontStyle.getFamily(), fontList, false);
		fonts = fontList.toArray(new FontSource[fontList.size()]);
		if (this.fontListCache == null) {
			this.fontListCache = new LRUMap();
		}
		this.fontListCache.put(fontStyle, fonts);
		return fonts;
	}

	protected void lookup(FontStyle fontStyle, FontFamilyList family, List<FontSource> fontList, boolean recurse) {
		for (int i = 0; i < family.getLength(); ++i) {
			FontSource[] fonts;
			FontFamily entry = family.get(i);
			String name = entry.getName();

			// ファミリ名が一致するフォントを取得
			if (entry.isGenericFamily()) {
				if (recurse) {
					throw new IllegalStateException("一般フォントが一般フォントで定義されています");
				}
				FontFamilyList gfamily = this.genericToFamily.get(name);
				if (gfamily != null) {
					this.lookup(fontStyle, gfamily, fontList, true);
				}
				continue;
			} else {
				name = FontUtils.normalizeName(name);
				fonts = MultimapUtils.get(this.nameToFonts, name);
				if (fonts == null) {
					continue;
				}
			}

			// 各条件のマッチング
			FontPolicyList policy = fontStyle.getPolicy();
			Object[][] orders = new Object[fonts.length][2];
			for (int j = 0; j < fonts.length; ++j) {
				FontSource font = fonts[j];
				int order = 0;

				// フォントのタイプが最優先条件
				if (font instanceof PdfFontSource) {
					PdfFontSource pdfFont = (PdfFontSource) font;
					byte type = pdfFont.getType();
					for (int k = 0; k < policy.getLength(); ++k) {
						byte policyCode = policy.get(k);
						switch (policyCode) {
						case FontPolicyList.FONT_POLICY_CORE:
							// CORE
							if (type != PdfFontSource.TYPE_CORE) {
								continue;
							}
							break;

						case FontPolicyList.FONT_POLICY_CID_KEYED:
							// CID-Keyed
							if (type != PdfFontSource.TYPE_CID_KEYED) {
								continue;
							}
							break;

						case FontPolicyList.FONT_POLICY_CID_IDENTITY:
							// CID外部
							if (type != PdfFontSource.TYPE_CID_IDENTITY) {
								continue;
							}
							break;

						case FontPolicyList.FONT_POLICY_EMBEDDED:
							// 埋め込み
							if (type != PdfFontSource.TYPE_EMBEDDED) {
								continue;
							}
							break;

						case FontPolicyList.FONT_POLICY_OUTLINES:
							// アウトライン化
							continue;

						default:
							throw new IllegalStateException();
						}
						order = policy.getLength() - k + 1;
						break;
					}
					if (order == 0) {
						continue;
					}
				}
				else {
					order = 1;
				}

				// 横書きモードでは縦書きフォントを排除する
				byte direction = fontStyle.getDirection();
				byte fsDirection = font.getDirection();
				if (direction != FontStyle.DIRECTION_TB && fsDirection == FontStyle.DIRECTION_TB) {
					continue;
				}

				// ファミリ名が完全に一致するものを優先する
				order <<= 4;
				String fontName = FontUtils.normalizeName(font.getFontName());
				if (fontName.equals(name)) {
					order |= 1;
				} else if (this.strictMatchName) {
					continue;
				}

				// italicの判定はウエイトより優先する
				order <<= 4;
				short style = fontStyle.getStyle();
				if (style == FontStyle.FONT_STYLE_ITALIC) {
					if (font.isItalic()) {
						order |= 1;
					}
				} else if (style == FontStyle.FONT_STYLE_NORMAL) {
					if (!font.isItalic()) {
						order |= 1;
					}
				}

				// ウエイトの判定
				order <<= 4;
				short weight = fontStyle.getWeight();
				int delta = Math.abs(font.getWeight() - weight);
				order |= (0xF & ((1000 - delta) / 100));

				// obliqueは変換を使えばよいので優先順位は低い
				order <<= 4;
				if (style == FontStyle.FONT_STYLE_OBLIQUE) {
					if (font.isItalic()) {
						order |= 1;
					}
				}
				orders[j][0] = NumberUtils.intValue(order);
				orders[j][1] = font;
			}
			Arrays.sort(orders, FONT_COMP);
			for (int j = 0; j < fonts.length; ++j) {
				Integer order = (Integer) orders[j][0];
				if (order != null) {
					FontSource font = (FontSource) orders[j][1];
					fontList.add(font);
				}
			}
		}
	}

	private static final Comparator<Object[]> FONT_COMP = new Comparator<Object[]>() {
		public int compare(Object[] f1, Object[] f2) {
			Integer i1 = (Integer) f1[0];
			Integer i2 = (Integer) f2[0];
			if (i1 == null & i2 == null) {
				return 0;
			}
			if (i1 == null & i2 != null) {
				return 1;
			}
			if (i1 != null & i2 == null) {
				return -1;
			}
			if (i1.intValue() < i2.intValue()) {
				return 1;
			}
			if (i1.intValue() > i2.intValue()) {
				return -1;
			}
			return 0;
		}
	};

	private static class PdfFontSourceWrapper extends FontSourceWrapper implements PdfFontSource {
		private static final long serialVersionUID = 1L;

		protected final UnicodeRangeList includes;

		public PdfFontSourceWrapper(PdfFontSource source, UnicodeRangeList includes) {
			super(source);
			this.includes = includes;
			assert !includes.isEmpty();
		}

		public boolean canDisplay(int c) {
			if (this.includes.canDisplay(c)) {
				return this.source.canDisplay(c);
			}
			return false;
		}

		public PdfFont createFont(String name, ObjectRef fontRef) {
			return ((PdfFontSource) this.source).createFont(name, fontRef);
		}

		public byte getType() {
			return ((PdfFontSource) this.source).getType();
		}
	}
}