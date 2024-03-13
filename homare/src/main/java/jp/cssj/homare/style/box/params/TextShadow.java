package jp.cssj.homare.style.box.params;

import jp.cssj.sakae.gc.paint.Color;

public class TextShadow {
	public final double x, y;
	public final Color color;

	public TextShadow(double x, double y, Color color) {
		this.x = x;
		this.y = y;
		this.color = color;
	}
}
