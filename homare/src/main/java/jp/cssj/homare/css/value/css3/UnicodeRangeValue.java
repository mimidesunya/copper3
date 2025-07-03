package jp.cssj.homare.css.value.css3;

import jp.cssj.sakae.gc.font.UnicodeRange;
import jp.cssj.sakae.gc.font.UnicodeRangeList;

/**
 * Unicode-Range です。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: UnicodeRangeValue.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public class UnicodeRangeValue extends UnicodeRangeList implements CSS3Value {
	public static final UnicodeRangeValue EMPTY = new UnicodeRangeValue(new UnicodeRange[0]);

	public UnicodeRangeValue(UnicodeRange[] includes) {
		super(includes);
	}

	public short getValueType() {
		return TYPE_UNICODE_RANGE;
	}
}