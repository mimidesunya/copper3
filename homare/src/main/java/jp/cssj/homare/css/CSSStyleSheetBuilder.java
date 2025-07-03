package jp.cssj.homare.css;

import java.awt.Font;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.cssj.homare.css.property.ElementPropertySet;
import jp.cssj.homare.css.property.FontFacePropertySet;
import jp.cssj.homare.css.property.PagePropertySet;
import jp.cssj.homare.impl.css.property.CSSFontFamily;
import jp.cssj.homare.impl.css.property.CSSFontStyle;
import jp.cssj.homare.impl.css.property.FontWeight;
import jp.cssj.homare.impl.css.property.css3.CSSUnicodeRange;
import jp.cssj.homare.impl.css.property.css3.Src;
import jp.cssj.homare.message.MessageCodes;
import jp.cssj.homare.ua.UserAgent;
import jp.cssj.homare.xml.util.XMLUtils;
import jp.cssj.resolver.Source;
import jp.cssj.resolver.helpers.URIHelper;
import jp.cssj.sakae.gc.font.FontFace;
import jp.cssj.sakae.gc.font.FontManager;
import jp.cssj.sakae.sac.css.CSSException;
import jp.cssj.sakae.sac.css.DocumentHandler;
import jp.cssj.sakae.sac.css.InputSource;
import jp.cssj.sakae.sac.css.LexicalUnit;
import jp.cssj.sakae.sac.css.SACMediaList;
import jp.cssj.sakae.sac.css.SelectorList;
import jp.cssj.sakae.sac.parser.Parser;

