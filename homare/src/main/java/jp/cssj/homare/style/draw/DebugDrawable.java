package jp.cssj.homare.style.draw;

import java.awt.geom.Rectangle2D;

import jp.cssj.sakae.gc.GC;
import jp.cssj.sakae.gc.GraphicsException;
import jp.cssj.sakae.gc.paint.Color;

public class DebugDrawable implements Drawable {
	protected final double width;
	protected final double height;
	protected final Color color;

	public DebugDrawable(double width, double height, Color color) {
		this.width = width;
		this.height = height;
		this.color = color;
	}

	public void draw(GC gc, double x, double y) throws GraphicsException {
		gc.begin();
		gc.setStrokePaint(this.color);
		gc.draw(new Rectangle2D.Double(x, y, this.width, this.height));
		gc.end();
	}
}
