package jp.cssj.homare.css.property;

import java.net.URI;

import jp.cssj.homare.css.CSSStyle;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: Property.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public interface Property {
	public boolean isImportant();

	public String getName();

	public URI getURI();

	public void applyProperty(CSSStyle style);
}
