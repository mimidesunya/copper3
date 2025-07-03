package jp.cssj.homare.style.box.params;

import jp.cssj.sakae.gc.paint.Color;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: Decoration.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public class Decoration {
	public final Color underlineColor;

	public final Color overlineColor;

	public final Color lineThroughColor;

	public Decoration(Color underlineColor, Color overlineColor, Color lineThroughColor) {
		this.underlineColor = underlineColor;
		this.overlineColor = overlineColor;
		this.lineThroughColor = lineThroughColor;
	}
}