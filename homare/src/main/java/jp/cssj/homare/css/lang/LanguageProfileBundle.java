package jp.cssj.homare.css.lang;

import java.util.Locale;

import jp.cssj.homare.impl.css.lang.LanguageProfile_ja;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: LanguageProfileBundle.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public final class LanguageProfileBundle {
	private LanguageProfileBundle() {
		// unused
	}

	private static LanguageProfile lp = new LanguageProfile_ja();

	public static LanguageProfile getLanguageProfile(Locale lang) {
		return lp;
	}
}
