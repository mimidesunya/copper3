package jp.cssj.homare.css.util;

import jp.cssj.homare.css.value.Value;
import jp.cssj.homare.ua.UserAgent;
import jp.cssj.sakae.sac.css.LexicalUnit;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: PageValueUtils.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public final class PageValueUtils {
	private PageValueUtils() {
		// unused
	}

	/**
	 * &lt;margin-width&gt;|inherit を値に変換します。
	 * 
	 * @param ua
	 * @param lu
	 * @return
	 */
	public static Value toMarginWidth(UserAgent ua, LexicalUnit lu) {
		short luType = lu.getLexicalUnitType();
		switch (luType) {
		case LexicalUnit.SAC_INHERIT:
		case LexicalUnit.SAC_IDENT:
		case LexicalUnit.SAC_EM:
		case LexicalUnit.SAC_EX:
			return null;

		case LexicalUnit.SAC_PERCENTAGE:
			return ValueUtils.toPercentage(lu);

		default:
			return ValueUtils.toLength(ua, lu);
		}
	}
}