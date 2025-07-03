package jp.cssj.homare.css;

import java.net.URI;

import jp.cssj.homare.css.property.Property;
import jp.cssj.homare.css.property.PropertySet;
import jp.cssj.homare.ua.UserAgent;
import jp.cssj.sakae.sac.css.CSSException;
import jp.cssj.sakae.sac.css.DocumentHandler;
import jp.cssj.sakae.sac.css.InputSource;
import jp.cssj.sakae.sac.css.LexicalUnit;
import jp.cssj.sakae.sac.css.SACMediaList;
import jp.cssj.sakae.sac.css.SelectorList;

/**
 * SACイベントからDeclarationオブジェクトを構築します。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: DeclarationBuilder.java 1554 2018-04-26 03:34:02Z miyabe $
 */
public class DeclarationBuilder implements DocumentHandler {
	private final UserAgent ua;

	private PropertySet propertySet;

	private Declaration declaration;

	private URI uri;

	public DeclarationBuilder(UserAgent ua) {
		assert ua != null;
		this.ua = ua;
	}

	/**
	 * 宣言の存在するスタイルシートのURIを設定します。
	 * 
	 * @param uri
	 */
	public void setURI(URI uri) {
		assert uri != null;
		this.uri = uri;
	}

	public URI getURI() {
		return this.uri;
	}

	public void setPropertySet(PropertySet propertySet) {
		assert propertySet != null;
		this.propertySet = propertySet;
	}

	public void setDeclaration(Declaration declaration) {
		this.declaration = declaration;
	}

	public Declaration getDeclaration() {
		return this.declaration;
	}

	public void startDocument(InputSource source) throws CSSException {
		// ignore
	}

	public void endDocument(InputSource source) throws CSSException {
		// ignore
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

	public void importStyle(String uri, SACMediaList media, String defaultNamespaceURI) throws CSSException {
		// ignore
	}

	public void startMedia(SACMediaList media) throws CSSException {
		// ignore
	}

	public void endMedia(SACMediaList media) throws CSSException {
		// ignore
	}

	public void startPage(String name, String pseudo_page) throws CSSException {
		// ignore
	}

	public void endPage(String name, String pseudoPage) throws CSSException {
		// ignore
	}

	public void startFontFace() throws CSSException {
		// ignore
	}

	public void endFontFace() throws CSSException {
		// ignore
	}

	public void startSelector(SelectorList selectors) throws CSSException {
		// ignore
	}

	public void endSelector(SelectorList selectors) throws CSSException {
		// ignore
	}

	public void property(String name, LexicalUnit lu, boolean important) throws CSSException {
		assert name != null && this.uri != null && lu != null;
		if (this.inProperMedia()) {
			Property property = this.propertySet.parseDeclaration(name, lu, this.ua, this.uri, important);
			if (property == null) {
				return;
			}
			if (this.declaration == null) {
				this.declaration = new Declaration();
			}
			this.declaration.addProperty(property);
			// System.out.println(property);
		}
	}

	protected boolean inProperMedia() {
		return true;
	}
}