/**
 * SACイベントからCSSStyleSheetオブジェクトを構築します。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: CSSStyleSheetBuilder.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public class CSSStyleSheetBuilder implements DocumentHandler {
	private static final Logger LOG = Logger.getLogger(CSSStyleSheetBuilder.class.getName());

	private static final boolean DEBUG = false;

	private static final int MAX_DEPTH = 10;

	private static final short NONE = 1;

	private static final short IN_SELECTOR = 2;

	private static final short IN_PAGE = 3;

	private static final short IN_FONT_FACE = 4;

	private static final short IN_PAGE_CONTENT = 5;

	private final UserAgent ua;

	/* 現在のスタイル宣言 */
	private final DeclarationBuilder declBuilder;

	/* ページごとに生成する内容のスタイル宣言 */
	private final DeclarationBuilder pageContentDeclBuilder;

	/* 現在のページの種類 */
	private String pseudoPage;

	/** スタイルシートのソースのスタック。 */
	private final List<InputSource> sourceStack = new ArrayList<InputSource>();

	/** スタイルシートのURIのスタック。 */
	private final List<URI> uriStack = new ArrayList<URI>();

	/** ブロックを無視するためのスタック。 */
	private final List<Boolean> mediaStack = new ArrayList<Boolean>();

	/** 状態。 */
	private short state = NONE;

	private CSSStyleSheet cssStyleSheet;

	public CSSStyleSheetBuilder(UserAgent ua) {
		this.ua = ua;
		this.declBuilder = new DeclarationBuilder(ua);
		this.declBuilder.setPropertySet(ElementPropertySet.getInstance());
		this.pageContentDeclBuilder = new DeclarationBuilder(ua);
		this.pageContentDeclBuilder.setPropertySet(ElementPropertySet.getInstance());
	}

	public void setCSSStyleSheet(CSSStyleSheet cssStyleSheet) {
		this.cssStyleSheet = cssStyleSheet;
	}

	public CSSStyleSheet getCSSStyleSheet() {
		return this.cssStyleSheet;
	}

	public void comment(String text) throws CSSException {
		// ignore
	}

	public void ignorableAtRule(String atRule) throws CSSException {
		// ignore
	}

	public void namespaceDeclaration(String prefix, String uri) throws CSSException {
		// ignore
	}

	public void property(String name, LexicalUnit lu, boolean important) throws CSSException {
		switch (this.state) {
		case NONE:
			// 無視
			break;
		case IN_PAGE_CONTENT:
			this.pageContentDeclBuilder.property(name, lu, important);
			break;
		default:
			this.declBuilder.property(name, lu, important);
			break;
		}
	}

	public void startDocument(InputSource source) throws CSSException {
		URI uri = URI.create(source.getURI());
		this.sourceStack.add(source);
		this.uriStack.add(uri);
		this.declBuilder.setURI(uri);
		this.pageContentDeclBuilder.setURI(uri);
		if (DEBUG) {
			System.out.println(uri);
		}
	}

	public void endDocument(InputSource source) throws CSSException {
		this.sourceStack.remove(this.sourceStack.size() - 1);
		this.uriStack.remove(this.uriStack.size() - 1);
		if (!this.uriStack.isEmpty()) {
			URI uri = (URI) this.uriStack.get(this.uriStack.size() - 1);
			this.declBuilder.setURI(uri);
			this.pageContentDeclBuilder.setURI(uri);
			if (DEBUG) {
				System.out.println(uri);
			}
		}
	}

	protected InputSource getInputSource() {
		return (InputSource) this.sourceStack.get(this.sourceStack.size() - 1);
	}

	public void importStyle(String href, SACMediaList media, String defaultNamespaceURI) throws CSSException {
		if (DEBUG) {
			System.out.println("import:" + href);
		}
		StringBuffer buff = null;
		for (int i = 0; i < media.getLength(); ++i) {
			String medium = media.item(i);
			if (buff == null) {
				buff = new StringBuffer(medium);
			} else {
				buff.append(' ');
				buff.append(medium);
			}
		}
		// System.out.println(href+"/"+media);
		String mediaTypes = buff.toString();
		if (this.ua.is(mediaTypes)) {
			if (this.sourceStack.size() > MAX_DEPTH) {
				URI uri = (URI) this.uriStack.get(this.uriStack.size() - 1);
				this.ua.message(MessageCodes.WARN_DEEP_IMPORT, uri.toString(), String.valueOf(MAX_DEPTH));
				return;
			}
			URI baseURI = this.declBuilder.getURI();
			URI uri;
			try {
				uri = URIHelper.resolve(this.ua.getDocumentContext().getEncoding(), baseURI, href);
			} catch (URISyntaxException e) {
				this.ua.message(MessageCodes.WARN_MISSING_CSS_STYLESHEET, href);
				return;
			}
			for (int i = 0; i < this.uriStack.size(); ++i) {
				URI stackURI = (URI) this.uriStack.get(i);
				if (stackURI.equals(uri)) {
					this.ua.message(MessageCodes.WARN_LOOP_IMPORT, baseURI.toString(), uri.toString());
					return;
				}
			}
			final Parser parser = new Parser();
			parser.setDocumentHandler(this);
			try {
				Source source = this.ua.resolve(uri);
				try {
					InputSource inputSource = XMLUtils.toSACInputSource(source, this.getInputSource().getEncoding(),
							mediaTypes, null);
					parser.setDefaultCharset(this.ua.getDocumentContext().getEncoding());
					parser.parseStyleSheet(inputSource);
				} finally {
					this.ua.release(source);
				}
			} catch (CSSException e) {
				this.ua.message(MessageCodes.WARN_BAD_CSS_SYNTAX, uri.toString(), e.getMessage());
				LOG.log(Level.FINE, "CSS文法エラー", e);
			} catch (IOException e) {
				this.ua.message(MessageCodes.WARN_MISSING_CSS_STYLESHEET, uri.toString());
				LOG.log(Level.FINE, "CSS読み込みエラー", e);
			}
		}
	}

	public void startMedia(SACMediaList media) throws CSSException {
		// System.out.println(media);
		for (int i = 0; i < media.getLength(); ++i) {
			String medium = media.item(i).toLowerCase();
			if (this.ua.is(medium)) {
				this.mediaStack.add(Boolean.TRUE);
				return;
			}
		}
		this.mediaStack.add(Boolean.FALSE);
	}

	public void endMedia(SACMediaList media) throws CSSException {
		// System.out.println("/"+media);
		this.mediaStack.remove(this.mediaStack.size() - 1);
	}

	protected boolean inProperMedia() {
		if (this.mediaStack.isEmpty()) {
			return true;
		}
		return ((Boolean) this.mediaStack.get(this.mediaStack.size() - 1)).booleanValue();
	}

	public void startPage(String name, String pseudoPage) throws CSSException {
		if ("-cssj-page-content".equalsIgnoreCase(pseudoPage)) {
			if (this.inProperMedia()) {
				this.state = IN_PAGE_CONTENT;
			}
			return;
		}
		if (name != null) {
			URI uri = (URI) this.uriStack.get(this.uriStack.size() - 1);
			this.ua.message(MessageCodes.WARN_BAD_CSS_SYNTAX, uri.toString(), "名前つきページはサポートしていません");
			return;
		}
		if (this.inProperMedia()) {
			if (this.state != NONE) {
				URI uri = (URI) this.uriStack.get(this.uriStack.size() - 1);
				this.ua.message(MessageCodes.WARN_BAD_CSS_SYNTAX, uri.toString(), "@pageルールがネストされています。");
			}
			this.declBuilder.setDeclaration(null);
			this.declBuilder.setPropertySet(PagePropertySet.getInstance());
			this.state = IN_PAGE;
			this.pseudoPage = pseudoPage;
		}
	}

	public void endPage(String name, String pseudoPage) throws CSSException {
		if ("-cssj-page-content".equalsIgnoreCase(pseudoPage)) {
			if (this.inProperMedia()) {
				this.cssStyleSheet.addPageContent(name, this.pseudoPage, this.pageContentDeclBuilder.getDeclaration());
				this.pageContentDeclBuilder.setDeclaration(null);
			}
			this.state = NONE;
			return;
		}
		if (name != null) {
			return;
		}
		if (this.inProperMedia()) {
			this.cssStyleSheet.addPage(pseudoPage, this.declBuilder.getDeclaration());
			this.state = NONE;
			this.declBuilder.setDeclaration(null);
			this.declBuilder.setPropertySet(ElementPropertySet.getInstance());
		}
	}

	public void startFontFace() throws CSSException {
		if (this.state != NONE) {
			URI uri = (URI) this.uriStack.get(this.uriStack.size() - 1);
			this.ua.message(MessageCodes.WARN_BAD_CSS_SYNTAX, uri.toString(), "@font-faceルールがネストされています。");
		}
		this.state = IN_FONT_FACE;
		this.declBuilder.setDeclaration(null);
		this.declBuilder.setPropertySet(FontFacePropertySet.getInstance());
	}

	public void endFontFace() throws CSSException {
		Declaration decl = this.declBuilder.getDeclaration();
		if (decl == null) {
			return;
		}
		CSSStyle style = CSSStyle.getCSSStyle(this.ua, null, null);
		decl.applyProperties(style);
		URI[] uris = Src.get(style);
		if (uris != null) {
			boolean missing = true;
			for (int i = 0; i < uris.length; ++i) {
				URI uri = uris[i];
				try {
					Source src = null;
					try {
						FontFace face;
						if (uri.getScheme() != null && uri.getScheme().equals("local-font")) {
							String name = uri.getSchemeSpecificPart();
							Font local = Font.decode(name);
							// System.err.println(name+"/"+local);
							if (local == null) {
								continue;
							}
							face = new FontFace();
							face.local = local;
						} else {
							src = this.ua.resolve(uri);
							if (!src.exists()) {
								continue;
							}
							face = new FontFace();
							face.src = src;
						}

						face.fontFamily = CSSFontFamily.get(style);
						face.fontWeight = FontWeight.get(style);
						face.fontStyle = CSSFontStyle.get(style);
						face.unicodeRange = CSSUnicodeRange.get(style);
						FontManager fm = this.ua.getFontManager();
						fm.addFontFace(face);
						missing = false;
						break;
					} finally {
						if (src != null) {
							this.ua.release(src);
						}
					}
				} catch (Exception e) {
					LOG.log(Level.FINE, "Font error", e);
				}
			}
			if (missing) {
				this.ua.message(MessageCodes.WARN_MISSING_FONT_FILE, Arrays.asList(uris).toString());
			}
		}

		this.state = NONE;
		this.declBuilder.setDeclaration(null);
		this.declBuilder.setPropertySet(ElementPropertySet.getInstance());
	}

	public void startSelector(SelectorList selectors) throws CSSException {
		if (DEBUG) {
			System.out.println(selectors);
		}
		if (this.inProperMedia()) {
			// 宣言の構築を準備する
			if (this.state != NONE) {
				URI uri = (URI) this.uriStack.get(this.uriStack.size() - 1);
				this.ua.message(MessageCodes.WARN_BAD_CSS_SYNTAX, uri.toString(), "セレクタがネストされています。");
			}
			this.declBuilder.setDeclaration(null);
			this.state = IN_SELECTOR;
		}
	}

	public void endSelector(SelectorList selectors) throws CSSException {
		if (this.inProperMedia()) {
			this.cssStyleSheet.addRule(selectors, this.declBuilder.getDeclaration());
			// 宣言の構築を終了した
			this.state = NONE;
		}
	}
}
