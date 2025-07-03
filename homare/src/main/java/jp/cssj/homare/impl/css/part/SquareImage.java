package jp.cssj.homare.impl.css.part;

import java.awt.geom.Rectangle2D;

import jp.cssj.sakae.gc.GC;
import jp.cssj.sakae.gc.GraphicsException;
import jp.cssj.sakae.gc.font.FontStyle;
import jp.cssj.sakae.gc.image.Image;
import jp.cssj.sakae.gc.paint.Color;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: SquareImage.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public class SquareImage implements Image {
	protected final double size;

	protected final Color color;

	public SquareImage(FontStyle fontStyle, Color color) {
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
		return "■";
	}

	public void drawTo(GC gc) throws GraphicsException {
		gc.begin();

		gc.setFillPaint(this.color);

		double d = this.size * 0.35;
		gc.fill(new Rectangle2D.Double(this.size / 2.0 - d / 2.0, this.size * 0.2 + this.size / 2.0 - d / 2.0, d, d));
		gc.end();
	}
}
