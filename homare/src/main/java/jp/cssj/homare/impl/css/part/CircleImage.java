package jp.cssj.homare.impl.css.part;

import java.awt.geom.Ellipse2D;

import jp.cssj.sakae.gc.GC;
import jp.cssj.sakae.gc.GraphicsException;
import jp.cssj.sakae.gc.font.FontStyle;
import jp.cssj.sakae.gc.image.Image;
import jp.cssj.sakae.gc.paint.Color;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: CircleImage.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public class CircleImage implements Image {
	private final double size;

	private final Color color;

	public CircleImage(FontStyle fontStyle, Color color) {
		this.size = fontStyle.getSize();
		this.color = color;
	}

	public double getWidth() {
		return this.size;
	}

	public double getHeight() {
		return this.size;
	}

	public String getAltString() {
		return "○";
	}

	public void drawTo(GC gc) throws GraphicsException {
		gc.begin();

		gc.setFillPaint(this.color);
		gc.setLineWidth(this.size / 24.0);
		gc.setLinePattern(GC.STROKE_SOLID);

		double d = this.size * 0.35;
		gc.draw(new Ellipse2D.Double(this.size / 2.0 - d / 2.0, this.size * 0.2 + this.size / 2.0 - d / 2.0, d, d));

		gc.end();
	}
}
